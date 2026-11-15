package com.my.chery.wear

import android.content.Context
import android.util.Log
import com.my.chery.data.AppConfig
import com.my.chery.data.VehicleData
import com.my.chery.net.CheryApi
import com.my.chery.net.VehicleRepository
import com.xiaomi.xms.wearable.Wearable
import com.xiaomi.xms.wearable.auth.Permission
import com.xiaomi.xms.wearable.message.MessageApi
import com.xiaomi.xms.wearable.message.OnMessageReceivedListener
import com.xiaomi.xms.wearable.node.Node
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job

/** 手表回传的单个加速度采样点。 */
data class WatchSample(
    val t: Long,
    val x: Double,
    val y: Double,
    val z: Double,
    val n: Double,
    val d: Double
)

/** 一次采集（手机下发开始，手表按 dUntil 窗口回传）。 */
data class WatchBatch(
    val id: Long,
    val startedAt: Long,
    val plannedMs: Long,
    val samples: List<WatchSample> = emptyList()
) {
    val count: Int get() = samples.size
    val durationMs: Long get() = if (samples.size < 2) 0L else samples.last().t - samples.first().t
    val hz: Double get() = if (durationMs <= 0L) 0.0 else (samples.size - 1) * 1000.0 / durationMs
    val maxD: Double get() = samples.maxOfOrNull { it.d } ?: 0.0
    val maxDn: Double get() = samples.indices.drop(1).maxOfOrNull { kotlin.math.abs(samples[it].n - samples[it - 1].n) } ?: 0.0

    /** 按手表当前判据推演会触发几次「摇一摇」：单帧强脉冲，10 秒冷却。 */
    val triggers: List<Long> get() {
        val out = mutableListOf<Long>()
        var cool = 0L
        samples.forEach { s ->
            if (s.d >= 14.0 && s.n >= 18.0 && s.n < 45.0 && s.t - cool > 10_000L) {
                cool = s.t
                out += s.t
            }
        }
        return out
    }
}

/**
 * 小米穿戴互联桥：手表端（Vela 快应用）通过 @system.interconnect 发 JSON 请求，
 * 这里负责应答 /ping、/status、/refresh、/command，车况与控制复用本 App 的云端实现。
 *
 * 报文格式（与手表端一致）：
 *   请求  {id, path, method, body, token}
 *   应答  {id, http, code, data} 或 {id, http, code, message}
 */
object WearBridge {
    private const val TAG = "WearBridge"
    private const val WATCH_TOKEN = "b33d986be4e94e59"
    private const val MAX_CLOCK_SKEW_MS = 120_000L
    private const val CACHE_TTL_MS = 180_000L
    private const val NONCE_LIMIT = 200

    val state = MutableStateFlow("未启动")

    /** 采集中的实时数据（手机 UI 订阅）与最近几次采集结果。 */
    val dbgLive = MutableStateFlow<WatchBatch?>(null)
    val dbgHistory = MutableStateFlow<List<WatchBatch>>(emptyList())

    /** 手表最近一次触发（不依赖采集窗口），如「开后备箱 20:15:31」。 */
    val lastEvent = MutableStateFlow("")

    /** 手表最近识别到的手势，最新在前，如「20:15:31 · 攥拳 → 开后备箱」。 */
    val gestureLog = MutableStateFlow<List<String>>(emptyList())
    private var gestureBusy = false
    private var dbgJob: Job? = null
    private var dbgSeq = 0L

    /** 手表端可识别的手势；顺序即 App 设置页展示顺序。 */
    val gestureKeys = listOf("fist", "pinch", "cw", "ccw", "push", "pull")
    val gestureNames = mapOf(
        "fist" to "攥拳",
        "pinch" to "捏手指",
        "cw" to "顺时针转手",
        "ccw" to "逆时针转手",
        "push" to "向前推",
        "pull" to "向后拉"
    )

    /** 手势可绑定的车控动作；none 表示只识别不上报执行。 */
    val actionOptions = listOf(
        "none" to "不执行",
        "trunk" to "开后备箱",
        "trunkClose" to "关后备箱",
        "find" to "寻车",
        "ac" to "开空调",
        "window" to "开窗通风",
        "lock" to "闭锁",
        "unlock" to "解锁"
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val nonces = LinkedHashSet<String>()
    private var attached = false
    private var appContext: Context? = null
    private var messageApi: MessageApi? = null
    private var nodeId: String? = null
    private var cached: VehicleData? = null
    private var cachedAt = 0L

    fun attach(context: Context) {
        if (attached) return
        attached = true
        val ctx = context.applicationContext
        appContext = ctx
        messageApi = runCatching { Wearable.getMessageApi(ctx) }.getOrNull()
        if (messageApi == null) {
            setState("未安装小米穿戴服务")
            return
        }
        scope.launch {
            while (isActive) {
                runCatching { discover(ctx) }.onFailure { Log.w(TAG, "发现手表失败", it) }
                delay(15_000)
            }
        }
    }

    private suspend fun discover(ctx: Context) {
        val nodes = runCatching { Wearable.getNodeApi(ctx).getConnectedNodes().await(8_000) }.getOrNull()
        if (nodes == null) {
            setState("无法连接小米穿戴服务")
            return
        }
        val node = nodes.firstOrNull()
        if (node == null) {
            nodeId = null
            setState("等待手表连接")
            return
        }
        if (node.id == nodeId) return
        grantPermissions(ctx, node.id)
        register(node)
    }

    private fun grantPermissions(ctx: Context, id: String) {
        runCatching {
            val auth = Wearable.getAuthApi(ctx)
            val permissions = arrayOf(Permission.DEVICE_MANAGER, Permission.NOTIFY)
            auth.checkPermissions(id, permissions).addOnSuccessListener { granted ->
                if (granted.any { !it }) {
                    auth.requestPermission(id, *permissions)
                        .addOnSuccessListener { Log.i(TAG, "穿戴权限已授予") }
                        .addOnFailureListener { Log.w(TAG, "穿戴权限授予失败", it) }
                }
            }
        }.onFailure { Log.w(TAG, "检查穿戴权限失败", it) }
    }

    private fun register(node: Node) {
        val api = messageApi ?: return
        api.addListener(node.id, OnMessageReceivedListener { from, data ->
            val request = String(data, Charsets.UTF_8)
            val path = runCatching { JSONObject(request).optString("path") }.getOrDefault("")
            // 调试通道：手表回传加速度采样，收集起来给手机端面板展示
            if (path == "/dbg") {
                onDbg(request)
                return@OnMessageReceivedListener
            }
            // 手势通道：手表识别到动作后由手机按用户配置执行车控
            if (path == "/gesture") {
                onGesture(request)
                return@OnMessageReceivedListener
            }
            scope.launch {
                val reply = runCatching { handle(request) }.getOrElse { cause ->
                    val id = runCatching { JSONObject(request).optInt("id") }.getOrDefault(0)
                    JSONObject()
                        .put("id", id)
                        .put("http", 200)
                        .put("code", 1)
                        .put("message", cause.message ?: "桥处理失败")
                        .toString()
                }
                runCatching { api.sendMessage(from, reply.toByteArray(Charsets.UTF_8)) }
                    .onFailure { Log.w(TAG, "回包失败", it) }
            }
        }).addOnSuccessListener {
            nodeId = node.id
            setState("已连接" + (node.name?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""))
            Log.i(TAG, "手表节点已注册：${node.id}")
            pushGestureConfig()
        }.addOnFailureListener {
            setState("手表注册失败")
            Log.w(TAG, "手表节点注册失败", it)
        }
    }

    private fun setState(text: String) {
        state.value = text
        Log.i(TAG, "桥状态：$text")
    }

    /** 手机端入口：通知手表开始采集 ms 毫秒的加速度。返回空串表示已下发。 */
    fun startWatchCollect(ms: Long): String {
        val api = messageApi ?: return "小米穿戴服务不可用"
        val node = nodeId ?: return "手表还没连上"
        val payload = JSONObject().put("cmd", "dbg").put("on", true).put("ms", ms)
        val ok = runCatching {
            api.sendMessage(node, payload.toString().toByteArray(Charsets.UTF_8))
        }.isSuccess
        if (!ok) return "下发失败，请重开手表车控页"

        dbgJob?.cancel()
        dbgSeq += 1
        dbgLive.value = WatchBatch(id = dbgSeq, startedAt = System.currentTimeMillis(), plannedMs = ms)
        dbgJob = scope.launch {
            delay(ms + 2_000L)
            finishBatch()
        }
        Log.i(TAG, "开始采集 ${ms}ms")
        return ""
    }

    fun stopWatchCollect() {
        val api = messageApi
        val node = nodeId
        if (api != null && node != null) {
            runCatching {
                api.sendMessage(
                    node,
                    JSONObject().put("cmd", "dbg").put("on", false).toString().toByteArray(Charsets.UTF_8)
                )
            }
        }
        dbgJob?.cancel()
        finishBatch()
    }

    /** 用户在 App 里绑定了具体动作的手势；其余在手表端完全静默（不识别、不震动、不上报）。 */
    private fun enabledGestureList(): String {
        val ctx = appContext ?: return "fist"
        val config = AppConfig(ctx)
        return gestureKeys.filter { config.gestureAction(it) != "none" }.joinToString(",")
    }

    /** 主动把手势白名单推给手表（手表断线重连、App 内改绑定后都会调用）。 */
    fun pushGestureConfig() {
        val api = messageApi ?: return
        val node = nodeId ?: return
        val payload = JSONObject().put("cmd", "gcfg").put("list", enabledGestureList())
        runCatching { api.sendMessage(node, payload.toString().toByteArray(Charsets.UTF_8)) }
            .onFailure { Log.w(TAG, "下发手势配置失败", it) }
    }

    /** 手表识别到手势：记录一条日志，并按 App 内配置执行对应车控。 */
    private fun onGesture(raw: String) {
        val gesture = runCatching { JSONObject(raw).optJSONObject("body")?.optString("g") }
            .getOrNull().orEmpty()
        if (gesture.isBlank()) return
        val ctx = appContext ?: return
        val action = AppConfig(ctx).gestureAction(gesture)
        val name = gestureNames[gesture] ?: gesture
        val label = if (action == "none") "未绑定" else commandLabels[action] ?: action
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        gestureLog.value = (listOf("$stamp · $name → $label") + gestureLog.value).take(8)
        Log.i(TAG, "手势 $gesture($name) → $action")
        if (action == "none" || gestureBusy) return
        gestureBusy = true
        scope.launch {
            try {
                runCatching { performAction(action) }
                    .onFailure { Log.w(TAG, "手势执行失败", it) }
            } finally {
                gestureBusy = false
            }
        }
    }

    private fun onDbg(raw: String) {
        val body = runCatching { JSONObject(raw).optJSONObject("body") }.getOrNull() ?: return
        val current = dbgLive.value ?: return
        if (current.samples.size >= 20000) return
        val add = mutableListOf<WatchSample>()
        val packed = body.optJSONArray("b")
        if (packed != null) {
            var i = 0
            while (i + 5 < packed.length()) {
                add += WatchSample(
                    t = packed.optLong(i),
                    x = packed.optDouble(i + 1),
                    y = packed.optDouble(i + 2),
                    z = packed.optDouble(i + 3),
                    n = packed.optDouble(i + 4),
                    d = packed.optDouble(i + 5)
                )
                i += 6
            }
        } else {
            add += WatchSample(
                t = body.optLong("t"),
                x = body.optDouble("x"),
                y = body.optDouble("y"),
                z = body.optDouble("z"),
                n = body.optDouble("n"),
                d = body.optDouble("d")
            )
        }
        if (add.isEmpty()) return
        dbgLive.value = current.copy(samples = current.samples + add)
    }

    private fun finishBatch() {
        val batch = dbgLive.value ?: return
        dbgLive.value = null
        if (batch.samples.isEmpty()) return
        dbgHistory.value = (listOf(batch) + dbgHistory.value).take(6)
        saveCsv(batch)
    }

    private fun saveCsv(batch: WatchBatch) {
        val ctx = appContext ?: return
        runCatching {
            val dir = File(ctx.getExternalFilesDir(null), "dbg").apply { mkdirs() }
            val stamp = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date(batch.startedAt))
            val file = File(dir, "watch_$stamp.csv")
            file.writeText(
                buildString {
                    appendLine(
                        "# samples=${batch.count} hz=${"%.1f".format(batch.hz)} " +
                            "maxD=${"%.2f".format(batch.maxD)} maxDn=${"%.2f".format(batch.maxDn)} " +
                            "triggers=${batch.triggers.size}"
                    )
                    appendLine("t,x,y,z,n,d")
                    batch.samples.forEach { appendLine("${it.t},${it.x},${it.y},${it.z},${it.n},${it.d}") }
                }
            )
            Log.i(TAG, "采集已保存：${file.absolutePath}")
        }.onFailure { Log.w(TAG, "保存采集失败", it) }
    }

    private suspend fun handle(text: String): String {
        val request = JSONObject(text)
        val id = request.optInt("id")
        val token = request.optString("token")
        val path = request.optString("path")
        val method = request.optString("method")
        val body = runCatching { request.optJSONObject("body") }.getOrNull() ?: JSONObject()
        if (path != "/dbg") Log.i(TAG, "收到 $method $path")
        val (http, result) = dispatch(method, path, body, token)
        return JSONObject()
            .put("id", id)
            .put("http", http)
            .put("code", result.optInt("code"))
            .apply {
                result.opt("data")?.let { put("data", it) }
                result.optString("message").takeIf { it.isNotBlank() }?.let { put("message", it) }
            }
            .toString()
    }

    private suspend fun dispatch(
        method: String,
        path: String,
        body: JSONObject,
        token: String
    ): Pair<Int, JSONObject> {
        if (token != WATCH_TOKEN) return 403 to errorReply("token 不正确")
        return when {
            path == "/ping" && method == "GET" ->
                200 to ok(JSONObject().put("pong", true))

            path == "/status" && method == "GET" ->
                200 to statusReply()

            path == "/refresh" && method == "POST" ->
                200 to refreshReply()

            path == "/command" && method == "POST" ->
                200 to commandReply(body)

            path == "/gcfg" && method == "GET" ->
                200 to ok(JSONObject().put("list", enabledGestureList()))

            else -> 200 to errorReply("未知路由")
        }
    }

    private suspend fun statusReply(): JSONObject {
        val snapshot = cached
        if (snapshot != null && System.currentTimeMillis() - cachedAt < CACHE_TTL_MS) {
            return ok(statusJson(snapshot))
        }
        return refreshReply()
    }

    private suspend fun refreshReply(): JSONObject {
        val config = config() ?: return errorReply("桥未启动")
        if (!config.isConfigured) return errorReply("手机端还没填写车辆凭据")
        return runCatching { ok(statusJson(fetch(config))) }
            .getOrElse { errorReply(it.message ?: "读取车况失败") }
    }

    private suspend fun commandReply(body: JSONObject): JSONObject {
        val action = body.optString("action")
        if (commands[action] == null) return errorReply("不支持的动作：$action")

        val ts = body.optLong("ts")
        if (ts > 0 && kotlin.math.abs(System.currentTimeMillis() - ts) > MAX_CLOCK_SKEW_MS) {
            return errorReply("指令已过期")
        }

        val nonce = body.optString("nonce")
        if (nonce.isBlank()) return errorReply("nonce 重复或缺失")
        synchronized(nonces) {
            if (!nonces.add(nonce)) return errorReply("nonce 重复或缺失")
            if (nonces.size > NONCE_LIMIT) {
                val iterator = nonces.iterator()
                repeat(nonces.size - NONCE_LIMIT / 2) {
                    if (iterator.hasNext()) {
                        iterator.next()
                        iterator.remove()
                    }
                }
            }
        }

        return runCatching { performAction(action) }
            .getOrElse { errorReply(it.message ?: "车辆未响应") }
    }

    /** 执行一次车控：校验凭据与解锁开关，发指令后回读车况。 */
    private suspend fun performAction(action: String): JSONObject {
        val command = commands[action] ?: return errorReply("不支持的动作：$action")
        val config = config() ?: return errorReply("桥未启动")
        if (!config.isConfigured) return errorReply("手机端还没填写车辆凭据")
        if (action == "unlock" && !config.allowUnlock) {
            return errorReply("解锁未启用，请在 App 界面打开开关")
        }

        CheryApi().sendControl(
            token = config.token,
            vin = config.vin,
            secretKey = config.secretKey,
            feature = command.feature,
            fields = command.fields
        )
        val label = commandLabels[action] ?: action
        lastEvent.value = "$label " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        delay(command.settleMs)
        return ok(statusJson(fetch(config)))
    }

    /** 手表端可用动作；字段与首页按钮使用同一套已实测协议。 */
    private class Command(
        val feature: String,
        val fields: Map<String, Any>,
        val settleMs: Long = 4_000L
    )

    private val commands = mapOf(
        "lock" to Command("lockControl", mapOf("lockType" to 0)),
        "unlock" to Command("lockControl", mapOf("lockType" to 1)),
        "find" to Command("findCar", mapOf("state" to 3)),
        "trunk" to Command("tailgateControl", mapOf("controlType" to 1), settleMs = 6_000L),
        "trunkClose" to Command("tailgateControl", mapOf("controlType" to 0), settleMs = 6_000L),
        "window" to Command("windowControl", mapOf("controlType" to 2), settleMs = 6_000L),
        "ac" to Command("airControl", mapOf("airControlType" to 1, "airType" to 0))
    )

    private val commandLabels = mapOf(
        "lock" to "闭锁",
        "unlock" to "解锁",
        "find" to "寻车",
        "trunk" to "开后备箱",
        "trunkClose" to "关后备箱",
        "window" to "开窗通风",
        "ac" to "开空调"
    )

    private suspend fun fetch(config: AppConfig): VehicleData {
        val vehicle = VehicleRepository.fetch(config)
        cached = vehicle
        cachedAt = System.currentTimeMillis()
        config.lastDataJson = vehicle.rawJson
        return vehicle
    }

    private fun statusJson(vehicle: VehicleData): JSONObject = JSONObject()
        .put("soc", vehicle.batteryPercent?.toInt() ?: JSONObject.NULL)
        .put("rangeKm", vehicle.rangeKm ?: JSONObject.NULL)
        .put("locked", vehicle.locked ?: JSONObject.NULL)
        .put("charging", vehicle.charging == true)
        .put("updatedAt", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(cachedAt)))
        .put("stale", System.currentTimeMillis() - cachedAt > 10 * 60_000L)

    private fun config(): AppConfig? = appContext?.let { AppConfig(it) }

    private fun ok(data: JSONObject) = JSONObject().put("code", 0).put("data", data)

    private fun errorReply(message: String) = JSONObject().put("code", 1).put("message", message)

    private suspend fun <T> com.xiaomi.xms.wearable.tasks.Task<T>.await(timeoutMs: Long): T? {
        val deferred = CompletableDeferred<T?>()
        addOnSuccessListener { deferred.complete(it) }
        addOnFailureListener { deferred.complete(null) }
        return withTimeoutOrNull(timeoutMs) { deferred.await() }
    }
}

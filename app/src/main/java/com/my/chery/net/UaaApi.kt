package com.my.chery.net

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.my.chery.data.VehicleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * 奇瑞统一账号（UAA）登录，实测链路：
 * 1. POST uaa2c.chery.cn/api/v1/common/mobile/sms            发短信验证码
 * 2. POST uaa2c.chery.cn/api/v1/uaa/mobile/mobile-code-login 验证码登录，返回 accessToken
 * 3. GET  mobile-consumer-sapp.chery.cn/.../car-manage/list  车辆列表
 * 4. POST mobile-consumer-sapp.chery.cn/.../getTspSdkToken   换取 NTSP sdk_token
 * 车服务接口的 Authorization 必须是 "Bearer <accessToken>"。
 */
class UaaApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    /** 发送登录短信验证码。 */
    suspend fun sendLoginCode(mobile: String) = withContext(Dispatchers.IO) {
        postJson(SMS_URL, mapOf("mobile" to mobile, "template" to "login"))
    }

    /** 验证码登录，返回账号会话。 */
    suspend fun loginByCode(mobile: String, code: String): LoginSession =
        withContext(Dispatchers.IO) {
            val root = postJson(
                LOGIN_URL,
                mapOf("mobile" to mobile, "verificationCode" to code)
            )
            val data = root.get("data")?.takeIf { it.isJsonObject }?.asJsonObject
                ?: throw UaaException("登录成功但未返回账号凭证")
            val accessToken = data.text("accessToken", "access_token")
                ?: throw UaaException("登录成功但未返回 accessToken")
            LoginSession(
                accessToken = accessToken,
                refreshToken = data.text("refreshToken", "refresh_token").orEmpty()
            )
        }

    /** 当前账号下的车辆列表。 */
    suspend fun vehicleList(accessToken: String): List<VehicleItem> = withContext(Dispatchers.IO) {
        val root = getJson(CAR_LIST_PATH, accessToken)
        val items = mutableListOf<VehicleItem>()
        collectVehicles(root, items)
        if (items.isEmpty()) throw UaaException("账号下未找到车辆")
        // 默认车辆排最前
        items.sortedByDescending { it.isDefault }
    }

    /** 用账号 accessToken 换取 NTSP(TSP) token。 */
    suspend fun tspToken(accessToken: String): String = withContext(Dispatchers.IO) {
        val root = postJson(CAR_HOST + TSP_PATH, emptyMap(), accessToken)
        val data = root.get("data")?.takeIf { it.isJsonObject }?.asJsonObject
            ?: throw UaaException("未获取到车辆凭证")
        data.text("sdk_token", "tspSdkToken", "tspToken")
            ?: throw UaaException("未获取到车辆凭证")
    }

    private fun collectVehicles(element: JsonElement, output: MutableList<VehicleItem>) {
        when {
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val vin = obj.text("vin")
                if (!vin.isNullOrBlank() && vin.length == 17) {
                    output += VehicleItem(
                        vin = vin,
                        name = obj.text("standardName", "nickName", "vehicleName", "insideName")
                            ?: "奇瑞汽车",
                        plate = obj.text("licensenum", "plateNum", "plateNo", "licensePlate") ?: "",
                        isDefault = obj.get("isDefault")?.takeIf { it.isJsonPrimitive }?.asInt == 1
                    )
                }
                obj.entrySet().forEach { collectVehicles(it.value, output) }
            }
            element.isJsonArray -> element.asJsonArray.forEach { collectVehicles(it, output) }
        }
    }

    private fun getJson(path: String, accessToken: String): JsonObject {
        val request = Request.Builder()
            .url(CAR_HOST + path)
            .header("Authorization", "Bearer $accessToken")
            .header("User-Agent", "okhttp/4.9.0")
            .get()
            .build()
        return execute(request)
    }

    private fun postJson(
        url: String,
        body: Map<String, Any>,
        accessToken: String? = null
    ): JsonObject {
        val builder = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("User-Agent", "okhttp/4.9.0")
        if (!accessToken.isNullOrBlank()) builder.header("Authorization", "Bearer $accessToken")
        val request = builder
            .post(gson.toJson(body).toRequestBody("application/json;charset=UTF-8".toMediaType()))
            .build()
        return execute(request)
    }

    private fun execute(request: Request): JsonObject {
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            val json = runCatching { gson.fromJson(text, JsonObject::class.java) }.getOrNull()
            if (!response.isSuccessful) {
                throw UaaException(json?.text("error", "message") ?: "网络请求失败：HTTP ${response.code}")
            }
            if (json == null) throw UaaException("账号服务返回格式错误")
            val status = json.get("status")?.takeIf { it.isJsonPrimitive }?.asInt ?: 200
            if (status != 200) {
                throw UaaException(json.text("error", "message") ?: "请求失败（$status）")
            }
            return json
        }
    }

    private fun JsonObject.text(vararg keys: String): String? {
        for (key in keys) {
            val value = get(key) ?: continue
            if (value.isJsonPrimitive) {
                val text = value.asString
                if (text.isNotBlank()) return text
            }
        }
        return null
    }

    data class LoginSession(val accessToken: String, val refreshToken: String)

    companion object {
        private const val UAA_HOST = "https://uaa2c.chery.cn"
        private const val SMS_URL = "$UAA_HOST/api/v1/common/mobile/sms"
        private const val LOGIN_URL = "$UAA_HOST/api/v1/uaa/mobile/mobile-code-login"
        private const val CAR_HOST = "https://mobile-consumer-sapp.chery.cn"
        private const val CAR_LIST_PATH = "/web/chery/car-service/v2/car-manage/list"
        private const val TSP_PATH = "/web/chery/car-service/v2/car-appsdk/getTspSdkToken"
    }
}

class UaaException(message: String) : Exception(message)

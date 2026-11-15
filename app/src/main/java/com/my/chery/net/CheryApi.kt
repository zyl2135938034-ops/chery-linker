package com.my.chery.net

import android.util.Base64
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
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class CheryApiException(message: String) : Exception(message)

/**
 * 奇瑞 LionCloud 云端接口（新版「奇瑞汽车」App）。
 * 鉴权：Authorization 头直接放 NTSP JWT；
 * 车况接口额外需要 sign = UPPER(Base64(HMAC-SHA256(secretKey, 固定拼接串)))。
 */
class CheryApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    /** 实时车况（续航/电量/车锁/胎压/位置…），返回 body 节点。 */
    suspend fun condition(token: String, vin: String, secretKey: String): JsonObject =
        withContext(Dispatchers.IO) {
            val root = post(
                path = "/asd/v1/rt/data",
                token = token,
                body = mapOf("vin" to vin),
                signBody = true,
                secretKey = secretKey
            )
            root["body"]?.takeIf(JsonElement::isJsonObject)?.asJsonObject
                ?: throw CheryApiException("云端未返回车况数据")
        }

    /**
     * 下发车控指令（endpoint 前缀 /asc/v1/vc/）。
     * feature：lockControl / tailgateControl / windowControl / airControl / engineControl / findCar
     * 依据：安卓端实测 A00079 = 已下发，A00082 = 有指令正在下发。
     */
    suspend fun sendControl(
        token: String,
        vin: String,
        secretKey: String,
        feature: String,
        fields: Map<String, Any> = emptyMap()
    ): String = withContext(Dispatchers.IO) {
        val body = linkedMapOf<String, Any>(
            "clientType" to 1,
            "seq" to "$vin-${System.currentTimeMillis()}",
            "vin" to vin
        )
        body.putAll(fields)
        val root = post(
            path = "/asc/v1/vc/$feature",
            token = token,
            body = body,
            signBody = true,
            secretKey = secretKey,
            successCodes = setOf("A00079")
        )
        root.text("code") ?: "A00079"
    }

    suspend fun vehicleList(token: String, vin: String): List<VehicleItem> =
        withContext(Dispatchers.IO) {
            val root = post(
                path = "/asd/vehicleManage/v1/userVehicle/list",
                token = token,
                body = mapOf("vin" to vin, "vehicleTypeId" to "621", "channelCode" to "chery")
            )
            val items = mutableListOf<VehicleItem>()
            collectVehicles(root, items)
            items.distinctBy { it.vin }
        }

    private fun collectVehicles(element: JsonElement, output: MutableList<VehicleItem>) {
        when {
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val vin = obj.text("vin")
                if (!vin.isNullOrBlank()) {
                    output += VehicleItem(
                        vin = vin,
                        name = obj.text("nickName", "outsideStandardName", "insideName", "vehicleName") ?: "奇瑞汽车",
                        plate = obj.text("plateNum", "plateNo", "licensePlate") ?: ""
                    )
                }
                obj.entrySet().forEach { collectVehicles(it.value, output) }
            }
            element.isJsonArray -> element.asJsonArray.forEach { collectVehicles(it, output) }
        }
    }

    private fun post(
        path: String,
        token: String,
        body: Map<String, Any>,
        signBody: Boolean = false,
        secretKey: String? = null,
        successCodes: Set<String> = setOf("000000")
    ): JsonObject {
        val timestamp = System.currentTimeMillis().toString()
        val nonce = randomNonce()
        val builder = Request.Builder()
            .url(BASE_URL + path + "?terminal=3")
            .header("Authorization", token)
            .header("timestamp", timestamp)
            .header("nonce", nonce)
            .header("x-App-Id", "chery")
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("User-Agent", "okhttp/4.9.0")
        if (signBody && secretKey != null) {
            builder.header("sign", sign(secretKey, body, nonce, timestamp))
        }
        val httpRequest = builder
            .post(gson.toJson(body).toRequestBody("application/json;charset=UTF-8".toMediaType()))
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw CheryApiException("网络请求失败：HTTP ${response.code}")
            val json = try {
                gson.fromJson(text, JsonObject::class.java)
            } catch (e: Exception) {
                throw CheryApiException("云端返回格式错误")
            } ?: throw CheryApiException("云端返回为空")
            val code = json.text("code")
            if (code !in successCodes) {
                throw CheryApiException(json.text("msg") ?: "奇瑞云端请求失败（$code）")
            }
            return json
        }
    }

    /** sign = UPPER(Base64(HMAC-SHA256(secretKey, body 字母序 k=v & 固定后缀)))，值不做 URL 编码。 */
    private fun sign(secretKey: String, body: Map<String, Any>, nonce: String, timestamp: String): String {
        val payload = body.entries.sortedBy { it.key }.joinToString("&") { "${it.key}=${it.value}" } +
            "&secretKey=$secretKey&nonce=$nonce&timestamp=$timestamp&x-App-Id=chery"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return Base64.encodeToString(
            mac.doFinal(payload.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        ).uppercase()
    }

    private fun randomNonce(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        return buildString { repeat(32) { append(chars[random.nextInt(chars.length)]) } }
    }

    private fun JsonObject.text(vararg keys: String): String? {
        for (key in keys) {
            val value = get(key) ?: continue
            if (value.isJsonPrimitive) return value.asString
        }
        return null
    }

    companion object {
        private const val BASE_URL = "https://app-lioncloud.mychery.com"
    }
}

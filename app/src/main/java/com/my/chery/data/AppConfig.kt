package com.my.chery.data

import android.content.Context

class AppConfig(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("chery_config", Context.MODE_PRIVATE)

    /** NTSP JWT，Authorization 头直接使用。 */
    var token: String
        get() = prefs.getString("ntsp_token", "").orEmpty()
        set(value) = prefs.edit().putString("ntsp_token", value).apply()

    /** 车况接口签名密钥（奇瑞云端全局密钥，与账号无关）。 */
    var secretKey: String
        get() = prefs.getString("secret_key", DEFAULT_SECRET).orEmpty()
        set(value) = prefs.edit().putString("secret_key", value).apply()

    var vin: String
        get() = prefs.getString("vin", "").orEmpty()
        set(value) = prefs.edit().putString("vin", value).apply()

    /** 登录手机号（用于下次预填）。 */
    var phone: String
        get() = prefs.getString("phone", "").orEmpty()
        set(value) = prefs.edit().putString("phone", value).apply()

    /** 奇瑞统一账号 accessToken（用于续期 NTSP token）。 */
    var uaaToken: String
        get() = prefs.getString("uaa_token", "").orEmpty()
        set(value) = prefs.edit().putString("uaa_token", value).apply()

    /** 奇瑞统一账号 refreshToken。 */
    var refreshToken: String
        get() = prefs.getString("uaa_refresh_token", "").orEmpty()
        set(value) = prefs.edit().putString("uaa_refresh_token", value).apply()

    var carName: String
        get() = prefs.getString("car_name", "奇瑞汽车").orEmpty()
        set(value) = prefs.edit().putString("car_name", value).apply()

    var lastDataJson: String
        get() = prefs.getString("last_data", "").orEmpty()
        set(value) = prefs.edit().putString("last_data", value).apply()

    /** 是否允许手表端解锁（默认开启，手表侧仍保留二次确认）。 */
    var allowUnlock: Boolean
        get() = prefs.getBoolean("allow_unlock", true)
        set(value) = prefs.edit().putBoolean("allow_unlock", value).apply()

    /** 首页主视觉配色：0 星夜蓝紫，1 深海青，2 暮光紫。 */
    var heroStyle: Int
        get() = prefs.getInt("hero_style", 0)
        set(value) = prefs.edit().putInt("hero_style", value).apply()

    /** 手表手势对应的车控动作；none 表示不执行。 */
    fun gestureAction(gesture: String): String =
        prefs.getString("gesture_$gesture", defaultGestureAction(gesture)).orEmpty()

    fun setGestureAction(gesture: String, action: String) {
        prefs.edit().putString("gesture_$gesture", action).apply()
    }

    private fun defaultGestureAction(gesture: String): String =
        if (gesture == "fist") "trunk" else "none"

    /** 退出登录：清空凭据与缓存，桌面组件回到未配置状态。 */
    fun clear() {
        prefs.edit()
            .remove("ntsp_token")
            .remove("secret_key")
            .remove("vin")
            .remove("uaa_token")
            .remove("uaa_refresh_token")
            .putString("car_name", "奇瑞汽车")
            .putString("last_data", "")
            .apply()
    }

    val isConfigured: Boolean
        get() = token.isNotBlank() && secretKey.isNotBlank() && vin.isNotBlank()

    /** 是否已有可用登录态（账号登录或手动凭据都算）。 */
    val hasSession: Boolean
        get() = isConfigured

    companion object {
        private const val DEFAULT_SECRET = "de07173a4e4b35c3067fb5333e48e300"
    }
}

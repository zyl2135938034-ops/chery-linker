package com.my.chery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.my.chery.data.AppConfig
import com.my.chery.data.VehicleData
import com.my.chery.data.VehicleItem
import com.my.chery.data.VehicleParser
import com.my.chery.net.CheryApi
import com.my.chery.net.UaaApi
import com.my.chery.net.VehicleRepository
import com.my.chery.wear.WearBridgeService
import com.my.chery.worker.cancelWidgetUpdatesIfUnused
import com.my.chery.worker.scheduleWidgetUpdates
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val token: String = "",
    val secretKey: String = "",
    val vin: String = "",
    val vehicles: List<VehicleItem> = emptyList(),
    val vehicle: VehicleData? = null,
    val loading: Boolean = false,
    val message: String = "",
    val rawExpanded: Boolean = false,
    val heroStyle: Int = 0,
    val phone: String = "",
    val needsLogin: Boolean = false,
    val codeSending: Boolean = false,
    val codeCooldown: Int = 0,
    val loginLoading: Boolean = false,
    val authMessage: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val config = AppConfig(application)
    private var codeJob: Job? = null
    private val _uiState = MutableStateFlow(
        MainUiState(
            token = config.token,
            secretKey = config.secretKey,
            vin = config.vin,
            heroStyle = config.heroStyle,
            phone = config.phone,
            needsLogin = !config.hasSession
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        WearBridgeService.ensure(application)
        if (config.lastDataJson.isNotBlank()) {
            runCatching {
                val root = JsonParser.parseString(config.lastDataJson).asJsonObject
                _uiState.value = _uiState.value.copy(
                    vehicle = VehicleParser.parse(root, config.carName, config.vin)
                )
            }
        }
        // 上次登录已拿到账号 token 但未完成绑定（如车辆凭证换取失败），静默续上，避免重复发短信。
        if (config.uaaToken.isNotBlank() && !config.hasSession) resumeBind()
    }

    /** 用已保存的账号 token 重新完成车辆绑定，不需要再收验证码。 */
    private fun resumeBind() {
        viewModelScope.launch {
            update { copy(loginLoading = true, authMessage = "正在恢复上次登录…") }
            try {
                bindVehicles(UaaApi(), config.uaaToken)
            } catch (e: Exception) {
                update { copy(loginLoading = false, authMessage = friendly(e.message)) }
            }
        }
    }

    /** 换取 NTSP token、拉车辆列表并落地 VIN，成功后进入首页。 */
    private suspend fun bindVehicles(api: UaaApi, accessToken: String) {
        val vehicles = runCatching { api.vehicleList(accessToken) }.getOrDefault(emptyList())
        val target = vehicles.firstOrNull()
        update {
            copy(
                vehicles = vehicles,
                authMessage = if (target == null) "正在获取车辆凭证…" else "正在绑定 ${target.name}…"
            )
        }
        config.token = api.tspToken(accessToken)
        config.secretKey = config.secretKey.ifBlank { DEFAULT_SECRET_KEY }
        if (target != null) {
            config.vin = target.vin
            config.carName = target.name
        }
        update {
            copy(
                loginLoading = false,
                needsLogin = false,
                token = config.token,
                secretKey = config.secretKey,
                vin = target?.vin ?: vin,
                authMessage = ""
            )
        }
        if (target == null) {
            update { copy(message = "登录成功，但账号下未找到车辆，请手动填写 VIN") }
            return
        }
        loadVehicle()
        scheduleWidgetUpdates(getApplication())
        update { copy(message = "登录成功，车辆状态已更新") }
    }

    /** 把接口返回的英文错误码翻译成可操作的提示。 */
    private fun friendly(message: String?): String {
        if (message.isNullOrBlank()) return "登录失败，请重试"
        return when {
            message.contains("sms.captcha.only.three") ->
                "验证码发送过于频繁（1 小时最多 3 条），稍后再试；收到过的验证码仍可直接登录"
            message.contains("sms.captcha") || message.contains("captcha") ->
                "验证码错误或已过期，请重新获取"
            else -> message
        }
    }

    fun updateToken(value: String) = update { copy(token = value.trim()) }
    fun updateSecretKey(value: String) = update { copy(secretKey = value.trim()) }
    fun updateVin(value: String) = update { copy(vin = value.uppercase().filter { it.isLetterOrDigit() }.take(17)) }
    fun toggleRaw() = update { copy(rawExpanded = !rawExpanded) }

    fun setHeroStyle(value: Int) {
        config.heroStyle = value
        update { copy(heroStyle = value) }
    }

    fun logout() {
        codeJob?.cancel()
        config.clear()
        cancelWidgetUpdatesIfUnused(getApplication())
        _uiState.value = MainUiState(
            heroStyle = config.heroStyle,
            phone = config.phone,
            needsLogin = true,
            authMessage = "已退出登录"
        )
    }

    /** 发送登录短信验证码。 */
    fun sendLoginCode(phone: String) {
        val mobile = phone.filter { it.isDigit() }
        if (mobile.length != 11) {
            update { copy(authMessage = "请输入 11 位手机号") }
            return
        }
        if (_uiState.value.codeCooldown > 0 || _uiState.value.codeSending) return
        viewModelScope.launch {
            update { copy(codeSending = true, authMessage = "正在发送验证码…") }
            try {
                UaaApi().sendLoginCode(mobile)
                update { copy(codeSending = false, phone = mobile, authMessage = "验证码已发送") }
                codeJob?.cancel()
                codeJob = viewModelScope.launch {
                    for (left in 60 downTo 1) {
                        update { copy(codeCooldown = left) }
                        delay(1000)
                    }
                    update { copy(codeCooldown = 0) }
                }
            } catch (e: Exception) {
                update { copy(codeSending = false, authMessage = e.message ?: "验证码发送失败") }
            }
        }
    }

    /** 手机号 + 验证码登录，成功后自动换取 NTSP token 并读取车辆。 */
    fun loginByCode(phone: String, code: String) {
        val mobile = phone.filter { it.isDigit() }
        if (mobile.length != 11) {
            update { copy(authMessage = "请输入 11 位手机号") }
            return
        }
        if (code.length < 4) {
            update { copy(authMessage = "请输入短信验证码") }
            return
        }
        if (_uiState.value.loginLoading) return
        viewModelScope.launch {
            update { copy(loginLoading = true, authMessage = "正在登录…") }
            try {
                val api = UaaApi()
                val session = api.loginByCode(mobile, code.trim())
                config.uaaToken = session.accessToken
                config.refreshToken = session.refreshToken
                config.phone = mobile
                update { copy(phone = mobile) }
                bindVehicles(api, session.accessToken)
            } catch (e: Exception) {
                update { copy(loginLoading = false, authMessage = friendly(e.message)) }
            }
        }
    }

    /** 高级：不登录，直接进入手动填写 Token/VIN 页面。 */
    fun useManualConfig() {
        update { copy(needsLogin = false, authMessage = "") }
    }

    fun saveAndLoad() {
        val state = _uiState.value
        if (state.token.isBlank() || state.secretKey.isBlank()) {
            update { copy(message = "请填写 NTSP Token 与 secretKey") }
            return
        }
        if (state.vin.length != 17) {
            update { copy(message = "请填写 17 位 VIN") }
            return
        }
        viewModelScope.launch {
            update { copy(loading = true, message = "正在读取车辆状态…") }
            try {
                config.token = state.token
                config.secretKey = state.secretKey
                config.vin = state.vin
                val vehicles = runCatching { CheryApi().vehicleList(state.token, state.vin) }.getOrDefault(emptyList())
                vehicles.firstOrNull { it.vin == state.vin }?.let { config.carName = it.name }
                update { copy(vehicles = vehicles) }
                loadVehicle()
                scheduleWidgetUpdates(getApplication())
                update { copy(loading = false, message = "车辆状态已更新，可将 Chery Linker 添加到桌面") }
            } catch (e: Exception) {
                update { copy(loading = false, message = e.message ?: "读取车辆状态失败") }
            }
        }
    }

    fun selectVehicle(vehicle: VehicleItem) {
        if (_uiState.value.loading) return
        config.vin = vehicle.vin
        config.carName = vehicle.name
        update { copy(vin = vehicle.vin) }
        viewModelScope.launch {
            update { copy(loading = true, message = "正在切换车辆…") }
            try {
                loadVehicle()
                update { copy(loading = false, message = "已切换车辆") }
            } catch (e: Exception) {
                update { copy(loading = false, message = e.message ?: "获取车辆失败") }
            }
        }
    }

    /** 下发车控指令：feature 为 /asc/v1/vc/ 下的端点名，fields 为端点业务字段。 */
    fun control(label: String, feature: String, fields: Map<String, Any>) {
        if (!config.isConfigured) {
            update { copy(message = "请先完成配置并读取一次车辆状态") }
            return
        }
        val state = _uiState.value
        viewModelScope.launch {
            update { copy(loading = true, message = "正在下发「$label」…") }
            try {
                CheryApi().sendControl(
                    token = state.token.ifBlank { config.token },
                    vin = config.vin,
                    secretKey = state.secretKey.ifBlank { config.secretKey },
                    feature = feature,
                    fields = fields
                )
                delay(4000)
                loadVehicle()
                update { copy(loading = false, message = "「$label」已下发，状态已刷新") }
            } catch (e: Exception) {
                update { copy(loading = false, message = e.message ?: "「$label」下发失败") }
            }
        }
    }

    fun refresh() {
        if (!config.isConfigured) {
            update { copy(message = "请先填写 Token、secretKey 与 VIN") }
            return
        }
        viewModelScope.launch {
            update { copy(loading = true, message = "正在刷新车辆状态…") }
            try {
                loadVehicle()
                scheduleWidgetUpdates(getApplication())
                update { copy(loading = false, message = "车辆状态已更新") }
            } catch (e: Exception) {
                update { copy(loading = false, message = e.message ?: "刷新失败") }
            }
        }
    }

    private suspend fun loadVehicle() {
        val vehicle = VehicleRepository.fetch(config)
        config.lastDataJson = vehicle.rawJson
        config.carName = vehicle.carName
        update { copy(vehicle = vehicle) }
    }

    private inline fun update(block: MainUiState.() -> MainUiState) {
        _uiState.value = _uiState.value.block()
    }

    companion object {
        private const val DEFAULT_SECRET_KEY = "de07173a4e4b35c3067fb5333e48e300"
    }
}

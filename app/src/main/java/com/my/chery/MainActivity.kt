package com.my.chery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.my.chery.data.AppConfig
import com.my.chery.data.VehicleData
import com.my.chery.data.VehicleItem
import com.my.chery.ui.LoginScreen
import com.my.chery.ui.theme.CheryLinkerTheme
import com.my.chery.wear.WearBridge
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF070A0F)
private val CardBg = Color(0xFF11161E)
private val Line = Color(0xFF232C38)
private val Sub = Color(0xFF98A2B3)
private val Accent = Color(0xFF5AC8FA)
private val Violet = Color(0xFF8B7DFF)

private data class HeroStyle(val name: String, val top: Color, val bottom: Color, val glowA: Color, val glowB: Color)

private val heroStyles = listOf(
    HeroStyle("星夜蓝紫", Color(0xFF242A4A), Color(0xFF0A0D16), Color(0xFF7C6CFF), Color(0xFF2B6CFF)),
    HeroStyle("深海青", Color(0xFF10303B), Color(0xFF070D12), Color(0xFF37E6C4), Color(0xFF2C8CFF)),
    HeroStyle("暮光紫", Color(0xFF35203F), Color(0xFF0C0A12), Color(0xFFFF6FB5), Color(0xFF8B5CF6))
)

private enum class Panel { None, Config, Background, Logout }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            CheryLinkerTheme {
                val model: MainViewModel = viewModel()
                val state by model.uiState.collectAsState()
                if (state.needsLogin) {
                    LoginScreen(
                        state = state,
                        onSendCode = model::sendLoginCode,
                        onLogin = model::loginByCode,
                        onManual = model::useManualConfig
                    )
                } else {
                    MainScreen(
                        state = state,
                        onToken = model::updateToken,
                        onSecretKey = model::updateSecretKey,
                        onVin = model::updateVin,
                        onLogin = model::saveAndLoad,
                        onRefresh = model::refresh,
                        onVehicle = model::selectVehicle,
                        onToggleRaw = model::toggleRaw,
                        onHeroStyle = model::setHeroStyle,
                        onLogout = model::logout,
                        onControl = model::control
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    state: MainUiState,
    onToken: (String) -> Unit,
    onSecretKey: (String) -> Unit,
    onVin: (String) -> Unit,
    onLogin: () -> Unit,
    onRefresh: () -> Unit,
    onVehicle: (VehicleItem) -> Unit,
    onToggleRaw: () -> Unit,
    onHeroStyle: (Int) -> Unit,
    onLogout: () -> Unit,
    onControl: (String, String, Map<String, Any>) -> Unit
) {
    var panel by remember { mutableStateOf(Panel.None) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val unsupported: (String) -> Unit = { name ->
        scope.launch { snackbar.showSnackbar("「$name」云端暂无开放接口，需官方 App 操作") }
    }

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TopBar(
                    loading = state.loading,
                    onRefresh = onRefresh,
                    onSettings = { panel = Panel.Config }
                )
            }

            if (state.vehicle == null) {
                item {
                    ConnectBanner(
                        loading = state.loading,
                        message = state.message,
                        onSetup = { panel = Panel.Config },
                        onRefresh = onRefresh
                    )
                }
            }

            item { HeroCard(state.vehicle, state.heroStyle, onControl) }
            item { TyreRow(state.vehicle) }
            item { QuickActions(onControl, unsupported) }
            item { EnergyCard(state.vehicle) }
            item { DetailsCard(state.vehicle) }
            item {
                SecondaryActions(
                    onConfig = { panel = Panel.Config },
                    onBackground = { panel = Panel.Background },
                    onRefresh = onRefresh,
                    onLogout = { panel = Panel.Logout }
                )
            }
            item { WatchDebugCard() }
            item { GestureCard() }
            item {
                SectionCard(title = "添加到桌面", subtitle = null) {
                    Text(
                        "1. 成功读取一次车辆状态\n2. 长按手机桌面，选择「小组件」\n3. 添加「Chery Linker 车辆状态」或「Chery Linker 续航」\n\n组件每 30 分钟自动刷新，也可点击组件上的「刷新」。",
                        lineHeight = 22.sp,
                        fontSize = 13.sp,
                        color = Sub
                    )
                }
            }
            item {
                SectionCard(title = "接口调试", subtitle = "字段未识别时，可依据原始返回补充车型映射") {
                    TextButton(onClick = onToggleRaw, contentPadding = PaddingValues(0.dp)) {
                        Text(if (state.rawExpanded) "收起原始数据" else "查看原始数据", color = Accent, fontSize = 13.sp)
                    }
                    if (state.rawExpanded) {
                        SelectionContainer {
                            Text(
                                state.vehicle?.rawJson ?: "暂无数据",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 15.sp,
                                color = Sub
                            )
                        }
                    }
                }
            }
            if (state.message.isNotBlank() && state.vehicle != null) {
                item {
                    Text(state.message, color = Accent, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }

    when (panel) {
        Panel.Config -> ConfigPanel(
            state = state,
            onToken = onToken,
            onSecretKey = onSecretKey,
            onVin = onVin,
            onSave = onLogin,
            onVehicle = onVehicle,
            onDismiss = { panel = Panel.None }
        )
        Panel.Background -> BackgroundPanel(
            selected = state.heroStyle,
            onPick = onHeroStyle,
            onDismiss = { panel = Panel.None }
        )
        Panel.Logout -> LogoutPanel(
            onConfirm = {
                panel = Panel.None
                onLogout()
            },
            onDismiss = { panel = Panel.None }
        )
        Panel.None -> Unit
    }
}

@Composable
private fun TopBar(loading: Boolean, onRefresh: () -> Unit, onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("CHERY LINKER", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text("爱车状态", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
        }
        RoundIcon(onRefresh, "刷新") {
            if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Accent)
            else Icon(Icons.Filled.Refresh, "刷新", tint = Color(0xFFE6ECF3), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        RoundIcon(onSettings, "设置") {
            Icon(Icons.Filled.Settings, "设置", tint = Color(0xFFE6ECF3), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun RoundIcon(onClick: () -> Unit, description: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun ConnectBanner(loading: Boolean, message: String, onSetup: () -> Unit, onRefresh: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text("尚未连接车辆", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                message.ifBlank { "填写官方 App 的 NTSP Token、secretKey 与 VIN，即可读取实时车况。" },
                fontSize = 12.sp,
                color = Sub,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(Modifier.padding(top = 14.dp)) {
                Button(
                    onClick = onSetup,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9F1FA), contentColor = Color(0xFF07121B)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("填写凭据", fontSize = 13.sp) }
                Spacer(Modifier.width(10.dp))
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !loading,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line)
                ) { Text("立即刷新", fontSize = 13.sp, color = Color(0xFFE6ECF3)) }
            }
        }
    }
}

@Composable
private fun HeroCard(vehicle: VehicleData?, style: Int, onControl: (String, String, Map<String, Any>) -> Unit) {
    val theme = heroStyles[style.coerceIn(0, heroStyles.lastIndex)]
    val locked = vehicle?.locked?.let { if (it) "已闭锁" else "未闭锁" } ?: "--"
    val interior = vehicle?.interiorTemperature?.let { "${format(it)}°C" } ?: "--°C"
    val range = vehicle?.rangeKm
        ?: listOfNotNull(vehicle?.fuelRangeKm, vehicle?.electricRangeKm).takeIf { it.isNotEmpty() }?.sum()
    val odometer = vehicle?.odometerKm

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(theme.top, theme.bottom)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(theme.glowA.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(size.width * 0.82f, size.height * 0.14f),
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width * 0.82f, size.height * 0.14f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(theme.glowB.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(size.width * 0.06f, size.height * 0.72f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.06f, size.height * 0.72f)
            )
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.32f))))
        }

        Column(Modifier.fillMaxSize().padding(22.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(
                        vehicle?.carName ?: "我的奇瑞",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 7.dp)) {
                        Text("车锁 $locked", fontSize = 12.sp, color = Sub)
                        Text("  ·  ", fontSize = 12.sp, color = Sub)
                        Text("车内 $interior", fontSize = 12.sp, color = Sub)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            range?.toString() ?: "--",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White,
                            lineHeight = 34.sp
                        )
                        Text(" km", fontSize = 12.sp, color = Sub, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text(
                        "总里程 ${odometer?.let { "${format(it)} km" } ?: "--"}",
                        fontSize = 11.sp,
                        color = Sub,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SocGauge(vehicle?.batteryPercent, Modifier.size(158.dp))
            }
            Spacer(Modifier.weight(1f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CircleAction(painterResource(R.drawable.ic_lock_open), "解锁") {
                    onControl("解锁", "lockControl", mapOf("lockType" to 1))
                }
                CircleAction(painterResource(R.drawable.ic_lock), "上锁") {
                    onControl("上锁", "lockControl", mapOf("lockType" to 0))
                }
                CircleAction(painterResource(R.drawable.ic_trunk), "开后备箱") {
                    onControl("开后备箱", "tailgateControl", mapOf("controlType" to 1))
                }
                CircleAction(painterResource(R.drawable.ic_trunk), "关后备箱") {
                    onControl("关后备箱", "tailgateControl", mapOf("controlType" to 2))
                }
            }
        }
    }
}

@Composable
private fun SocGauge(percent: Double?, modifier: Modifier = Modifier) {
    val value = normalizePercent(percent)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val inset = strokeWidth / 2 + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            val fraction = ((value ?: 0.0) / 100.0).toFloat()
            if (fraction > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to Accent,
                        0.5f to Violet,
                        1f to Accent,
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = 270f * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value?.let { "${it.toInt()}%" } ?: "--",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text("动力电池", fontSize = 11.sp, color = Sub, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun CircleAction(painter: Painter, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter, label, tint = Color(0xFFE6ECF3), modifier = Modifier.size(22.dp))
        }
        Text(label, fontSize = 10.sp, color = Sub, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun TyreRow(vehicle: VehicleData?) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 15.dp)) {
            Tyre("左前", vehicle?.frontLeftTyre)
            Tyre("右前", vehicle?.frontRightTyre)
            Tyre("左后", vehicle?.rearLeftTyre)
            Tyre("右后", vehicle?.rearRightTyre)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Tyre(label: String, value: Double?) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Sub)
        Text(
            value?.let { "${format(it)} kPa" } ?: "--",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
private fun QuickActions(onControl: (String, String, Map<String, Any>) -> Unit, onTip: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(painterResource(R.drawable.ic_snowflake), "空调", Modifier.weight(1f), 24.dp, 92.dp) {
                onControl("空调", "airControl", mapOf("airControlType" to 1, "airType" to 0))
            }
            ActionTile(painterResource(R.drawable.ic_horn), "鸣笛寻车", Modifier.weight(1f), 24.dp, 92.dp) {
                onControl("鸣笛寻车", "findCar", mapOf("state" to 3))
            }
            ActionTile(painterResource(R.drawable.ic_window), "车窗通风", Modifier.weight(1f), 24.dp, 92.dp) {
                onControl("车窗通风", "windowControl", mapOf("controlType" to 2))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(painterResource(R.drawable.ic_bolt), "充电", Modifier.weight(1f), 30.dp, 108.dp) { onTip("充电") }
            ActionTile(painterResource(R.drawable.ic_car), "一键备车", Modifier.weight(1f), 30.dp, 108.dp) { onTip("一键备车") }
        }
    }
}

@Composable
private fun ActionTile(
    painter: Painter,
    label: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    height: Dp = 92.dp,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.height(height)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(painter, label, tint = Color(0xFFE6ECF3), modifier = Modifier.size(iconSize))
            Text(label, fontSize = 12.sp, color = Color(0xFFDDE5EE), modifier = Modifier.padding(top = 9.dp))
        }
    }
}

@Composable
private fun EnergyCard(vehicle: VehicleData?) {
    val battery = normalizePercent(vehicle?.batteryPercent)
    val fuel = normalizePercent(vehicle?.fuelPercent)
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0E141C)), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("能源状态", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                vehicle?.charging?.let {
                    Text(
                        if (it) "正在充电" else "未在充电",
                        fontSize = 11.sp,
                        color = if (it) Color(0xFF66E3B4) else Sub,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (battery != null) {
                EnergyLine("动力电池", battery, vehicle?.electricRangeKm, Accent)
            }
            if (fuel != null) {
                if (battery != null) Spacer(Modifier.height(16.dp))
                EnergyLine("燃油", fuel, vehicle?.fuelRangeKm, Color(0xFFF2C66D))
            }
            if (battery == null && fuel == null) {
                Text("该车型暂未返回油量或电量字段", color = Sub, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EnergyLine(label: String, percent: Double, range: Int?, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(
            buildString {
                if (range != null) append("$range km  /  ")
                append("${percent.toInt()}%")
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(Modifier.height(9.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.07f))
    ) {
        Box(
            Modifier
                .fillMaxWidth((percent / 100.0).toFloat().coerceIn(0.01f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.55f))))
        )
    }
}

@Composable
private fun DetailsCard(vehicle: VehicleData?) {
    val metrics = listOf(
        "行驶里程" to vehicle?.odometerKm?.let { "${format(it)} km" },
        "平均油耗" to vehicle?.fuelConsumption?.let { "${format(it)} L/100km" },
        "当前车速" to vehicle?.speed?.let { "${format(it)} km/h" },
        "车锁" to vehicle?.locked?.let { if (it) "已闭锁" else "未闭锁" },
        "车门" to vehicle?.anyDoorOpen?.let { if (it) "有车门开启" else "全部关闭" },
        "引擎" to vehicle?.engineOn?.let { if (it) "已启动" else "已熄火" },
        "在线状态" to vehicle?.online?.let { if (it) "在线" else "离线" },
        "车内温度" to vehicle?.interiorTemperature?.let { "${format(it)} °C" }
    )
    SectionCard(title = "车辆详情", subtitle = vehicle?.updateTime?.let { "车况时间 $it" }) {
        metrics.chunked(2).forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 2.dp))
            Row(Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
                row.forEach { (label, value) ->
                    Column(Modifier.weight(1f)) {
                        Text(label, fontSize = 11.sp, color = Sub)
                        Text(
                            value ?: "--",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (vehicle?.latitude != null && vehicle.longitude != null) {
            HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 4.dp))
            Text("车辆位置", fontSize = 11.sp, color = Sub, modifier = Modifier.padding(top = 8.dp))
            Text("${vehicle.latitude}, ${vehicle.longitude}", fontSize = 14.sp, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun WatchDebugCard() {
    val bridge by WearBridge.state.collectAsState()
    val live by WearBridge.dbgLive.collectAsState()
    val history by WearBridge.dbgHistory.collectAsState()
    val lastEvent by WearBridge.lastEvent.collectAsState()
    var tip by remember { mutableStateOf("") }
    val time = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    SectionCard(
        title = "手表动作采集",
        subtitle = "手表停在车控首页 · 点按钮后按提示动手腕，采集结束后自动保存"
    ) {
        Text("蓝牙桥：$bridge", fontSize = 12.sp, color = Sub)
        if (lastEvent.isNotBlank()) {
            Text(
                "手表最近触发：$lastEvent",
                fontSize = 12.sp,
                color = Color(0xFF7BE0A0),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            OutlineAction("采集 10 秒", Modifier.weight(1f)) {
                tip = WearBridge.startWatchCollect(10_000).ifBlank { "已下发 · 开始采集" }
            }
            OutlineAction("采集 20 秒", Modifier.weight(1f)) {
                tip = WearBridge.startWatchCollect(20_000).ifBlank { "已下发 · 开始采集" }
            }
        }
        if (live != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlineAction("提前停止", Modifier.weight(1f), danger = true) {
                    WearBridge.stopWatchCollect()
                    tip = "已停止"
                }
            }
        }
        val current = live
        if (current != null) {
            Text(
                "采集中 · 已收到 ${current.count} 个样本",
                fontSize = 13.sp,
                color = Accent,
                modifier = Modifier.padding(top = 10.dp)
            )
        } else if (tip.isNotBlank()) {
            Text(tip, fontSize = 12.sp, color = Accent, modifier = Modifier.padding(top = 10.dp))
        }
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            history.forEach { batch ->
                Text(
                    "${time.format(Date(batch.startedAt))} · ${batch.count} 点 · " +
                        "${"%.1f".format(batch.hz)}Hz · 峰值差 ${"%.1f".format(batch.maxD)} · " +
                        "模长差 ${"%.1f".format(batch.maxDn)} · 判定触发 ${batch.triggers.size} 次",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = if (batch.triggers.isNotEmpty()) Color(0xFF7BE0A0) else Sub,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun GestureCard() {
    val ctx = LocalContext.current
    val log by WearBridge.gestureLog.collectAsState()
    var rev by remember { mutableStateOf(0) }
    val config = remember(rev) { AppConfig(ctx) }

    SectionCard(
        title = "手势控制",
        subtitle = "手表识别动作，手机按下面的绑定执行 · 点一下即时生效"
    ) {
        WearBridge.gestureKeys.forEach { gesture ->
            val current = config.gestureAction(gesture)
            Column(Modifier.padding(top = 10.dp)) {
                Text(
                    WearBridge.gestureNames[gesture] ?: gesture,
                    fontSize = 13.sp,
                    color = Color(0xFFDDE5EE)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    WearBridge.actionOptions.forEach { (key, label) ->
                        GestureChip(label = label, selected = current == key) {
                            config.setGestureAction(gesture, key)
                            WearBridge.pushGestureConfig()
                            rev++
                        }
                    }
                }
            }
        }
        Text(
            "攥拳默认开后备箱；其余手势默认「不执行」，校准后再绑定。",
            fontSize = 11.sp,
            color = Sub,
            modifier = Modifier.padding(top = 10.dp)
        )
        if (log.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            log.forEach {
                Text(it, fontSize = 11.sp, color = Accent, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

@Composable
private fun GestureChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Accent.copy(alpha = 0.16f) else Color.Transparent)
            .border(1.dp, if (selected) Accent else Line, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp, color = if (selected) Accent else Sub)
    }
}

@Composable
private fun SecondaryActions(onConfig: () -> Unit, onBackground: () -> Unit, onRefresh: () -> Unit, onLogout: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlineAction("凭据与车辆", Modifier.weight(1f), onClick = onConfig)
            OutlineAction("背景图设置", Modifier.weight(1f), onClick = onBackground)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlineAction("立即刷新", Modifier.weight(1f), onClick = onRefresh)
            OutlineAction("退出登录", Modifier.weight(1f), danger = true, onClick = onLogout)
        }
    }
}

@Composable
private fun OutlineAction(label: String, modifier: Modifier = Modifier, danger: Boolean = false, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (danger) Color(0xFF5A2E33) else Line),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
    ) {
        Text(label, fontSize = 13.sp, color = if (danger) Color(0xFFFF9A94) else Color(0xFFDDE5EE))
    }
}

@Composable
private fun SectionCard(title: String, subtitle: String?, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, fontSize = 11.sp, color = Sub, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ConfigPanel(
    state: MainUiState,
    onToken: (String) -> Unit,
    onSecretKey: (String) -> Unit,
    onVin: (String) -> Unit,
    onSave: () -> Unit,
    onVehicle: (VehicleItem) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = CardBg, shape = RoundedCornerShape(24.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text("凭据与车辆", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("凭据仅保存在本机，直接连接奇瑞 LionCloud 云端", fontSize = 11.sp, color = Sub, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = state.token,
                    onValueChange = onToken,
                    label = { Text("NTSP Token") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.secretKey,
                    onValueChange = onSecretKey,
                    label = { Text("secretKey") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.vin,
                    onValueChange = onVin,
                    label = { Text("车辆 VIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onSave,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9F1FA), contentColor = Color(0xFF07121B))
                ) {
                    Text(if (state.vehicle == null) "保存并获取车辆" else "保存并刷新", fontSize = 14.sp)
                }
                if (state.vehicles.isNotEmpty()) {
                    HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 16.dp))
                    Text("我的车辆", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    state.vehicles.forEach { vehicle ->
                        val selected = vehicle.vin == state.vin
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Color(0xFF16222C) else Color.Transparent)
                                .clickable { onVehicle(vehicle) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(vehicle.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(vehicle.vin, fontSize = 10.sp, color = Sub, modifier = Modifier.padding(top = 2.dp))
                            }
                            if (selected) Icon(Icons.Filled.Check, null, tint = Accent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (state.message.isNotBlank()) {
                    Text(state.message, fontSize = 11.sp, color = Accent, modifier = Modifier.padding(top = 12.dp))
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("关闭", color = Sub, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun BackgroundPanel(selected: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = CardBg, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text("背景风格", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("首页主视觉配色", fontSize = 11.sp, color = Sub, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(14.dp))
                heroStyles.forEachIndexed { index, style ->
                    val isSelected = index == selected
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Accent else Line,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onPick(index) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(width = 52.dp, height = 32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Brush.horizontalGradient(listOf(style.top, style.glowA.copy(alpha = 0.75f), style.bottom)))
                        )
                        Text(style.name, fontSize = 14.sp, modifier = Modifier.weight(1f).padding(start = 14.dp))
                        if (isSelected) Icon(Icons.Filled.Check, null, tint = Accent, modifier = Modifier.size(18.dp))
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("完成", color = Accent, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun LogoutPanel(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = CardBg, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp)) {
                Text("退出登录？", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                Text(
                    "将清空本机保存的 Token、secretKey 与车况缓存，桌面组件会回到未配置状态。",
                    fontSize = 12.sp,
                    color = Sub,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消", color = Sub, fontSize = 13.sp) }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1E22), contentColor = Color(0xFFFF9A94))
                    ) { Text("退出登录", fontSize = 13.sp) }
                }
            }
        }
    }
}

private fun normalizePercent(value: Double?): Double? = value?.let {
    val normalized = when {
        it in 0.0..1.0 -> it * 100
        it > 100.0 && it <= 1000.0 -> it / 10
        else -> it
    }
    normalized.coerceIn(0.0, 100.0)
}

private fun format(value: Double) = if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.getDefault(), "%.1f", value)

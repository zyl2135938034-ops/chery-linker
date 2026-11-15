package com.my.chery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.chery.MainUiState

private val LoginBg = Color(0xFF070A0F)
private val LoginCard = Color(0xFF11161E)
private val LoginSub = Color(0xFF98A2B3)
private val LoginAccent = Color(0xFF5AC8FA)
private val LoginViolet = Color(0xFF8B7DFF)

@Composable
fun LoginScreen(
    state: MainUiState,
    onSendCode: (String) -> Unit,
    onLogin: (String, String) -> Unit,
    onManual: () -> Unit
) {
    var phone by remember(state.phone) { mutableStateOf(state.phone) }
    var code by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF131A2A), LoginBg))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Chery Linker", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(
                "使用奇瑞账号登录，自动同步你的车辆",
                fontSize = 13.sp,
                color = LoginSub,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LoginCard, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        phone = input.filter { it.isDigit() }.take(11)
                    },
                    label = { Text("手机号") },
                    placeholder = { Text("请输入 11 位手机号") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                        label = { Text("验证码") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(10.dp))
                    TextButton(
                        onClick = { onSendCode(phone) },
                        enabled = state.codeCooldown == 0 && !state.codeSending && phone.length == 11,
                        modifier = Modifier.width(112.dp)
                    ) {
                        Text(
                            when {
                                state.codeSending -> "发送中…"
                                state.codeCooldown > 0 -> "${state.codeCooldown}s"
                                else -> "获取验证码"
                            },
                            color = if (state.codeCooldown == 0) LoginAccent else LoginSub,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { onLogin(phone, code) },
                    enabled = !state.loginLoading && phone.length == 11 && code.length >= 4,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoginViolet,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (state.loginLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (state.loginLoading) "登录中…" else "登录", fontSize = 15.sp)
                }

                if (state.authMessage.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(state.authMessage, color = LoginAccent, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "登录即表示同意奇瑞账号相关协议；账号信息仅保存在本机",
                color = LoginSub,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onManual) {
                Text("高级：手动填写 Token / VIN", color = LoginSub, fontSize = 12.sp)
            }
        }
    }
}

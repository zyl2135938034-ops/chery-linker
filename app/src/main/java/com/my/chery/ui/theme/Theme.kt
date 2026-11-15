package com.my.chery.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5AC8FA),
    onPrimary = Color(0xFF03121B),
    primaryContainer = Color(0xFF12313F),
    onPrimaryContainer = Color(0xFFD3F1FF),
    secondary = Color(0xFF8B7DFF),
    onSecondary = Color(0xFF0B0716),
    secondaryContainer = Color(0xFF241E42),
    onSecondaryContainer = Color(0xFFE3DDFF),
    tertiary = Color(0xFF66E3B4),
    background = Color(0xFF070A0F),
    onBackground = Color(0xFFF2F5F8),
    surface = Color(0xFF0B0F15),
    onSurface = Color(0xFFF2F5F8),
    surfaceVariant = Color(0xFF141A23),
    onSurfaceVariant = Color(0xFF98A2B3),
    outline = Color(0xFF2A3341),
    outlineVariant = Color(0xFF1C242F),
    error = Color(0xFFFF8A80)
)

@Composable
fun CheryLinkerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}

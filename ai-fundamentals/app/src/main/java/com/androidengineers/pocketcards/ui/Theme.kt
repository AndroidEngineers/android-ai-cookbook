package com.androidengineers.pocketcards.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Violet = Color(0xFF6740C4)
val Lavender = Color(0xFFEDE5FF)
val Ink = Color(0xFF24183D)
private val Light = lightColorScheme(primary = Violet, onPrimary = Color.White, primaryContainer = Lavender, onPrimaryContainer = Ink,
    background = Color(0xFFFAF8F4), onBackground = Ink, surface = Color(0xFFFFFDFC), onSurface = Ink,
    surfaceVariant = Color(0xFFF0EBE7), onSurfaceVariant = Color(0xFF69616F), outline = Color(0xFF817788), secondaryContainer = Color(0xFFDCEFE7), onSecondaryContainer = Color(0xFF1A5140))
private val Dark = darkColorScheme(primary = Color(0xFFCBB6FF), onPrimary = Color(0xFF34116E), primaryContainer = Color(0xFF3C285D), onPrimaryContainer = Color(0xFFEDE5FF), background = Color(0xFF16121D), surface = Color(0xFF211B2A), onSurface = Color(0xFFF4EDF9), onBackground = Color(0xFFF4EDF9), surfaceVariant = Color(0xFF322B3D), onSurfaceVariant = Color(0xFFC9BFD2))
@Composable fun PocketTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, typography = Typography(
        displaySmall = TextStyle(fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 42.sp),
        headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 32.sp, lineHeight = 38.sp),
        headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 33.sp, fontWeight = FontWeight.SemiBold),
        titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 26.sp),
        labelLarge = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
    ), content = content)
}

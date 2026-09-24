package com.androidengineers.pocketstories.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import com.androidengineers.pocketstories.R

val Paper = Color(0xFFF7F2E9)
val Ink = Color(0xFF33263B)
val Gold = Color(0xFFE9C580)
val Moss = Color(0xFF38574B)
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val StorySerif =
    FontFamily(
        Font(
            R.font.fraunces,
            variationSettings =
                FontVariation.Settings(
                    FontVariation.weight(500),
                    FontVariation.opticalSizing(32.sp),
                ),
        )
    )
val StorySans = FontFamily(Font(R.font.dm_sans))

@Composable
fun StoriesTheme(content: @Composable () -> Unit) {
    val colors =
        lightColorScheme(
            primary = Ink,
            onPrimary = Paper,
            background = Paper,
            surface = Paper,
            onSurface = Ink,
            surfaceVariant = Color(0xFFECE5DA),
            onSurfaceVariant = Color(0xFF6D626B),
            secondary = Moss,
        )
    MaterialTheme(
        colorScheme = colors,
        typography =
            Typography(
                displayLarge =
                    TextStyle(fontFamily = StorySerif, fontSize = 48.sp, lineHeight = 52.sp),
                headlineLarge =
                    TextStyle(fontFamily = StorySerif, fontSize = 36.sp, lineHeight = 40.sp),
                headlineMedium =
                    TextStyle(fontFamily = StorySerif, fontSize = 28.sp, lineHeight = 34.sp),
                titleLarge =
                    TextStyle(fontFamily = StorySerif, fontSize = 23.sp, lineHeight = 29.sp),
                titleMedium =
                    TextStyle(
                        fontFamily = StorySans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                    ),
                bodyLarge = TextStyle(fontFamily = StorySans, fontSize = 16.sp, lineHeight = 25.sp),
                bodyMedium =
                    TextStyle(fontFamily = StorySans, fontSize = 14.sp, lineHeight = 21.sp),
                labelLarge =
                    TextStyle(
                        fontFamily = StorySans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                labelSmall =
                    TextStyle(fontFamily = StorySans, fontSize = 11.sp, letterSpacing = 1.5.sp),
            ),
        content = content,
    )
}

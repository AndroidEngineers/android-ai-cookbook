package com.androidengineers.pocketcook.ui
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val Light = lightColorScheme(primary=Color(0xFFA73E2A), onPrimary=Color.White, primaryContainer=Color(0xFFFFE4D6), onPrimaryContainer=Color(0xFF572415), background=Color(0xFFFFF8F2), onBackground=Color(0xFF2D1913), surface=Color(0xFFFFF8F2), onSurface=Color(0xFF2D1913), surfaceVariant=Color(0xFFF3E7DE), onSurfaceVariant=Color(0xFF6F5348), outline=Color(0xFF896F64), outlineVariant=Color(0xFFD6BEB0))
private val Dark = darkColorScheme(primary=Color(0xFFFFB59B), onPrimary=Color(0xFF542313), primaryContainer=Color(0xFF563026), onPrimaryContainer=Color(0xFFFFE4D6), background=Color(0xFF1D1411), onBackground=Color(0xFFFFF1E8), surface=Color(0xFF1D1411), onSurface=Color(0xFFFFF1E8), surfaceVariant=Color(0xFF30211B), onSurfaceVariant=Color(0xFFD5BDB1), outline=Color(0xFFA18B80), outlineVariant=Color(0xFF70564A))
@Composable fun PocketCookTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) { MaterialTheme(colorScheme=if(dark) Dark else Light, content=content) }

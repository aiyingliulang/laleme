package com.example.lifeassets.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppBackground = Color(0xFFF3F5F7)
val AppSurface = Color(0xFFFFFEFB)
val AppNavy = Color(0xFF0B2A3A)
val AppOrange = Color(0xFFEF6B1A)
val AppGreen = Color(0xFF36B37E)
val AppGray = Color(0xFF7B848A)
val AppLine = Color(0xFFE5E7E5)
val AppMuted = Color(0xFFF0F1F0)
val AppBlue = Color(0xFF49698D)

private val LightColors = lightColorScheme(
    primary = AppOrange,
    onPrimary = Color.White,
    secondary = AppNavy,
    onSecondary = Color.White,
    background = AppBackground,
    onBackground = AppNavy,
    surface = AppSurface,
    onSurface = AppNavy,
    surfaceVariant = AppMuted,
    onSurfaceVariant = AppGray,
    outline = AppLine
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF955F),
    secondary = Color(0xFF9CC7D9),
    background = Color(0xFF0C1A21),
    surface = Color(0xFF14262F),
    onSurface = Color(0xFFEFF6F8),
    surfaceVariant = Color(0xFF263C44),
    onSurfaceVariant = Color(0xFFB5C6CB)
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp),
        bodyLarge = bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodyMedium = bodyMedium.copy(fontSize = 12.sp, lineHeight = 18.sp),
        bodySmall = bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
        labelMedium = labelMedium.copy(fontSize = 11.sp, lineHeight = 16.sp)
    )
}

@Composable
fun LifeAssetsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}

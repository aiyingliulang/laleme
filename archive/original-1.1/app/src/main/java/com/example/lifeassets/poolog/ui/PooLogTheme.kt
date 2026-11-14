package com.example.lifeassets.poolog.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Bathroom tile rather than a candy-coloured health dashboard: clean, calm, and a little playful.
val PooCanvas = Color(0xFFF7F7F3)
val PooPaper = Color(0xFFFFFEFA)
val PooInk = Color(0xFF163437)
val PooCocoa = Color(0xFF744126)
val PooApricot = Color(0xFFF0A971)
val PooBlush = Color(0xFFF6E3D2)
val PooSage = Color(0xFF1F6A70)
val PooMuted = Color(0xFF607174)
val PooLine = Color(0xFFD9E4E1)
val PooSoft = Color(0xFFECF2F0)

private val LightColors = lightColorScheme(
    primary = PooCocoa,
    onPrimary = Color.White,
    secondary = PooSage,
    onSecondary = Color.White,
    background = PooCanvas,
    onBackground = PooInk,
    surface = PooPaper,
    onSurface = PooInk,
    surfaceVariant = PooSoft,
    onSurfaceVariant = PooMuted,
    outline = PooLine
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF0B78B),
    onPrimary = Color(0xFF40230F),
    secondary = Color(0xFF89C5C3),
    onSecondary = Color(0xFF0E3A3D),
    background = Color(0xFF14282A),
    onBackground = Color(0xFFE9F3EF),
    surface = Color(0xFF1D3537),
    onSurface = Color(0xFFE9F3EF),
    surfaceVariant = Color(0xFF2A4546),
    onSurfaceVariant = Color(0xFFC5D3D1),
    outline = Color(0xFF577071)
)

private val PooTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 48.sp,
            lineHeight = 54.sp,
            letterSpacing = (-1).sp
        ),
        displaySmall = displaySmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp,
            lineHeight = 30.sp
        ),
        titleLarge = titleLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 25.sp
        ),
        titleMedium = titleMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        bodyLarge = bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
        labelMedium = labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    )
}

@Composable
fun PooLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = PooTypography,
        content = content
    )
}

package com.nothatcher.sproutbook.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

val Moss = Color(0xFFB6D59C)
val Gold = Color(0xFFE5BF7B)
val Rose = Color(0xFFE0AEAA)
val Sky = Color(0xFFAFCBD5)
private val Dark =
    darkColorScheme(
        primary = Moss,
        onPrimary = Color(0xFF163723),
        primaryContainer = Color(0xFF294C38),
        onPrimaryContainer = Color(0xFFE4EED7),
        secondary = Gold,
        secondaryContainer = Color(0xFF2C4938),
        onSecondaryContainer = Moss,
        surfaceContainerHighest = Color(0xFF334C3E),
        background = Color(0xFF10251E),
        surface = Color(0xFF142B23),
        surfaceContainer = Color(0xFF203C30),
        surfaceContainerHigh = Color(0xFF294238),
        surfaceContainerLow = Color(0xFF182F26),
        surfaceContainerLowest = Color(0xFF0D2019),
        onBackground = Color(0xFFF5F2E7),
        onSurface = Color(0xFFF5F2E7),
        onSurfaceVariant = Color(0xFFC2CEBF),
        outlineVariant = Color(0xFF49634F),
        error = Color(0xFFFFB4AB),
    )
private val Light =
    lightColorScheme(
        primary = Color(0xFF365B35),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDFEACC),
        secondary = Color(0xFF795B2D),
        secondaryContainer = Color(0xFFE1E8D6),
        onSecondaryContainer = Color(0xFF395137),
        onPrimaryContainer = Color(0xFF163723),
        onSurfaceVariant = Color(0xFF526550),
        outlineVariant = Color(0xFFCCD2BF),
        background = Color(0xFFF6F3E9),
        surface = Color(0xFFFAF8F0),
        surfaceContainer = Color(0xFFEFEEE2),
        surfaceContainerLow = Color(0xFFF3F1E6),
        surfaceContainerLowest = Color(0xFFFFFDF5),
        surfaceContainerHigh = Color(0xFFECEEE3),
        surfaceContainerHighest = Color(0xFFE5E9DD),
        onSurface = Color(0xFF18352A),
        onBackground = Color(0xFF18352A),
    )

internal fun woodlandColorScheme(dark: Boolean, accent: String): ColorScheme {
    val base = if (dark) Dark else Light
    val colors = when (accent) {
        "Moss" -> if (dark) listOf(0xFFC5DCA8, 0xFF223C1C, 0xFF344D29, 0xFFE5F1D6)
            else listOf(0xFF4B6137, 0xFFFFFFFF, 0xFFE3EDCC, 0xFF263D1B)
        "Amber" -> if (dark) listOf(0xFFE5BF7B, 0xFF2A2112, 0xFF4C3B22, 0xFFFFF0D0)
            else listOf(0xFF795B2D, 0xFFFFFFFF, 0xFFF1E2BE, 0xFF4C341A)
        "Sky" -> if (dark) listOf(0xFFAFCBD5, 0xFF153B44, 0xFF284952, 0xFFDEEEF3)
            else listOf(0xFF345F69, 0xFFFFFFFF, 0xFFD9EBEF, 0xFF153F48)
        else -> return base
    }.map { Color(it) }
    return base.copy(primary = colors[0], onPrimary = colors[1], primaryContainer = colors[2],
        onPrimaryContainer = colors[3], secondaryContainer = colors[2], onSecondaryContainer = colors[3])
}

@Composable
fun BabyTheme(dark: Boolean, accent: String = "Forest", content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(
        colorScheme = woodlandColorScheme(dark, accent),
        typography =
            base.copy(
                headlineLarge =
                    base.headlineLarge.copy(fontFamily = FontFamily.Serif, fontSize = 30.sp, lineHeight = 36.sp),
                headlineMedium =
                    base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontSize = 27.sp),
                headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif, fontSize = 22.sp, lineHeight = 31.sp),
                titleMedium = base.titleMedium.copy(fontSize = 18.sp, lineHeight = 24.sp),
                labelLarge = base.labelLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
                bodyLarge = base.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
            ),
        content = content,
    )
}

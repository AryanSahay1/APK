package com.nexos.ai.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.nexos.ai.data.repository.ThemeMode

private val NexosDarkScheme = darkColorScheme(
    primary = NexosPrimary,
    onPrimary = NexosBackground,
    primaryContainer = NexosPrimarySoft,
    onPrimaryContainer = NexosPrimary,
    secondary = NexosInfo,
    onSecondary = NexosBackground,
    background = NexosBackground,
    onBackground = NexosOnSurface,
    surface = NexosSurface,
    onSurface = NexosOnSurface,
    surfaceVariant = NexosSurfaceElevated,
    onSurfaceVariant = NexosOnSurfaceMuted,
    outline = NexosBorder,
    outlineVariant = NexosBorder,
    error = NexosError,
    onError = NexosBackground
)

private val NexosLightScheme = lightColorScheme(
    primary = NexosPrimaryLight,
    onPrimary = NexosSurfaceLight,
    primaryContainer = NexosPrimarySoftLight,
    onPrimaryContainer = NexosPrimaryLight,
    secondary = NexosInfo,
    onSecondary = NexosSurfaceLight,
    background = NexosBackgroundLight,
    onBackground = NexosOnSurfaceLight,
    surface = NexosSurfaceLight,
    onSurface = NexosOnSurfaceLight,
    surfaceVariant = NexosSurfaceElevatedLight,
    onSurfaceVariant = NexosOnSurfaceMutedLight,
    outline = NexosBorderLight,
    outlineVariant = NexosBorderLight,
    error = NexosErrorLight,
    onError = NexosSurfaceLight
)

val NexosShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * NexOS theme. Defaults to following the system but accepts an explicit override so the
 * Settings screen can toggle Day / Night / System with no restart.
 *
 * Light mode uses a darker primary green (NexosPrimaryLight = #00B85F) to clear AA contrast
 * over the warm off-white background — bright #00E676 reads as washed-out on light backgrounds.
 */
@Composable
fun NexosTheme(
    mode: ThemeMode = ThemeMode.System,
    fontChoice: FontChoice = FontChoice.Inter,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (mode) {
        ThemeMode.System -> systemDark
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }
    val colorScheme = if (useDark) NexosDarkScheme else NexosLightScheme

    // Apply the user-picked Google Font across every typography slot. The default value
    // (Inter) closely matches the bundled FontFamily.SansSerif, so picking 'Inter' is a
    // no-op visually — the other 4 fonts produce a genuinely different look.
    val family = rememberFontFamily(fontChoice)
    val typography = remember(fontChoice) { applyFontFamily(NexosTypography, family) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !useDark
                    isAppearanceLightNavigationBars = !useDark
                }
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = NexosShapes,
        content = content
    )
}

/**
 * Rebuild a Typography with every TextStyle re-pointed at the supplied [family]. Cheaper
 * than constructing the whole Typography from scratch for each font choice — the per-slot
 * weight / size / letter-spacing tokens stay identical.
 */
private fun applyFontFamily(base: androidx.compose.material3.Typography, family: androidx.compose.ui.text.font.FontFamily): androidx.compose.material3.Typography =
    androidx.compose.material3.Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family)
    )

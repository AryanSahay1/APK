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
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (mode) {
        ThemeMode.System -> systemDark
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }
    val colorScheme = if (useDark) NexosDarkScheme else NexosLightScheme

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
        typography = NexosTypography,
        shapes = NexosShapes,
        content = content
    )
}

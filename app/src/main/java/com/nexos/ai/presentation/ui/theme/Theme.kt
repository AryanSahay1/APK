package com.nexos.ai.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NexosColorScheme = darkColorScheme(
    primary = NexosPrimary,
    onPrimary = NexosBackground,
    primaryContainer = NexosPrimaryDim,
    onPrimaryContainer = NexosBackground,
    secondary = NexosInfo,
    onSecondary = NexosBackground,
    background = NexosBackground,
    onBackground = NexosOnSurface,
    surface = NexosSurface,
    onSurface = NexosOnSurface,
    surfaceVariant = NexosSurfaceElevated,
    onSurfaceVariant = NexosOnSurfaceMuted,
    outline = NexosOutline,
    error = NexosError,
    onError = NexosBackground
)

@Composable
fun NexosTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = NexosBackground.toArgb()
            window.navigationBarColor = NexosBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    MaterialTheme(
        colorScheme = NexosColorScheme,
        typography = NexosTypography,
        content = content
    )
}

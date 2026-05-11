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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.nexos.ai.data.repository.ThemeMode

// M3 Expressive (2025) tonal palettes: primary / secondary / tertiary all populated with
// matching `container` + `onContainer` roles, so MaterialTheme.colorScheme.*Container reads
// give intentional values (the warm peach tertiary for accent surfaces, the blue secondary
// for informational chips). Components stop falling back to surfaceVariant for tinted
// backgrounds — a key M3 Expressive correctness win.
private val NexosDarkScheme = darkColorScheme(
    primary = NexosPrimary,
    onPrimary = NexosBackground,
    primaryContainer = NexosPrimarySoft,
    onPrimaryContainer = NexosPrimary,
    secondary = NexosInfo,
    onSecondary = NexosBackground,
    secondaryContainer = Color(0x334DA6FF),
    onSecondaryContainer = NexosInfo,
    tertiary = NexosTertiary,
    onTertiary = NexosOnTertiary,
    tertiaryContainer = NexosTertiaryContainer,
    onTertiaryContainer = NexosOnTertiaryContainer,
    background = NexosBackground,
    onBackground = NexosOnSurface,
    surface = NexosSurface,
    onSurface = NexosOnSurface,
    surfaceVariant = NexosSurfaceElevated,
    onSurfaceVariant = NexosOnSurfaceMuted,
    surfaceTint = NexosPrimary,
    outline = NexosBorder,
    outlineVariant = NexosBorder,
    scrim = Color(0xCC000000),
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
    secondaryContainer = Color(0x224DA6FF),
    onSecondaryContainer = NexosInfo,
    tertiary = NexosTertiaryLight,
    onTertiary = NexosOnTertiaryLight,
    tertiaryContainer = NexosTertiaryContainerLight,
    onTertiaryContainer = NexosOnTertiaryContainerLight,
    background = NexosBackgroundLight,
    onBackground = NexosOnSurfaceLight,
    surface = NexosSurfaceLight,
    onSurface = NexosOnSurfaceLight,
    surfaceVariant = NexosSurfaceElevatedLight,
    onSurfaceVariant = NexosOnSurfaceMutedLight,
    surfaceTint = NexosPrimaryLight,
    outline = NexosBorderLight,
    outlineVariant = NexosBorderLight,
    scrim = Color(0xAA000000),
    error = NexosErrorLight,
    onError = NexosSurfaceLight
)

// M3 Expressive shape scale (per SKILL__2_.md §5):
//   extraSmall 4, small 8, medium 12, large 16, extraLarge 28.
// We round slightly looser than spec on `large` (20 dp instead of 16) because the panda
// mascot's circular silhouette pairs better with softer card edges.
val NexosShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
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

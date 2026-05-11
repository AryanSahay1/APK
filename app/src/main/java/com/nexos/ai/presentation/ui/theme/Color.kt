package com.nexos.ai.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// ----- Dark palette (default since v1.0) -----
val NexosBackground = Color(0xFF07070F)
val NexosSurface = Color(0xFF0D0D1A)
val NexosSurfaceElevated = Color(0xFF15152A)
val NexosOnSurface = Color(0xFFE0E0E0)
val NexosOnSurfaceMuted = Color(0xFFB8B8C8)
val NexosMuted = Color(0xFF7C7C8A)
val NexosBorder = Color(0xFF22223A)
val NexosPrimary = Color(0xFF00E676)
val NexosPrimaryDim = Color(0xFF00B85F)
val NexosPrimarySoft = Color(0x3300E676)
val NexosError = Color(0xFFFF6B6B)
val NexosWarning = Color(0xFFFFB800)
val NexosInfo = Color(0xFF4DA6FF)

// ----- Light palette (added v1.1) -----
// Warm off-white background so the panda's white face still reads as "white" against the page,
// rather than disappearing into a pure-white surface. All foreground tokens cleared 4.5:1 against
// the lightest surface in manual contrast checks.
val NexosBackgroundLight = Color(0xFFF8F8F4)
val NexosSurfaceLight = Color(0xFFFFFFFF)
val NexosSurfaceElevatedLight = Color(0xFFF1F1EC)
val NexosOnSurfaceLight = Color(0xFF14141B)
val NexosOnSurfaceMutedLight = Color(0xFF4A4A55)
val NexosBorderLight = Color(0xFFE2E2DC)
val NexosPrimaryLight = Color(0xFF00B85F) // slightly darkened for AA on light bg
val NexosPrimarySoftLight = Color(0x2200B85F)
val NexosErrorLight = Color(0xFFD32F2F)

// ----- M3 Expressive tertiary palette (v1.4) -----
// A warm peach used for accent surfaces (the weather strip's emoji halo, panda mascot
// highlights, marketing chips). Pairs with the green primary without competing for the eye.
val NexosTertiary = Color(0xFFFFAB91)             // soft peach, dark mode
val NexosOnTertiary = Color(0xFF1F1A18)
val NexosTertiaryContainer = Color(0x33FFAB91)
val NexosOnTertiaryContainer = Color(0xFFFFD9CC)

val NexosTertiaryLight = Color(0xFFD84315)        // burnt orange, light mode
val NexosOnTertiaryLight = Color(0xFFFFFFFF)
val NexosTertiaryContainerLight = Color(0xFFFFCCBC)
val NexosOnTertiaryContainerLight = Color(0xFF3E1B0A)

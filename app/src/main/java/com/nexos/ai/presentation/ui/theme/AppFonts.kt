package com.nexos.ai.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.nexos.ai.R

/**
 * The 5 user-pickable display fonts requested in v1.4. All loaded on-demand from the Google
 * Fonts CDN via Play Services — they're not bundled in the APK, which keeps the build size
 * flat. The first time a font is used, the system downloads + caches it; subsequent uses
 * are instant.
 *
 * The five fonts cover distinct moods so the user can re-skin the whole app feel:
 *   * Inter         — clean modern sans (the system default look)
 *   * Lora          — warm serif, good for long-form notes / notebook PDF export
 *   * Roboto Slab   — slab serif, technical/editorial feel
 *   * Quicksand     — rounded geometric sans, friendlier mascot vibe
 *   * JetBrains Mono — monospace, for users who write code-heavy notes
 */
enum class FontChoice(val displayName: String, val googleFontName: String, val key: String) {
    Inter("Inter", "Inter", "inter"),
    Lora("Lora", "Lora", "lora"),
    RobotoSlab("Roboto Slab", "Roboto Slab", "roboto-slab"),
    Quicksand("Quicksand", "Quicksand", "quicksand"),
    JetBrainsMono("JetBrains Mono", "JetBrains Mono", "jetbrains-mono");

    companion object {
        const val DEFAULT_KEY = "inter"
        fun fromKey(key: String?): FontChoice = entries.firstOrNull { it.key == key } ?: Inter
    }
}

/**
 * Family resolver — builds a FontFamily that downloads the requested Google Font on demand.
 * Cached by [remember] so repeated reads in the typography system don't trigger redundant
 * provider lookups.
 */
@Composable
fun rememberFontFamily(choice: FontChoice): FontFamily {
    val provider = remember {
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    }
    return remember(choice) {
        val font = GoogleFont(choice.googleFontName)
        FontFamily(
            Font(googleFont = font, fontProvider = provider, weight = FontWeight.Normal),
            Font(googleFont = font, fontProvider = provider, weight = FontWeight.Medium),
            Font(googleFont = font, fontProvider = provider, weight = FontWeight.SemiBold),
            Font(googleFont = font, fontProvider = provider, weight = FontWeight.Bold)
        )
    }
}

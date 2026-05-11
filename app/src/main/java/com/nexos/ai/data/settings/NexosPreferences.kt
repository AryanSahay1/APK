package com.nexos.ai.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Typed DataStore keys for every non-sensitive user preference.
 * API keys never live here — see [com.nexos.ai.data.secure.SecureStorage].
 */
object NexosPreferences {
    val AI_PROVIDER = stringPreferencesKey("ai_provider")
    val AUTO_SUMMARIZE = booleanPreferencesKey("auto_summarize")
    val AUTO_SAVE = booleanPreferencesKey("auto_save")
    val SHOW_FLOATING_BUTTON = booleanPreferencesKey("show_floating_button")
    val FLOATING_BUTTON_SIDE = stringPreferencesKey("floating_button_side")
    val FLOATING_BUTTON_Y = intPreferencesKey("floating_button_y")
}

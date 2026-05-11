package com.nexos.ai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nexos.ai.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "nexos_settings")

object NexosPreferences {
    val AI_PROVIDER = stringPreferencesKey("ai_provider")
    val AUTO_SUMMARIZE = booleanPreferencesKey("auto_summarize")
    val AUTO_SAVE = booleanPreferencesKey("auto_save")
    val FLOATING_BUTTON_SIDE = stringPreferencesKey("floating_button_side")
    val SHOW_FLOATING_BUTTON = booleanPreferencesKey("show_floating_button")
    val FLOATING_BUTTON_Y = stringPreferencesKey("floating_button_y")
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val store = context.settingsDataStore

    val aiProvider: Flow<String> = store.data.map { it[NexosPreferences.AI_PROVIDER] ?: Constants.PROVIDER_NONE }
    val autoSummarize: Flow<Boolean> = store.data.map { it[NexosPreferences.AUTO_SUMMARIZE] ?: true }
    val autoSave: Flow<Boolean> = store.data.map { it[NexosPreferences.AUTO_SAVE] ?: true }
    val showFloatingButton: Flow<Boolean> = store.data.map { it[NexosPreferences.SHOW_FLOATING_BUTTON] ?: false }
    val floatingButtonSide: Flow<String> = store.data.map { it[NexosPreferences.FLOATING_BUTTON_SIDE] ?: "right" }
    val floatingButtonY: Flow<Float> = store.data.map { (it[NexosPreferences.FLOATING_BUTTON_Y] ?: "0.45").toFloatOrNull() ?: 0.45f }

    suspend fun setAiProvider(provider: String) {
        store.edit { it[NexosPreferences.AI_PROVIDER] = provider }
    }

    suspend fun setAutoSummarize(value: Boolean) {
        store.edit { it[NexosPreferences.AUTO_SUMMARIZE] = value }
    }

    suspend fun setAutoSave(value: Boolean) {
        store.edit { it[NexosPreferences.AUTO_SAVE] = value }
    }

    suspend fun setShowFloatingButton(value: Boolean) {
        store.edit { it[NexosPreferences.SHOW_FLOATING_BUTTON] = value }
    }

    suspend fun setFloatingButtonPosition(side: String, yFraction: Float) {
        store.edit {
            it[NexosPreferences.FLOATING_BUTTON_SIDE] = side
            it[NexosPreferences.FLOATING_BUTTON_Y] = yFraction.coerceIn(0f, 1f).toString()
        }
    }

    /**
     * Wipes the screenshot cache directory. Architecture (Layer 5 — Image Cache Management):
     * cache lives only so users can view the original screenshot that generated a note. The
     * Settings screen exposes this; nothing else depends on those files being present.
     */
    suspend fun clearImageCache(): Long = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        var bytes = 0L
        runCatching {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("nexos_capture_")) {
                    bytes += file.length()
                    file.delete()
                }
            }
        }
        bytes
    }
}

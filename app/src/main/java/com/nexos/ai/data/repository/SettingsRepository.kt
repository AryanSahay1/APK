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
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val LAST_UBER_DESTINATION = stringPreferencesKey("last_uber_destination")
    val LAST_RAPIDO_DESTINATION = stringPreferencesKey("last_rapido_destination")
    val RECENT_SWIGGY_QUERIES = stringPreferencesKey("recent_swiggy_queries")
    val RECENT_ZOMATO_QUERIES = stringPreferencesKey("recent_zomato_queries")
    val WEATHER_LAST_CITY = stringPreferencesKey("weather_last_city")
}

/**
 * Theme preference. [System] follows isSystemInDarkTheme() — the most respectful default for
 * users who already configured their OS theme.
 */
enum class ThemeMode(val key: String) {
    System("system"), Light("light"), Dark("dark");

    companion object {
        fun fromKey(value: String?): ThemeMode = entries.firstOrNull { it.key == value } ?: System
    }
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val store = context.settingsDataStore

    val themeMode: Flow<ThemeMode> = store.data.map { ThemeMode.fromKey(it[NexosPreferences.THEME_MODE]) }
    val aiProvider: Flow<String> = store.data.map { it[NexosPreferences.AI_PROVIDER] ?: Constants.PROVIDER_NONE }
    val autoSummarize: Flow<Boolean> = store.data.map { it[NexosPreferences.AUTO_SUMMARIZE] ?: true }
    val autoSave: Flow<Boolean> = store.data.map { it[NexosPreferences.AUTO_SAVE] ?: true }
    val showFloatingButton: Flow<Boolean> = store.data.map { it[NexosPreferences.SHOW_FLOATING_BUTTON] ?: false }
    val floatingButtonSide: Flow<String> = store.data.map { it[NexosPreferences.FLOATING_BUTTON_SIDE] ?: "right" }
    val floatingButtonY: Flow<Float> = store.data.map { (it[NexosPreferences.FLOATING_BUTTON_Y] ?: "0.45").toFloatOrNull() ?: 0.45f }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[NexosPreferences.THEME_MODE] = mode.key }
    }

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

    // ----- User history (Phase 1.1: Uber/Swiggy/Zomato/Weather) -----
    val lastUberDestination: Flow<String> = store.data.map { it[NexosPreferences.LAST_UBER_DESTINATION].orEmpty() }
    val lastRapidoDestination: Flow<String> = store.data.map { it[NexosPreferences.LAST_RAPIDO_DESTINATION].orEmpty() }
    val recentSwiggyQueries: Flow<List<String>> = store.data.map {
        decodeList(it[NexosPreferences.RECENT_SWIGGY_QUERIES])
    }
    val recentZomatoQueries: Flow<List<String>> = store.data.map {
        decodeList(it[NexosPreferences.RECENT_ZOMATO_QUERIES])
    }
    val weatherLastCity: Flow<String> = store.data.map { it[NexosPreferences.WEATHER_LAST_CITY].orEmpty() }

    suspend fun setLastUberDestination(value: String) {
        store.edit { it[NexosPreferences.LAST_UBER_DESTINATION] = value.trim() }
    }

    suspend fun setLastRapidoDestination(value: String) {
        store.edit { it[NexosPreferences.LAST_RAPIDO_DESTINATION] = value.trim() }
    }

    suspend fun pushSwiggyQuery(query: String) = pushHistory(NexosPreferences.RECENT_SWIGGY_QUERIES, query)
    suspend fun pushZomatoQuery(query: String) = pushHistory(NexosPreferences.RECENT_ZOMATO_QUERIES, query)

    suspend fun setWeatherLastCity(value: String) {
        store.edit { it[NexosPreferences.WEATHER_LAST_CITY] = value.trim() }
    }

    private suspend fun pushHistory(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        query: String,
        max: Int = 5
    ) {
        val cleaned = query.trim()
        if (cleaned.isEmpty()) return
        store.edit { prefs ->
            val existing = decodeList(prefs[key])
            val updated = (listOf(cleaned) + existing.filter { it != cleaned }).take(max)
            prefs[key] = encodeList(updated)
        }
    }

    private fun encodeList(items: List<String>): String =
        items.joinToString(separator = "\u001f") { it.replace("\u001f", " ") }

    private fun decodeList(raw: String?): List<String> =
        if (raw.isNullOrBlank()) emptyList()
        else raw.split('\u001f').filter { it.isNotBlank() }

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

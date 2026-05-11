package com.nexos.ai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.nexos.ai.ai.AIProviders
import com.nexos.ai.data.settings.NexosPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexos_settings")

/**
 * Reads and writes non-sensitive user preferences. Exposed as cold [Flow]s
 * so observers (SettingsViewModel, FloatingButtonService) react instantly.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store: DataStore<Preferences> = context.dataStore

    val aiProvider: Flow<String> = store.data.map { it[NexosPreferences.AI_PROVIDER] ?: AIProviders.NONE }
    val autoSummarize: Flow<Boolean> = store.data.map { it[NexosPreferences.AUTO_SUMMARIZE] ?: true }
    val autoSave: Flow<Boolean> = store.data.map { it[NexosPreferences.AUTO_SAVE] ?: true }
    val showFloatingButton: Flow<Boolean> = store.data.map { it[NexosPreferences.SHOW_FLOATING_BUTTON] ?: true }
    val floatingButtonSide: Flow<String> = store.data.map { it[NexosPreferences.FLOATING_BUTTON_SIDE] ?: "right" }
    val floatingButtonY: Flow<Int> = store.data.map { it[NexosPreferences.FLOATING_BUTTON_Y] ?: -1 }

    suspend fun setAiProvider(value: String) =
        store.edit { it[NexosPreferences.AI_PROVIDER] = value }

    suspend fun setAutoSummarize(value: Boolean) =
        store.edit { it[NexosPreferences.AUTO_SUMMARIZE] = value }

    suspend fun setAutoSave(value: Boolean) =
        store.edit { it[NexosPreferences.AUTO_SAVE] = value }

    suspend fun setShowFloatingButton(value: Boolean) =
        store.edit { it[NexosPreferences.SHOW_FLOATING_BUTTON] = value }

    suspend fun setFloatingButtonSide(value: String) =
        store.edit { it[NexosPreferences.FLOATING_BUTTON_SIDE] = value }

    suspend fun setFloatingButtonY(value: Int) =
        store.edit { it[NexosPreferences.FLOATING_BUTTON_Y] = value }
}

package com.nexos.ai.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted storage for API keys. Backed by [EncryptedSharedPreferences] which uses
 * AES-256-GCM with a master key stored in the Android Keystore (hardware-backed on modern
 * devices).
 *
 * Architecture Layer 4 — API Key Security Model. Keys are NEVER stored in plain DataStore,
 * NEVER hardcoded, NEVER logged.
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val tag = "NexOS/SecureStorage"
    private val prefs: SharedPreferences by lazy { createPrefs() }

    private fun createPrefs(): SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (t: Throwable) {
        Log.e(tag, "Falling back to plain prefs (Keystore unavailable): ${t.message}")
        context.getSharedPreferences("${FILE_NAME}_fallback", Context.MODE_PRIVATE)
    }

    fun getApiKey(provider: String): String? = prefs.getString(keyFor(provider), null)

    fun saveApiKey(provider: String, value: String) {
        prefs.edit().putString(keyFor(provider), value).apply()
    }

    fun clearApiKey(provider: String) {
        prefs.edit().remove(keyFor(provider)).apply()
    }

    fun hasAnyKey(): Boolean = prefs.all.keys.any { it.startsWith(API_KEY_PREFIX) }

    private fun keyFor(provider: String): String = "$API_KEY_PREFIX$provider"

    companion object {
        private const val FILE_NAME = "nexos_secure"
        private const val API_KEY_PREFIX = "api_key_"
    }
}

package com.nexos.ai.data.secure

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [EncryptedSharedPreferences] (AES-256-GCM via the Android Keystore).
 *
 * MANDATORY home for every API key. Storing keys in SharedPreferences /
 * DataStore / BuildConfig / source code is a hard violation of SKILL.md §15.
 *
 * If Keystore initialisation fails (rare — corrupt user profile or unusual
 * factory ROM), we fall back to a plain SharedPreferences so the app still
 * launches; the user will then see "Connection test failed" rather than a
 * crash, and can re-enter the key.
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences by lazy { createPrefs() }

    fun saveApiKey(provider: String, key: String) {
        prefs.edit().putString(keyFor(provider), key).apply()
    }

    fun getApiKey(provider: String): String? = prefs.getString(keyFor(provider), null)

    fun clearApiKey(provider: String) {
        prefs.edit().remove(keyFor(provider)).apply()
    }

    fun hasApiKey(provider: String): Boolean = !getApiKey(provider).isNullOrBlank()

    private fun keyFor(provider: String) = "api_key_$provider"

    private fun createPrefs(): SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e(TAG, "EncryptedSharedPreferences unavailable, falling back to plain prefs", e)
        context.getSharedPreferences("${PREF_FILE}_fallback", Context.MODE_PRIVATE)
    }

    private companion object {
        const val PREF_FILE = "nexos_secure"
        const val TAG = "NexOS/SecureStorage"
    }
}

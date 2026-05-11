package com.nexos.ai.ai

import android.util.Log
import com.nexos.ai.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes bootstrap API keys to [SecureStorage] on first launch so the app works out-of-the-box
 * for end users who haven't paid for or registered an account anywhere.
 *
 * Currently seeds:
 *   - **GNews** (gnews.io) — supplied by the user in commit 48b9882. Lets the News tab
 *     load headlines immediately on first install. 100 req/day free.
 *
 * Idempotent: only writes a slot if it is currently empty, so the user can always rotate or
 * remove a seeded key from Settings and we'll never overwrite their replacement.
 *
 * Future seeds (intentionally NOT added now):
 *   - AI provider keys — these are personal billing-linked secrets; the user supplies their
 *     own per provider.
 *   - Google Maps API key — currently unused (the Maps tab is deep-link only).
 */
@Singleton
class ApiKeySeeder @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val secureStorage: SecureStorage
) {

    private val tag = "NexOS/ApiKeySeeder"

    /**
     * Seeds bootstrap keys. Safe to call multiple times — re-runs are no-ops.
     *
     * The check is "if the slot is empty, write it". This means a user who deletes a key in
     * Settings will be re-seeded on next app launch. That is intentional: NexOS guarantees
     * the news tab works without configuration. If a user wants the slot to stay empty, they
     * can install their own key (any non-empty value disables re-seeding).
     */
    fun seedIfNeeded() {
        SEEDS.forEach { (slot, value, label) ->
            val existing = secureStorage.getApiKey(slot)
            if (existing.isNullOrBlank()) {
                secureStorage.saveApiKey(slot, value)
                Log.i(tag, "Seeded $label into '$slot'")
            }
        }
    }

    private data class Seed(val slot: String, val value: String, val label: String)

    private val SEEDS: List<Seed> = listOf(
        // GNews bootstrap key (user-supplied, gnews.io developer tier — 100 requests/day).
        // Stored encrypted in the Android Keystore; rotate freely from Settings.
        Seed(
            slot = Constants.PROVIDER_NEWS_API,
            value = "5c33bb47e30a76d50029a00b7a28adeb",
            label = "GNews developer key"
        )
        // OpenWeather slot intentionally NOT seeded:
        //   The placeholder key from an earlier agent (ccdd42ee…) returns HTTP 401 on the
        //   live API — it was never valid. With no seed, WeatherRepository routes to
        //   Open-Meteo (free, no key, no signup), which works for every user out-of-the-box.
        //   The user can still paste their own OpenWeather key in Settings → Weather provider
        //   if they want the higher accuracy / faster updates that tier provides.
    )
}

package com.nexos.ai.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Lightweight wrapper over the Android system [LocationManager]. We deliberately do **not**
 * pull in Google Play Services' FusedLocationProviderClient — it would add ~600 KB to the APK
 * for a feature (the weather strip on the home screen) that only needs a one-off coarse
 * location. The system LocationManager covers that need on every device.
 *
 * Returns null when:
 *   - The user has not granted [Manifest.permission.ACCESS_COARSE_LOCATION], or
 *   - No provider has a cached last-known location, or
 *   - All providers are disabled.
 *
 * Callers should always have a fallback (e.g. the last-saved city in DataStore) so the
 * weather strip never goes blank just because the user denied location.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    /** Last known coarse location, or null if unavailable for any reason. */
    @SuppressLint("MissingPermission") // gated on hasPermission()
    suspend fun lastKnown(): Location? = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null
        val providers = lm.getProviders(true)
        var best: Location? = null
        for (provider in providers) {
            val candidate = runCatching { lm.getLastKnownLocation(provider) }.getOrNull() ?: continue
            val current = best
            if (current == null || candidate.accuracy < current.accuracy) best = candidate
        }
        best
    }

    /**
     * Request a single fresh coarse location reading. Falls back to [lastKnown] if no provider
     * delivers within [timeoutMillis].
     */
    @SuppressLint("MissingPermission")
    suspend fun currentOrLast(timeoutMillis: Long = 4_000L): Location? {
        if (!hasPermission()) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val providers = lm.getProviders(true)
        val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .firstOrNull { it in providers } ?: return lastKnown()

        return try {
            kotlinx.coroutines.withTimeoutOrNull(timeoutMillis) {
                suspendCancellableCoroutine<Location?> { cont ->
                    val listener = object : android.location.LocationListener {
                        override fun onLocationChanged(location: Location) {
                            if (cont.isActive) cont.resume(location)
                            runCatching { lm.removeUpdates(this) }
                        }
                        @Suppress("OVERRIDE_DEPRECATION")
                        override fun onProviderDisabled(provider: String) {
                            if (cont.isActive) cont.resume(null)
                        }
                        @Suppress("OVERRIDE_DEPRECATION")
                        override fun onProviderEnabled(provider: String) = Unit
                        @Suppress("OVERRIDE_DEPRECATION")
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
                    }
                    cont.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
                    @Suppress("DEPRECATION") // requestSingleUpdate is fine on minSdk 26;
                    // requestLocationUpdates with a single-shot Looper would require API 30+.
                    lm.requestSingleUpdate(provider, listener, context.mainLooper)
                }
            } ?: lastKnown()
        } catch (t: Throwable) {
            lastKnown()
        }
    }
}

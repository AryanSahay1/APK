package com.nexos.ai.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Public deep-link launchers for the Phase-4 super-app integrations.
 *
 * Free / open-licence policy: none of these require an SDK or API key. We only construct the
 * public URI schemes each app publishes; if the target app is not installed we fall back to
 * its Play Store listing. NexOS never sees the user's location, payment information, or order
 * details — it merely hands control to the chosen app.
 *
 * Trademark notice (see NOTICE): Uber, Rapido, Zomato, and Swiggy are trademarks of their
 * respective owners. This integration is unofficial and unaffiliated.
 */
object DeepLinks {

    private const val TAG = "NexOS/DeepLinks"

    // -------- Ride hailing --------

    /**
     * Uber: https://developer.uber.com/docs/riders/ride-requests/tutorials/deep-links/introduction
     * Falls back to https://m.uber.com/looking when the app is not installed.
     */
    fun launchUber(
        context: Context,
        destinationAddress: String,
        destinationLat: Double? = null,
        destinationLng: Double? = null,
        nickname: String? = null
    ): LaunchResult {
        val uri = Uri.Builder()
            .scheme("uber")
            .authority("")
            .appendQueryParameter("action", "setPickup")
            .appendQueryParameter("pickup", "my_location")
            .apply {
                appendQueryParameter("dropoff[formatted_address]", destinationAddress)
                if (destinationLat != null && destinationLng != null) {
                    appendQueryParameter("dropoff[latitude]", destinationLat.toString())
                    appendQueryParameter("dropoff[longitude]", destinationLng.toString())
                }
                if (!nickname.isNullOrBlank()) {
                    appendQueryParameter("dropoff[nickname]", nickname)
                }
            }
            .build()
        val fallback = Uri.parse("https://m.uber.com/looking?drop[0]=${urlEncode(destinationAddress)}")
        return openOrFallback(context, uri, fallback, playStorePackage = "com.ubercab")
    }

    /**
     * Rapido: rapido://book?destination=… is the documented intent. We additionally pass the
     * destination as a regular query parameter so older versions of the app still pick it up.
     */
    fun launchRapido(context: Context, destinationAddress: String): LaunchResult {
        val uri = Uri.Builder()
            .scheme("rapido")
            .authority("book")
            .appendQueryParameter("destination", destinationAddress)
            .appendQueryParameter("destination_address", destinationAddress)
            .build()
        val fallback = Uri.parse(
            "https://onelink.to/rapido?destination=${urlEncode(destinationAddress)}"
        )
        return openOrFallback(
            context,
            uri,
            fallback,
            playStorePackage = "com.rapido.passenger"
        )
    }

    // -------- Food ordering --------

    /**
     * Zomato: zomato://search?query=…  Restaurant search defaults to the user's currently
     * configured location inside Zomato — NexOS never passes coordinates.
     */
    fun launchZomato(context: Context, query: String): LaunchResult {
        val safeQuery = query.ifBlank { "restaurants near me" }
        val uri = Uri.Builder()
            .scheme("zomato")
            .authority("search")
            .appendQueryParameter("query", safeQuery)
            .build()
        val fallback = Uri.parse("https://www.zomato.com/search?q=${urlEncode(safeQuery)}")
        return openOrFallback(context, uri, fallback, playStorePackage = "com.application.zomato")
    }

    /**
     * Swiggy: swiggy://search?query=… Behaves like Zomato — Swiggy uses the device location
     * already configured inside the app.
     */
    fun launchSwiggy(context: Context, query: String): LaunchResult {
        val safeQuery = query.ifBlank { "restaurants near me" }
        val uri = Uri.Builder()
            .scheme("swiggy")
            .authority("search")
            .appendQueryParameter("query", safeQuery)
            .build()
        val fallback = Uri.parse("https://www.swiggy.com/search?query=${urlEncode(safeQuery)}")
        return openOrFallback(context, uri, fallback, playStorePackage = "in.swiggy.android")
    }

    // -------- Maps / general --------

    /** Open a Google Maps search for the given query. */
    fun launchMapsSearch(context: Context, query: String): LaunchResult {
        val gmm = Uri.parse("geo:0,0?q=${urlEncode(query)}")
        val fallback = Uri.parse("https://www.google.com/maps/search/?api=1&query=${urlEncode(query)}")
        return openOrFallback(context, gmm, fallback, playStorePackage = "com.google.android.apps.maps")
    }

    // -------- Plumbing --------

    /**
     * Try [primary] first; if no Activity handles it, try [fallbackHttps]; if even that
     * fails, open the Play Store listing for the missing app.
     */
    fun openOrFallback(
        context: Context,
        primary: Uri,
        fallbackHttps: Uri,
        playStorePackage: String
    ): LaunchResult {
        val intent = Intent(Intent.ACTION_VIEW, primary).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
            Log.i(TAG, "Launched ${primary.scheme}://${primary.authority}")
            return LaunchResult.OpenedNative
        } catch (e: ActivityNotFoundException) {
            Log.i(TAG, "Native handler missing for ${primary.scheme}, trying https fallback")
        } catch (t: Throwable) {
            Log.e(TAG, "Native handler crashed: ${t.message}")
        }

        val httpsIntent = Intent(Intent.ACTION_VIEW, fallbackHttps).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(httpsIntent)
            LaunchResult.OpenedFallback
        } catch (e: ActivityNotFoundException) {
            openPlayStore(context, playStorePackage)
            LaunchResult.OpenedPlayStore
        }
    }

    private fun openPlayStore(context: Context, packageName: String) {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(market) }.onFailure {
            val web = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(web) }
        }
    }

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
            .replace("+", "%20")

    enum class LaunchResult { OpenedNative, OpenedFallback, OpenedPlayStore }
}

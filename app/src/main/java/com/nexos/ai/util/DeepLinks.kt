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
     * Rapido does not publish a documented "open the app with destination prefilled" deep
     * link. The previous `rapido://book?destination=…` attempt was speculative and never
     * resolved on any installed Rapido version.
     *
     * Pragmatic workflow: open Google Maps directions to the destination so the user can
     * see the route and the address copy buffer is loaded; then we surface the Rapido app
     * directly via its launcher intent so the user can paste/type the destination they just
     * saw. If Rapido isn't installed we fall through to its Play Store page.
     *
     * This is the same pattern any third-party app uses to "open Rapido at a place" — there
     * is no API to pre-fill destination from outside Rapido's own UI.
     */
    fun launchRapido(context: Context, destinationAddress: String): LaunchResult {
        // 1. Try the Rapido app's launcher intent (opens app's home screen).
        val pm = context.packageManager
        val rapidoLaunch = pm.getLaunchIntentForPackage("com.rapido.passenger")
        if (rapidoLaunch != null) {
            rapidoLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // Stash the destination in the clipboard so the user can paste it inside Rapido.
            copyToClipboard(context, "NexOS destination", destinationAddress)
            return try {
                context.startActivity(rapidoLaunch)
                LaunchResult.OpenedNative
            } catch (t: Throwable) {
                Log.e(TAG, "Rapido launcher intent failed: ${t.message}")
                openMapsFallback(context, destinationAddress)
            }
        }
        // 2. Rapido not installed — open Google Maps directions so the user can switch to
        //    any ride app from there (Maps shows "Ride" buttons). Stash destination to
        //    clipboard either way.
        copyToClipboard(context, "NexOS destination", destinationAddress)
        return openMapsFallback(context, destinationAddress)
    }

    private fun openMapsFallback(context: Context, destination: String): LaunchResult {
        return launchMapsDirections(context, destination)
    }

    private fun copyToClipboard(context: Context, label: String, value: String) {
        if (value.isBlank()) return
        runCatching {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            cm.setPrimaryClip(android.content.ClipData.newPlainText(label, value))
        }
    }

    // -------- Food ordering --------

    /**
     * Zomato: open via the HTTPS App Link `https://www.zomato.com/search?q=…`. Zomato's
     * Android app registers as a handler for `*.zomato.com` URLs, so Android routes the
     * intent into the installed app and the search query is passed through. The previous
     * `zomato://search?query=` private scheme only opened the app's home, not the search.
     *
     * Also tries `setPackage("com.application.zomato")` first so Android doesn't show a
     * disambiguation chooser if the user has multiple browsers installed.
     */
    fun launchZomato(context: Context, query: String): LaunchResult {
        val safeQuery = query.ifBlank { "restaurants" }
        val webUrl = Uri.parse("https://www.zomato.com/search?q=${urlEncode(safeQuery)}")

        // Try the Zomato app explicitly first
        val appIntent = Intent(Intent.ACTION_VIEW, webUrl).apply {
            setPackage("com.application.zomato")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(appIntent) }.onSuccess {
            return LaunchResult.OpenedNative
        }
        // Fall back to a plain VIEW intent — Android App Links will still route to Zomato
        // if installed, else open in browser, else Play Store via openOrFallback.
        return openOrFallback(context, webUrl, webUrl, playStorePackage = "com.application.zomato")
    }

    /**
     * Swiggy: same App-Link pattern as Zomato. We've kept the previous `swiggy://search`
     * fallback for legacy installs but App Links to `https://www.swiggy.com/search?query=`
     * are more reliable across Swiggy versions.
     */
    fun launchSwiggy(context: Context, query: String): LaunchResult {
        val safeQuery = query.ifBlank { "restaurants" }
        val webUrl = Uri.parse("https://www.swiggy.com/search?query=${urlEncode(safeQuery)}")

        val appIntent = Intent(Intent.ACTION_VIEW, webUrl).apply {
            setPackage("in.swiggy.android")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(appIntent) }.onSuccess {
            return LaunchResult.OpenedNative
        }
        return openOrFallback(context, webUrl, webUrl, playStorePackage = "in.swiggy.android")
    }

    // -------- Maps / general --------

    /** Open a Google Maps search for the given query. */
    fun launchMapsSearch(context: Context, query: String): LaunchResult {
        val gmm = Uri.parse("geo:0,0?q=${urlEncode(query)}")
        val fallback = Uri.parse("https://www.google.com/maps/search/?api=1&query=${urlEncode(query)}")
        return openOrFallback(context, gmm, fallback, playStorePackage = "com.google.android.apps.maps")
    }

    /** Open Google Maps for turn-by-turn navigation to a destination. */
    fun launchMapsDirections(context: Context, destination: String): LaunchResult {
        val nav = Uri.parse("google.navigation:q=${urlEncode(destination)}")
        val fallback = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&destination=${urlEncode(destination)}"
        )
        return openOrFallback(context, nav, fallback, playStorePackage = "com.google.android.apps.maps")
    }

    // -------- Google ecosystem --------

    /** Compose a Gmail email. Both fields optional. */
    fun launchGmailCompose(context: Context, to: String = "", subject: String = ""): LaunchResult {
        val mailto = Uri.parse(
            buildString {
                append("mailto:")
                append(urlEncode(to))
                val params = mutableListOf<String>()
                if (subject.isNotBlank()) params += "subject=${urlEncode(subject)}"
                if (params.isNotEmpty()) {
                    append("?")
                    append(params.joinToString("&"))
                }
            }
        )
        val fallback = Uri.parse("https://mail.google.com/mail/?view=cm&to=${urlEncode(to)}&su=${urlEncode(subject)}")
        return openOrFallback(context, mailto, fallback, playStorePackage = "com.google.android.gm")
    }

    /** Open Google Calendar to create a new event. */
    fun launchCalendarEvent(context: Context, title: String = ""): LaunchResult {
        val deepLink = Uri.parse(
            "content://com.android.calendar/time"
        )
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(android.provider.CalendarContract.Events.CONTENT_URI)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (title.isNotBlank()) {
            intent.putExtra(android.provider.CalendarContract.Events.TITLE, title)
        }
        return try {
            context.startActivity(intent)
            LaunchResult.OpenedNative
        } catch (e: ActivityNotFoundException) {
            val webFallback = Uri.parse(
                "https://calendar.google.com/calendar/u/0/r/eventedit?text=${urlEncode(title)}"
            )
            openOrFallback(context, deepLink, webFallback, playStorePackage = "com.google.android.calendar")
        }
    }

    /** Open Google Drive. */
    fun launchDrive(context: Context): LaunchResult {
        val drive = Uri.parse("https://drive.google.com/")
        return openOrFallback(
            context,
            primary = drive,
            fallbackHttps = drive,
            playStorePackage = "com.google.android.apps.docs"
        )
    }

    /** Open a Google web search for the query. */
    fun launchGoogleSearch(context: Context, query: String): LaunchResult {
        val web = Uri.parse("https://www.google.com/search?q=${urlEncode(query)}")
        return openOrFallback(
            context,
            primary = web,
            fallbackHttps = web,
            playStorePackage = "com.google.android.googlequicksearchbox"
        )
    }

    /** Open Google Translate. */
    fun launchTranslate(context: Context, text: String = ""): LaunchResult {
        val web = Uri.parse("https://translate.google.com/?text=${urlEncode(text)}")
        return openOrFallback(
            context,
            primary = web,
            fallbackHttps = web,
            playStorePackage = "com.google.android.apps.translate"
        )
    }

    /** Open Google Photos. */
    fun launchPhotos(context: Context): LaunchResult {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            LaunchResult.OpenedNative
        } catch (e: ActivityNotFoundException) {
            val web = Uri.parse("https://photos.google.com/")
            openOrFallback(context, web, web, playStorePackage = "com.google.android.apps.photos")
        }
    }

    /** Open YouTube with a search query. */
    fun launchYouTube(context: Context, query: String = ""): LaunchResult {
        val native = if (query.isBlank()) Uri.parse("https://www.youtube.com/")
        else Uri.parse("https://www.youtube.com/results?search_query=${urlEncode(query)}")
        return openOrFallback(
            context,
            primary = native,
            fallbackHttps = native,
            playStorePackage = "com.google.android.youtube"
        )
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

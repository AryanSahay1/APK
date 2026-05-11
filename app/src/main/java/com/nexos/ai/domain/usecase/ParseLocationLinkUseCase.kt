package com.nexos.ai.domain.usecase

import com.nexos.ai.data.remote.api.NominatimApi
import com.nexos.ai.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects a Google Maps or WhatsApp location share URL inside a free-form string, extracts
 * the (latitude, longitude) pair, and reverse-geocodes it into a clean human-readable address
 * via OpenStreetMap Nominatim (free, no key required).
 *
 * Supported URL shapes:
 *   - Google Maps:
 *       https://www.google.com/maps/place/.../@LAT,LNG,zoom
 *       https://www.google.com/maps/?q=LAT,LNG
 *       https://www.google.com/maps/dir/...?destination=LAT,LNG
 *       https://maps.google.com/?q=LAT,LNG
 *       https://maps.app.goo.gl/SHORTCODE      (Maps shortlink — followed via OkHttp)
 *       geo:LAT,LNG[?q=...]
 *   - WhatsApp share:
 *       https://api.whatsapp.com/...?location=LAT,LNG
 *       https://wa.me/...                       (text-only, no coords)
 *
 * Falls back to a "couldn't parse this link" Result.failure when no coordinates are visible.
 * Always returns a [ParsedLocation] or a typed exception — never throws to the caller.
 */
@Singleton
class ParseLocationLinkUseCase @Inject constructor(
    private val nominatimApi: NominatimApi,
    private val okHttpClient: OkHttpClient
) {

    data class ParsedLocation(
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val sourceLabel: String   // 'Google Maps' / 'WhatsApp' / 'geo: URI'
    )

    sealed class ParseException(message: String) : Exception(message) {
        data object NoUrlFound : ParseException("No location link found in that message")
        data object NoCoordinates : ParseException("Couldn't extract coordinates from that link")
        data class GeocodeFailed(val detail: String) : ParseException("Reverse-geocoding failed: $detail")
        data object Timeout : ParseException("Reverse-geocoding timed out")
    }

    suspend operator fun invoke(input: String): Result<ParsedLocation> = withContext(Dispatchers.IO) {
        if (input.isBlank()) return@withContext Result.failure(ParseException.NoUrlFound)

        val rawUrl = findFirstUrl(input) ?: return@withContext Result.failure(ParseException.NoUrlFound)
        val urlToParse = if (isMapsShortLink(rawUrl)) {
            resolveRedirect(rawUrl) ?: rawUrl
        } else {
            rawUrl
        }
        val sourceLabel = when {
            urlToParse.startsWith("geo:") -> "geo: URI"
            urlToParse.contains("whatsapp", ignoreCase = true) -> "WhatsApp"
            urlToParse.contains("google.", ignoreCase = true) ||
                urlToParse.contains("goo.gl", ignoreCase = true) -> "Google Maps"
            else -> "URL"
        }
        val coords = extractCoordinates(urlToParse)
            ?: return@withContext Result.failure(ParseException.NoCoordinates)

        try {
            withTimeout(Constants.AI_REQUEST_TIMEOUT_MS) {
                val response = nominatimApi.reverse(coords.first, coords.second)
                if (!response.isSuccessful) {
                    return@withTimeout Result.failure(ParseException.GeocodeFailed("HTTP ${response.code()}"))
                }
                val body = response.body() ?: return@withTimeout Result.failure(ParseException.GeocodeFailed("empty"))
                val clean = cleanAddress(body) ?: body.displayName.orEmpty()
                if (clean.isBlank()) {
                    return@withTimeout Result.failure(ParseException.GeocodeFailed("blank address"))
                }
                Result.success(
                    ParsedLocation(
                        address = clean,
                        latitude = coords.first,
                        longitude = coords.second,
                        sourceLabel = sourceLabel
                    )
                )
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(ParseException.Timeout)
        } catch (e: IOException) {
            Result.failure(ParseException.GeocodeFailed(e.message ?: ""))
        } catch (t: Throwable) {
            Result.failure(ParseException.GeocodeFailed(t.message ?: ""))
        }
    }

    /**
     * Walk the address fields in order of specificity and stitch a clean, comma-separated
     * "street, suburb, city, country" string — strips out the spam Nominatim sometimes
     * includes (specific shop names, alternate language transliterations).
     */
    private fun cleanAddress(dto: com.nexos.ai.data.remote.dto.NominatimReverseDto): String? {
        val a = dto.address ?: return null
        val parts = listOfNotNull(
            listOfNotNull(a.houseNumber, a.road).joinToString(" ").takeIf { it.isNotBlank() },
            a.neighbourhood,
            a.suburb,
            a.village ?: a.town ?: a.city,
            a.state,
            a.country
        ).distinct()
        return if (parts.isEmpty()) null else parts.joinToString(", ")
    }

    private fun findFirstUrl(text: String): String? {
        // Match http(s) URLs OR geo: URIs OR Maps shortlinks
        val regex = Regex("""(?i)(?:https?://[^\s]+|geo:[^\s]+)""")
        return regex.find(text)?.value
    }

    private fun isMapsShortLink(url: String): Boolean =
        url.startsWith("https://maps.app.goo.gl/", ignoreCase = true) ||
            url.startsWith("https://goo.gl/maps/", ignoreCase = true)

    /**
     * Follow a 30x redirect once to expand a Maps shortlink to its full coordinate-bearing URL.
     */
    private fun resolveRedirect(url: String): String? {
        return try {
            val client = okHttpClient.newBuilder().followRedirects(false).build()
            val request = Request.Builder().url(url).head().build()
            client.newCall(request).execute().use { response ->
                response.header("Location") ?: response.request.url.toString()
            }
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * Extract (lat, lng) from any of the supported URL shapes. Returns null if no
     * coordinate-pair is visible (e.g. WhatsApp wa.me deep links with no location).
     */
    private fun extractCoordinates(url: String): Pair<Double, Double>? {
        // Maps "@lat,lng,zoom"
        Regex("""@(-?\d+\.\d+),(-?\d+\.\d+)""").find(url)?.let {
            val lat = it.groupValues[1].toDoubleOrNull()
            val lng = it.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) return lat to lng
        }
        // ?q=lat,lng  or  &q=lat,lng  or  &destination=lat,lng
        Regex("""[?&](?:q|destination|center|ll)=(-?\d+\.\d+),(-?\d+\.\d+)""").find(url)?.let {
            val lat = it.groupValues[1].toDoubleOrNull()
            val lng = it.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) return lat to lng
        }
        // WhatsApp ?location=lat,lng
        Regex("""[?&]location=(-?\d+\.\d+),(-?\d+\.\d+)""").find(url)?.let {
            val lat = it.groupValues[1].toDoubleOrNull()
            val lng = it.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) return lat to lng
        }
        // geo:lat,lng
        Regex("""^geo:(-?\d+\.\d+),(-?\d+\.\d+)""").find(url)?.let {
            val lat = it.groupValues[1].toDoubleOrNull()
            val lng = it.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) return lat to lng
        }
        // Last-resort: any "lat,lng"-shaped substring with reasonable magnitudes
        Regex("""(-?\d{1,2}\.\d{3,}),\s*(-?\d{1,3}\.\d{3,})""").find(url)?.let {
            val lat = it.groupValues[1].toDoubleOrNull()
            val lng = it.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                return lat to lng
            }
        }
        return null
    }
}

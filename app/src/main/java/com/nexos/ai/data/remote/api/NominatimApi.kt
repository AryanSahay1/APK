package com.nexos.ai.data.remote.api

import com.nexos.ai.data.remote.dto.NominatimReverseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * OpenStreetMap Nominatim — free reverse-geocoding service. No API key, no signup.
 * Rate limit: 1 request/second per IP for unauthenticated use, and a User-Agent header is
 * mandatory (Nominatim enforces it to block abusive scrapers).
 *
 * We send `User-Agent: NexOS/1.x` per Nominatim's policy. Reverse-geocoding is invoked by
 * [com.nexos.ai.domain.usecase.ParseLocationLinkUseCase] when the user pastes a Google Maps
 * or WhatsApp live-location URL into the panda assistant.
 */
interface NominatimApi {

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json",
        @Query("zoom") zoom: Int = 18,
        @Query("addressdetails") addressDetails: Int = 1,
        @Header("User-Agent") userAgent: String = USER_AGENT,
        @Header("Accept-Language") acceptLanguage: String = "en"
    ): Response<NominatimReverseDto>

    companion object {
        const val BASE_URL = "https://nominatim.openstreetmap.org/"
        const val USER_AGENT = "NexOS/1.3 (https://github.com/AryanSahay1/APK)"
    }
}

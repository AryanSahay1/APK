package com.nexos.ai.data.remote.api

import com.nexos.ai.data.remote.dto.GeocodingResponse
import com.nexos.ai.data.remote.dto.OpenMeteoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo Weather API. **No API key required.** No rate-limit signup. Free for
 * commercial use under the CC-BY 4.0 license (we attribute in NOTICE).
 *
 * Two endpoints used:
 *   - /v1/forecast — current weather + daily 7-day forecast
 *   - /v1/search   — city name → coordinates (via geocoding-api.open-meteo.com)
 */
interface WeatherApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("daily") daily: String =
            "weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7
    ): Response<OpenMeteoResponse>

    companion object {
        const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
    }
}

/**
 * Geocoding is hosted at a separate base URL, so it gets its own Retrofit-typed interface.
 */
interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): Response<GeocodingResponse>

    companion object {
        const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    }
}

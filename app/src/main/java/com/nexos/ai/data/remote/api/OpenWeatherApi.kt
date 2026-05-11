package com.nexos.ai.data.remote.api

import com.nexos.ai.data.remote.dto.OpenWeatherCurrentDto
import com.nexos.ai.data.remote.dto.OpenWeatherForecastDto
import com.nexos.ai.data.remote.dto.OpenWeatherGeoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeather API (https://openweathermap.org).
 *
 * Auth: appid as a **query parameter**. NexOS's [com.nexos.ai.di.RedactingLogger] masks the
 * `appid` value before it reaches Logcat in debug builds.
 *
 * Free tier covers:
 *   - /data/2.5/weather       — current weather (60 req/min)
 *   - /data/2.5/forecast      — 5-day, 3-hour forecast
 *   - /geo/1.0/direct         — city → coordinates
 *
 * We deliberately avoid /data/3.0/onecall (paid tier).
 */
interface OpenWeatherApi {

    @GET("data/2.5/weather")
    suspend fun currentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<OpenWeatherCurrentDto>

    @GET("data/2.5/forecast")
    suspend fun forecast5Day(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<OpenWeatherForecastDto>

    @GET("geo/1.0/direct")
    suspend fun geocode(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): Response<List<OpenWeatherGeoDto>>

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
    }
}

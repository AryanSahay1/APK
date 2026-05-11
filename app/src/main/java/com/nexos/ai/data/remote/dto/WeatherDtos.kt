package com.nexos.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Open-Meteo forecast response (subset). See https://open-meteo.com/en/docs.
 * Fields the UI does not use are intentionally omitted to keep Gson allocation cheap.
 */
data class OpenMeteoResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String? = null,
    @SerializedName("current_weather") val currentWeather: CurrentWeatherDto? = null,
    val daily: DailyDto? = null
)

data class CurrentWeatherDto(
    val temperature: Double = 0.0,
    val windspeed: Double = 0.0,
    val winddirection: Double = 0.0,
    @SerializedName("weathercode") val weatherCode: Int = 0,
    val time: String? = null
)

data class DailyDto(
    val time: List<String> = emptyList(),
    @SerializedName("weathercode") val weatherCode: List<Int> = emptyList(),
    @SerializedName("temperature_2m_max") val tempMax: List<Double> = emptyList(),
    @SerializedName("temperature_2m_min") val tempMin: List<Double> = emptyList(),
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList()
)

/**
 * Open-Meteo geocoding endpoint — converts a city name to a (lat, lon) pair so the user can
 * type a city instead of granting location permission.
 */
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

data class GeocodingResult(
    val name: String = "",
    val country: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String? = null
)

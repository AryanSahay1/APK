package com.nexos.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * OpenWeather `/data/2.5/weather` response shape (subset).
 * See https://openweathermap.org/current.
 */
data class OpenWeatherCurrentDto(
    val name: String? = null,
    val coord: OpenWeatherCoordDto? = null,
    val weather: List<OpenWeatherConditionDto> = emptyList(),
    val main: OpenWeatherMainDto? = null,
    val wind: OpenWeatherWindDto? = null,
    val sys: OpenWeatherSysDto? = null,
    val timezone: Int = 0,
    val dt: Long = 0L
)

data class OpenWeatherCoordDto(val lat: Double = 0.0, val lon: Double = 0.0)
data class OpenWeatherConditionDto(
    val id: Int = 0,
    val main: String? = null,
    val description: String? = null,
    val icon: String? = null
)
data class OpenWeatherMainDto(
    val temp: Double = 0.0,
    @SerializedName("temp_min") val tempMin: Double = 0.0,
    @SerializedName("temp_max") val tempMax: Double = 0.0,
    val humidity: Int = 0,
    @SerializedName("feels_like") val feelsLike: Double = 0.0
)
data class OpenWeatherWindDto(val speed: Double = 0.0, val deg: Double = 0.0)
data class OpenWeatherSysDto(
    val country: String? = null,
    val sunrise: Long = 0L,
    val sunset: Long = 0L
)

/**
 * OpenWeather `/data/2.5/forecast` response shape (subset).
 * Free-tier returns 5 days × 8 entries/day (3-hour granularity).
 * See https://openweathermap.org/forecast5.
 */
data class OpenWeatherForecastDto(
    val list: List<OpenWeatherForecastEntryDto> = emptyList(),
    val city: OpenWeatherCityDto? = null
)

data class OpenWeatherForecastEntryDto(
    val dt: Long = 0L,
    @SerializedName("dt_txt") val dtText: String? = null,
    val main: OpenWeatherMainDto? = null,
    val weather: List<OpenWeatherConditionDto> = emptyList(),
    val wind: OpenWeatherWindDto? = null
)

data class OpenWeatherCityDto(
    val name: String? = null,
    val country: String? = null,
    val sunrise: Long = 0L,
    val sunset: Long = 0L,
    val timezone: Int = 0
)

/** OpenWeather geocoding result. https://openweathermap.org/api/geocoding-api */
data class OpenWeatherGeoDto(
    val name: String = "",
    val country: String? = null,
    val state: String? = null,
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

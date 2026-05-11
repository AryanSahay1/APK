package com.nexos.ai.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain-level weather snapshot. UI uses this — not the Open-Meteo DTOs directly.
 *
 * Temperatures are in Celsius (Open-Meteo's metric default). The UI may convert to °F at
 * render time; we never store both to keep this immutable record canonical.
 */
@Immutable
data class WeatherSnapshot(
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double,
    val currentTempC: Double,
    val currentCode: WeatherCondition,
    val windSpeedKph: Double,
    val daily: List<DailyForecast>,
    val sunrise: String,
    val sunset: String,
    val capturedAt: Long = System.currentTimeMillis()
)

@Immutable
data class DailyForecast(
    val date: String,
    val condition: WeatherCondition,
    val maxC: Double,
    val minC: Double
)

/**
 * WMO weather code → friendly bucket + emoji. Mapping per Open-Meteo docs:
 * https://open-meteo.com/en/docs (search "WMO Weather interpretation codes").
 */
enum class WeatherCondition(val label: String, val emoji: String) {
    Clear("Clear", "☀️"),
    MainlyClear("Mainly clear", "🌤️"),
    PartlyCloudy("Partly cloudy", "⛅"),
    Overcast("Overcast", "☁️"),
    Fog("Fog", "🌫️"),
    Drizzle("Drizzle", "🌦️"),
    Rain("Rain", "🌧️"),
    Snow("Snow", "❄️"),
    Showers("Showers", "🌦️"),
    Thunderstorm("Thunderstorm", "⛈️"),
    Unknown("Unknown", "🌡️");

    companion object {
        fun fromWmo(code: Int): WeatherCondition = when (code) {
            0 -> Clear
            1 -> MainlyClear
            2 -> PartlyCloudy
            3 -> Overcast
            45, 48 -> Fog
            51, 53, 55, 56, 57 -> Drizzle
            61, 63, 65, 66, 67 -> Rain
            71, 73, 75, 77 -> Snow
            80, 81, 82, 85, 86 -> Showers
            95, 96, 99 -> Thunderstorm
            else -> Unknown
        }
    }
}

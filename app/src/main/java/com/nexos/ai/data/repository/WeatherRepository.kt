package com.nexos.ai.data.repository

import com.nexos.ai.ai.SecureStorage
import com.nexos.ai.data.remote.api.OpenMeteoGeocodingApi
import com.nexos.ai.data.remote.api.OpenWeatherApi
import com.nexos.ai.data.remote.api.WeatherApi
import com.nexos.ai.data.remote.dto.OpenWeatherForecastEntryDto
import com.nexos.ai.domain.model.DailyForecast
import com.nexos.ai.domain.model.WeatherCondition
import com.nexos.ai.domain.model.WeatherSnapshot
import com.nexos.ai.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dual-backend weather repository.
 *
 *   - **Primary: OpenWeather** when the user has an `openweather` key in [SecureStorage].
 *     Free tier covers /data/2.5/weather (current) and /data/2.5/forecast (5 days × 3-hour
 *     entries). We aggregate the 3-hour list into daily max/min for the UI.
 *   - **Fallback: Open-Meteo** when no key is configured. Free, no signup, 7-day daily forecast.
 *
 * The OpenWeather key is auto-seeded on first launch by [com.nexos.ai.ai.ApiKeySeeder] so the
 * primary path is active out-of-the-box. The user can rotate or clear the key in Settings →
 * Weather; clearing it transparently downgrades to Open-Meteo.
 *
 * Governance:
 *   - 30 s timeout on every call (Constants.AI_REQUEST_TIMEOUT_MS)
 *   - Dispatchers.IO
 *   - Typed WeatherException tree; UI never sees DTOs
 *   - appid/apikey values are query-string-redacted in Logcat by RedactingLogger
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val openMeteoApi: WeatherApi,
    private val openMeteoGeocodingApi: OpenMeteoGeocodingApi,
    private val openWeatherApi: OpenWeatherApi,
    private val secureStorage: SecureStorage
) {

    private fun openWeatherKey(): String? =
        secureStorage.getApiKey(Constants.PROVIDER_OPENWEATHER)?.takeIf { it.isNotBlank() }

    fun hasOpenWeatherKey(): Boolean = openWeatherKey() != null

    suspend fun searchCity(query: String): Result<List<CityResult>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext Result.success(emptyList())
        try {
            withTimeout(Constants.AI_REQUEST_TIMEOUT_MS) {
                // Prefer OpenWeather geocoding when key set — produces the same shape the user
                // sees if they later switch providers. Falls back to Open-Meteo geocoding.
                val key = openWeatherKey()
                if (key != null) {
                    val response = openWeatherApi.geocode(query, apiKey = key)
                    if (!response.isSuccessful) {
                        return@withTimeout Result.failure(WeatherException.Http(response.code()))
                    }
                    val results = response.body().orEmpty().map {
                        CityResult(
                            label = listOfNotNull(it.name, it.state, it.country).joinToString(", "),
                            latitude = it.lat,
                            longitude = it.lon
                        )
                    }
                    Result.success(results)
                } else {
                    val response = openMeteoGeocodingApi.searchCity(query)
                    if (!response.isSuccessful) {
                        return@withTimeout Result.failure(WeatherException.Http(response.code()))
                    }
                    val results = response.body()?.results.orEmpty().map {
                        CityResult(
                            label = listOfNotNull(it.name, it.country).joinToString(", "),
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    }
                    Result.success(results)
                }
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(WeatherException.Timeout)
        } catch (e: IOException) {
            Result.failure(WeatherException.Network(e.message ?: ""))
        } catch (t: Throwable) {
            Result.failure(WeatherException.Unknown(t.message ?: ""))
        }
    }

    /**
     * Routes to OpenWeather when configured, otherwise Open-Meteo. On any OpenWeather failure
     * (network blip, key revoked, quota exhausted) the call transparently falls back to
     * Open-Meteo so the user always sees data.
     */
    suspend fun forecast(
        latitude: Double,
        longitude: Double,
        locationLabel: String
    ): Result<WeatherSnapshot> = withContext(Dispatchers.IO) {
        val key = openWeatherKey()
        if (key != null) {
            val attempt = fetchOpenWeather(latitude, longitude, locationLabel, key)
            if (attempt.isSuccess) return@withContext attempt
        }
        runOpenMeteo(latitude, longitude, locationLabel)
    }

    private suspend fun fetchOpenWeather(
        latitude: Double,
        longitude: Double,
        locationLabel: String,
        key: String
    ): Result<WeatherSnapshot> = try {
        withTimeout(Constants.AI_REQUEST_TIMEOUT_MS) {
            val current = openWeatherApi.currentWeather(latitude, longitude, key)
            val forecast = openWeatherApi.forecast5Day(latitude, longitude, key)
            if (!current.isSuccessful || !forecast.isSuccessful) {
                return@withTimeout Result.failure(
                    WeatherException.Http(current.code().takeIf { !current.isSuccessful } ?: forecast.code())
                )
            }
            val cur = current.body() ?: return@withTimeout Result.failure(WeatherException.Empty)
            val fc = forecast.body() ?: return@withTimeout Result.failure(WeatherException.Empty)

            val daily = aggregateOpenWeather(fc.list, fc.city?.timezone ?: 0)
            val sunrise = cur.sys?.sunrise?.let { formatHm(it, cur.timezone) }.orEmpty()
            val sunset = cur.sys?.sunset?.let { formatHm(it, cur.timezone) }.orEmpty()

            Result.success(
                WeatherSnapshot(
                    locationLabel = listOfNotNull(cur.name, cur.sys?.country).joinToString(", ").ifBlank { locationLabel },
                    latitude = cur.coord?.lat ?: latitude,
                    longitude = cur.coord?.lon ?: longitude,
                    currentTempC = cur.main?.temp ?: 0.0,
                    currentCode = openWeatherIdToCondition(cur.weather.firstOrNull()?.id ?: 0),
                    windSpeedKph = (cur.wind?.speed ?: 0.0) * 3.6, // m/s → km/h
                    daily = daily,
                    sunrise = sunrise,
                    sunset = sunset
                )
            )
        }
    } catch (e: TimeoutCancellationException) {
        Result.failure(WeatherException.Timeout)
    } catch (e: IOException) {
        Result.failure(WeatherException.Network(e.message ?: ""))
    } catch (t: Throwable) {
        Result.failure(WeatherException.Unknown(t.message ?: ""))
    }

    private suspend fun runOpenMeteo(
        latitude: Double,
        longitude: Double,
        locationLabel: String
    ): Result<WeatherSnapshot> = try {
        withTimeout(Constants.AI_REQUEST_TIMEOUT_MS) {
            val response = openMeteoApi.forecast(latitude, longitude)
            if (!response.isSuccessful) {
                return@withTimeout Result.failure(WeatherException.Http(response.code()))
            }
            val body = response.body() ?: return@withTimeout Result.failure(WeatherException.Empty)
            val current = body.currentWeather ?: return@withTimeout Result.failure(WeatherException.Empty)
            val daily = body.daily ?: return@withTimeout Result.failure(WeatherException.Empty)

            val dailyForecasts = daily.time.indices.map { i ->
                DailyForecast(
                    date = daily.time.getOrNull(i).orEmpty(),
                    condition = WeatherCondition.fromWmo(daily.weatherCode.getOrNull(i) ?: -1),
                    maxC = daily.tempMax.getOrNull(i) ?: 0.0,
                    minC = daily.tempMin.getOrNull(i) ?: 0.0
                )
            }
            Result.success(
                WeatherSnapshot(
                    locationLabel = locationLabel,
                    latitude = body.latitude,
                    longitude = body.longitude,
                    currentTempC = current.temperature,
                    currentCode = WeatherCondition.fromWmo(current.weatherCode),
                    windSpeedKph = current.windspeed,
                    daily = dailyForecasts,
                    sunrise = daily.sunrise.firstOrNull().orEmpty(),
                    sunset = daily.sunset.firstOrNull().orEmpty()
                )
            )
        }
    } catch (e: TimeoutCancellationException) {
        Result.failure(WeatherException.Timeout)
    } catch (e: IOException) {
        Result.failure(WeatherException.Network(e.message ?: ""))
    } catch (t: Throwable) {
        Result.failure(WeatherException.Unknown(t.message ?: ""))
    }

    /**
     * Aggregate OpenWeather's 5-day × 3-hour entries into 5 daily summaries (max/min/condition).
     * Groups by `YYYY-MM-DD` in the city's local timezone.
     */
    private fun aggregateOpenWeather(
        entries: List<OpenWeatherForecastEntryDto>,
        timezoneOffsetSeconds: Int
    ): List<DailyForecast> {
        if (entries.isEmpty()) return emptyList()
        val tz = TimeZone.getTimeZone("UTC").apply { rawOffset = timezoneOffsetSeconds * 1000 }
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { this.timeZone = tz }
        val grouped = entries.groupBy { dayFormat.format(Date(it.dt * 1000)) }
        return grouped.entries.take(7).map { (date, dayEntries) ->
            val max = dayEntries.mapNotNull { it.main?.tempMax }.maxOrNull() ?: 0.0
            val min = dayEntries.mapNotNull { it.main?.tempMin }.minOrNull() ?: 0.0
            // Pick the noon-ish entry's condition for the day's "headline" weather
            val noonEntry = dayEntries.minByOrNull {
                val hour = SimpleDateFormat("HH", Locale.US).apply { this.timeZone = tz }
                    .format(Date(it.dt * 1000)).toIntOrNull() ?: 0
                kotlin.math.abs(hour - 12)
            } ?: dayEntries.first()
            val condId = noonEntry.weather.firstOrNull()?.id ?: 0
            DailyForecast(
                date = date,
                condition = openWeatherIdToCondition(condId),
                maxC = max,
                minC = min
            )
        }
    }

    private fun formatHm(unixSeconds: Long, tzOffsetSeconds: Int): String {
        val tz = TimeZone.getTimeZone("UTC").apply { rawOffset = tzOffsetSeconds * 1000 }
        val fmt = SimpleDateFormat("HH:mm", Locale.US).apply { this.timeZone = tz }
        return fmt.format(Date(unixSeconds * 1000))
    }

    /**
     * OpenWeather condition ID (https://openweathermap.org/weather-conditions) → our
     * [WeatherCondition] bucket. The mapping mirrors the WMO mapping so the UI behaves the
     * same regardless of which backend served the request.
     */
    private fun openWeatherIdToCondition(id: Int): WeatherCondition = when (id) {
        in 200..232 -> WeatherCondition.Thunderstorm
        in 300..321 -> WeatherCondition.Drizzle
        in 500..504 -> WeatherCondition.Rain
        511 -> WeatherCondition.Snow
        in 520..531 -> WeatherCondition.Showers
        in 600..622 -> WeatherCondition.Snow
        in 701..781 -> WeatherCondition.Fog
        800 -> WeatherCondition.Clear
        801 -> WeatherCondition.MainlyClear
        802 -> WeatherCondition.PartlyCloudy
        803, 804 -> WeatherCondition.Overcast
        else -> WeatherCondition.Unknown
    }

}

data class CityResult(val label: String, val latitude: Double, val longitude: Double)

sealed class WeatherException(message: String) : Exception(message) {
    data object Empty : WeatherException("Empty response from weather provider")
    data object Timeout : WeatherException("Weather request timed out")
    data class Http(val code: Int) : WeatherException("HTTP $code")
    data class Network(val detail: String) : WeatherException("Network: $detail")
    data class Unknown(val detail: String) : WeatherException("Unknown: $detail")
}

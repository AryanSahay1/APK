package com.nexos.ai.data.repository

import com.nexos.ai.data.remote.api.OpenMeteoGeocodingApi
import com.nexos.ai.data.remote.api.WeatherApi
import com.nexos.ai.domain.model.DailyForecast
import com.nexos.ai.domain.model.WeatherCondition
import com.nexos.ai.domain.model.WeatherSnapshot
import com.nexos.ai.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the Open-Meteo APIs with the same governance discipline as the other repositories:
 *   - 30 s timeout (Layer 4 rule re-used for non-AI providers)
 *   - All work on Dispatchers.IO
 *   - Typed [WeatherException] tree
 *   - DTO → domain mapping; UI never sees Gson DTOs
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi,
    private val geocodingApi: OpenMeteoGeocodingApi
) {

    suspend fun searchCity(query: String): Result<List<CityResult>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext Result.success(emptyList())
        try {
            withTimeout(Constants.AI_REQUEST_TIMEOUT_MS) {
                val response = geocodingApi.searchCity(query)
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
        } catch (e: TimeoutCancellationException) {
            Result.failure(WeatherException.Timeout)
        } catch (e: IOException) {
            Result.failure(WeatherException.Network(e.message ?: ""))
        } catch (t: Throwable) {
            Result.failure(WeatherException.Unknown(t.message ?: ""))
        }
    }

    suspend fun forecast(
        latitude: Double,
        longitude: Double,
        locationLabel: String
    ): Result<WeatherSnapshot> = withContext(Dispatchers.IO) {
        try {
            withTimeout(Constants.AI_REQUEST_TIMEOUT_MS) {
                val response = weatherApi.forecast(latitude, longitude)
                if (!response.isSuccessful) {
                    return@withTimeout Result.failure(WeatherException.Http(response.code()))
                }
                val body = response.body() ?: return@withTimeout Result.failure(WeatherException.Empty)
                val current = body.currentWeather ?: return@withTimeout Result.failure(WeatherException.Empty)
                val daily = body.daily ?: return@withTimeout Result.failure(WeatherException.Empty)

                val dailyForecasts = (daily.time.indices).map { i ->
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
    }
}

data class CityResult(val label: String, val latitude: Double, val longitude: Double)

sealed class WeatherException(message: String) : Exception(message) {
    data object Empty : WeatherException("Empty response from Open-Meteo")
    data object Timeout : WeatherException("Weather request timed out")
    data class Http(val code: Int) : WeatherException("HTTP $code")
    data class Network(val detail: String) : WeatherException("Network: $detail")
    data class Unknown(val detail: String) : WeatherException("Unknown: $detail")
}

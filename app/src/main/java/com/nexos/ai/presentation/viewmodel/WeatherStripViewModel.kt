package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.data.repository.WeatherRepository
import com.nexos.ai.domain.model.WeatherCondition
import com.nexos.ai.domain.model.WeatherSnapshot
import com.nexos.ai.util.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lives only as long as the Notes screen does. Powers the live weather strip pinned at the
 * top of the home screen above the panda mascot.
 *
 * Flow:
 *   1. On init, immediately push whatever the user's last saved city was so the strip never
 *      shows blank.
 *   2. Request a fresh coarse location reading (LocationProvider). If granted + available,
 *      use it as the primary source — gives "weather where I actually am right now" rather
 *      than wherever the user last typed.
 *   3. Re-fetch every 10 minutes while the screen is alive.
 *
 * If the user never granted ACCESS_COARSE_LOCATION, the city-based fallback keeps working —
 * we just don't get the "true current location" upgrade.
 */
data class WeatherStripUiState(
    val snapshot: WeatherSnapshot? = null,
    val isLoading: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val source: Source = Source.SavedCity
) {
    enum class Source { SavedCity, DeviceLocation }
}

@HiltViewModel
class WeatherStripViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherStripUiState())
    val state: StateFlow<WeatherStripUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { refresh() }
        viewModelScope.launch {
            while (isActive) {
                delay(10 * 60_000L) // 10 minutes
                refresh()
            }
        }
    }

    /** Public so the Notes screen can call this after the location permission dialog returns. */
    fun refresh() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, hasLocationPermission = locationProvider.hasPermission())
            }

            // Try device location first
            val loc = locationProvider.currentOrLast()
            if (loc != null) {
                val label = "%.2f, %.2f".format(loc.latitude, loc.longitude)
                weatherRepository.forecast(loc.latitude, loc.longitude, label)
                    .onSuccess { snap ->
                        _state.update {
                            it.copy(
                                snapshot = snap,
                                isLoading = false,
                                source = WeatherStripUiState.Source.DeviceLocation
                            )
                        }
                        return@launch
                    }
            }

            // Fallback: last-saved city
            val city = settings.weatherLastCity.first().ifBlank { "Bengaluru" }
            val cityResult = weatherRepository.searchCity(city).getOrNull()?.firstOrNull()
            if (cityResult == null) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            weatherRepository.forecast(cityResult.latitude, cityResult.longitude, cityResult.label)
                .onSuccess { snap ->
                    _state.update {
                        it.copy(
                            snapshot = snap,
                            isLoading = false,
                            source = WeatherStripUiState.Source.SavedCity
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
}

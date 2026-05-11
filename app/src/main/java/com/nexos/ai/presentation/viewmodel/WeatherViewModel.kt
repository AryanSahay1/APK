package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.data.repository.CityResult
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.data.repository.WeatherRepository
import com.nexos.ai.domain.model.WeatherSnapshot
import com.nexos.ai.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val query: String = "",
    val searching: Boolean = false,
    val candidates: List<CityResult> = emptyList(),
    val selectedCity: CityResult? = null,
    val loading: Boolean = false,
    val snapshot: WeatherSnapshot? = null,
    val error: String? = null,
    val savedNoteId: Long = -1L
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val noteRepository: NoteRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val lastCity = settings.weatherLastCity.first()
            if (lastCity.isNotBlank()) {
                _state.update { it.copy(query = lastCity) }
                onSearch()
            }
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value, error = null) }
    }

    fun onSearch() {
        val q = _state.value.query.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(searching = true, candidates = emptyList(), error = null) }
            weatherRepository.searchCity(q)
                .onSuccess { results ->
                    _state.update {
                        it.copy(searching = false, candidates = results)
                    }
                    // Auto-pick the top match for a friction-free experience
                    results.firstOrNull()?.let { pick(it) }
                }
                .onFailure { e ->
                    _state.update { it.copy(searching = false, error = e.message ?: "Search failed") }
                }
        }
    }

    fun pick(city: CityResult) {
        _state.update { it.copy(selectedCity = city, loading = true, error = null, snapshot = null) }
        viewModelScope.launch {
            settings.setWeatherLastCity(city.label)
            weatherRepository.forecast(city.latitude, city.longitude, city.label)
                .onSuccess { snapshot ->
                    _state.update { it.copy(loading = false, snapshot = snapshot) }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "Forecast failed") }
                }
        }
    }

    fun saveAsNote() {
        val snapshot = _state.value.snapshot ?: return
        viewModelScope.launch {
            val title = "Weather · ${snapshot.locationLabel}"
            val content = buildString {
                appendLine("Now: ${snapshot.currentCode.label} ${snapshot.currentCode.emoji}")
                appendLine("Temperature: %.1f°C".format(snapshot.currentTempC))
                appendLine("Wind: %.1f km/h".format(snapshot.windSpeedKph))
                appendLine("Sunrise: ${snapshot.sunrise}  ·  Sunset: ${snapshot.sunset}")
                appendLine()
                appendLine("7-day forecast:")
                snapshot.daily.forEach { d ->
                    appendLine("• ${d.date}: ${d.condition.emoji} ${d.condition.label}, %.0f° / %.0f°".format(d.maxC, d.minC))
                }
            }
            val id = noteRepository.insert(
                Note(
                    title = title,
                    content = content,
                    summary = "Snapshot of ${snapshot.locationLabel}'s weather",
                    sourceType = Constants.SOURCE_WEATHER,
                    tags = "weather"
                )
            )
            _state.update { it.copy(savedNoteId = id) }
        }
    }
}

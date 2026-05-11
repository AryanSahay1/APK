package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RideHistoryUiState(
    val lastUberDestination: String = "",
    val lastRapidoDestination: String = ""
)

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<RideHistoryUiState> = kotlinx.coroutines.flow.combine(
        settings.lastUberDestination, settings.lastRapidoDestination
    ) { uber, rapido ->
        RideHistoryUiState(lastUberDestination = uber, lastRapidoDestination = rapido)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), RideHistoryUiState())

    fun saveUberDestination(value: String) {
        viewModelScope.launch { settings.setLastUberDestination(value) }
    }

    fun saveRapidoDestination(value: String) {
        viewModelScope.launch { settings.setLastRapidoDestination(value) }
    }
}

data class FoodHistoryUiState(
    val recentSwiggy: List<String> = emptyList(),
    val recentZomato: List<String> = emptyList()
)

@HiltViewModel
class FoodHistoryViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<FoodHistoryUiState> = kotlinx.coroutines.flow.combine(
        settings.recentSwiggyQueries, settings.recentZomatoQueries
    ) { sw, zo ->
        FoodHistoryUiState(recentSwiggy = sw, recentZomato = zo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), FoodHistoryUiState())

    fun pushSwiggy(query: String) {
        viewModelScope.launch { settings.pushSwiggyQuery(query) }
    }

    fun pushZomato(query: String) {
        viewModelScope.launch { settings.pushZomatoQuery(query) }
    }
}

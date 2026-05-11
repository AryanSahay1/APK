package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.data.repository.ThemeMode
import com.nexos.ai.presentation.ui.theme.FontChoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application-wide theme state — observed in MainActivity so the whole tree re-themes when the
 * user toggles in Settings, with no restart. Hilt-scoped so any composable can grab it via
 * hiltViewModel() and the values are always the same instance.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    val mode: StateFlow<ThemeMode> = settings.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.System
    )

    val fontChoice: StateFlow<FontChoice> = settings.fontChoiceKey
        .map { FontChoice.fromKey(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FontChoice.Inter)

    fun setMode(value: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(value) }
    }

    fun setFont(value: FontChoice) {
        viewModelScope.launch { settings.setFontChoice(value) }
    }
}

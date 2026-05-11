package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.ai.AIRouter
import com.nexos.ai.ai.SecureStorage
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val activeProvider: String = Constants.PROVIDER_NONE,
    val autoSummarize: Boolean = true,
    val showFloatingButton: Boolean = false,
    val providers: List<AIRouter.ProviderInfo> = emptyList(),
    val keyStatuses: Map<String, Boolean> = emptyMap(),
    val testResult: String? = null,
    val isTesting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val secureStorage: SecureStorage,
    private val aiRouter: AIRouter
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState(providers = aiRouter.availableProviders()))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    activeProvider = settings.aiProvider.first(),
                    autoSummarize = settings.autoSummarize.first(),
                    showFloatingButton = settings.showFloatingButton.first(),
                    keyStatuses = refreshKeyStatuses()
                )
            }
        }
    }

    private fun refreshKeyStatuses(): Map<String, Boolean> =
        aiRouter.availableProviders().associate { p -> p.key to !secureStorage.getApiKey(p.key).isNullOrBlank() }

    fun selectProvider(key: String) {
        viewModelScope.launch {
            settings.setAiProvider(key)
            _state.update { it.copy(activeProvider = key) }
        }
    }

    fun setApiKey(provider: String, value: String) {
        val trimmed = value.trim()
        viewModelScope.launch {
            if (trimmed.isBlank()) secureStorage.clearApiKey(provider)
            else secureStorage.saveApiKey(provider, trimmed)
            _state.update { it.copy(keyStatuses = refreshKeyStatuses()) }
        }
    }

    fun setAutoSummarize(value: Boolean) {
        viewModelScope.launch {
            settings.setAutoSummarize(value)
            _state.update { it.copy(autoSummarize = value) }
        }
    }

    fun setShowFloatingButton(value: Boolean) {
        viewModelScope.launch {
            settings.setShowFloatingButton(value)
            _state.update { it.copy(showFloatingButton = value) }
        }
    }

    fun testActiveProvider() {
        viewModelScope.launch {
            _state.update { it.copy(isTesting = true, testResult = null) }
            val provider = aiRouter.getActive()
            val ok = runCatching { provider.testConnection() }.getOrDefault(false)
            _state.update {
                it.copy(
                    isTesting = false,
                    testResult = if (ok) "Connected to ${provider.name}" else "Could not reach ${provider.name}"
                )
            }
        }
    }

    fun clearTestResult() {
        _state.update { it.copy(testResult = null) }
    }
}

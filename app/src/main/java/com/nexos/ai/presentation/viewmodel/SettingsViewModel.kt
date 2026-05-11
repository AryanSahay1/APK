package com.nexos.ai.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.ai.AIProviders
import com.nexos.ai.ai.AIRouter
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.data.secure.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val secureStorage: SecureStorage,
    private val aiRouter: AIRouter
) : AndroidViewModel(application) {

    val provider: StateFlow<String> = settingsRepository.aiProvider
        .stateIn(viewModelScope, SharingStarted.Eagerly, AIProviders.NONE)
    val autoSummarize: StateFlow<Boolean> = settingsRepository.autoSummarize
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val autoSave: StateFlow<Boolean> = settingsRepository.autoSave
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val showFloatingButton: StateFlow<Boolean> = settingsRepository.showFloatingButton
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _apiKeyInput = MutableStateFlow("")
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

    private val _toasts = MutableStateFlow<String?>(null)
    val toasts: StateFlow<String?> = _toasts.asStateFlow()

    private val _hasStoredKey = MutableStateFlow(false)
    val hasStoredKey: StateFlow<Boolean> = _hasStoredKey.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.aiProvider.collect { p ->
                _hasStoredKey.value = secureStorage.hasApiKey(p)
            }
        }
    }

    fun onApiKeyInputChange(value: String) { _apiKeyInput.value = value }

    fun setProvider(value: String) {
        viewModelScope.launch {
            settingsRepository.setAiProvider(value)
            _hasStoredKey.value = secureStorage.hasApiKey(value)
            _apiKeyInput.value = ""
        }
    }

    fun setAutoSummarize(value: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoSummarize(value) }
    }

    fun setAutoSave(value: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoSave(value) }
    }

    fun setShowFloatingButton(value: Boolean) {
        viewModelScope.launch { settingsRepository.setShowFloatingButton(value) }
    }

    fun saveApiKey() {
        val provider = provider.value
        if (provider == AIProviders.NONE) {
            _toasts.value = "Pick a provider first."
            return
        }
        val key = _apiKeyInput.value.trim()
        if (key.isBlank()) {
            _toasts.value = "Enter an API key."
            return
        }
        viewModelScope.launch {
            secureStorage.saveApiKey(provider, key)
            _hasStoredKey.value = true
            _toasts.value = "Key saved."
            _apiKeyInput.value = ""
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            secureStorage.clearApiKey(provider.value)
            _hasStoredKey.value = false
            _toasts.value = "Key removed."
        }
    }

    fun testConnection() {
        val providerKey = provider.value
        if (providerKey == AIProviders.NONE) {
            _toasts.value = "Pick a provider first."
            return
        }
        viewModelScope.launch {
            _toasts.value = "Testing…"
            val ok = aiRouter.getActiveProvider().testConnection()
            _toasts.value = if (ok) "Connection OK." else "Connection failed."
        }
    }

    fun clearImageCache() {
        val ctx = getApplication<Application>().applicationContext
        viewModelScope.launch {
            var count = 0
            (ctx.cacheDir.listFiles() ?: emptyArray<File>()).forEach { f ->
                if (f.isFile && (f.name.startsWith("nexos_capture") || f.extension == "png")) {
                    if (f.delete()) count++
                }
            }
            _toasts.value = "Cleared $count cached image(s)."
        }
    }

    fun consumeToast() { _toasts.value = null }
}

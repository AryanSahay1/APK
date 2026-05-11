package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.domain.model.VoiceState
import com.nexos.ai.util.NexosOrchestrator
import com.nexos.ai.voice.VoiceInputManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val state: VoiceState = VoiceState.Idle,
    val partial: String = "",
    val finalText: String = "",
    val saved: Boolean = false,
    val savedNoteId: Long = -1L
)

@HiltViewModel
class VoiceCaptureViewModel @Inject constructor(
    private val voiceManager: VoiceInputManager,
    private val orchestrator: NexosOrchestrator
) : ViewModel() {

    private val _ui = MutableStateFlow(VoiceUiState())
    val ui: StateFlow<VoiceUiState> = _ui.asStateFlow()

    private var listenJob: Job? = null

    fun isAvailable(): Boolean = voiceManager.isAvailable()

    fun startListening() {
        listenJob?.cancel()
        _ui.update { VoiceUiState(state = VoiceState.Listening) }
        listenJob = viewModelScope.launch {
            voiceManager.listen().collect { state ->
                when (state) {
                    VoiceState.Idle -> _ui.update { it.copy(state = state) }
                    VoiceState.Listening -> _ui.update { it.copy(state = state) }
                    is VoiceState.Partial -> _ui.update { it.copy(state = state, partial = state.text) }
                    is VoiceState.Result -> {
                        _ui.update { it.copy(state = state, finalText = state.text) }
                        saveTranscript(state.text)
                    }
                    is VoiceState.Error -> _ui.update { it.copy(state = state) }
                }
            }
        }
    }

    fun stopListening() {
        listenJob?.cancel()
        listenJob = null
    }

    private fun saveTranscript(text: String) {
        viewModelScope.launch {
            val note = orchestrator.handleVoiceTranscript(text)
            _ui.update { it.copy(saved = note != null, savedNoteId = note?.id ?: -1L) }
        }
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}

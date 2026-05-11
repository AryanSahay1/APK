package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.domain.model.Note
import com.nexos.ai.domain.model.WorkflowState
import com.nexos.ai.orchestrator.NexosOrchestrator
import com.nexos.ai.service.ScreenshotService
import com.nexos.ai.util.ScreenCaptureBridge
import com.nexos.ai.voice.VoiceInputManager
import com.nexos.ai.voice.VoiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val orchestrator: NexosOrchestrator,
    private val voiceInputManager: VoiceInputManager,
    private val screenCaptureBridge: ScreenCaptureBridge,
    settingsRepository: SettingsRepository
) : ViewModel() {

    /** True while we wait for MediaProjection permission so the post-grant
     *  callback knows to fire a capture. */
    private val pendingCapture = AtomicBoolean(false)

    init {
        viewModelScope.launch {
            screenCaptureBridge.events.collect { event ->
                when (event) {
                    ScreenCaptureBridge.Event.Granted -> {
                        if (pendingCapture.compareAndSet(true, false)) {
                            // Allow the service a brief moment to bind its MediaProjection
                            // before kicking off the capture pipeline.
                            kotlinx.coroutines.delay(250)
                            orchestrator.handleScreenshotCapture()
                        }
                    }
                    ScreenCaptureBridge.Event.Denied -> pendingCapture.set(false)
                }
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredNotes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) noteRepository.allNotes
            else noteRepository.searchNotes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val workflowState: StateFlow<WorkflowState> = orchestrator.state
    val toasts = orchestrator.toasts
    val voiceState: StateFlow<VoiceState> = voiceInputManager.state

    val showFloatingButton: StateFlow<Boolean> = settingsRepository.showFloatingButton
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch { noteRepository.deleteNote(id) }
    }

    /**
     * Captures the screen if the service is already bound; otherwise records
     * a pending request and asks the Activity to launch the MediaProjection
     * permission dialog. The grant callback fires the capture automatically.
     */
    fun captureScreenshot() {
        if (ScreenshotService.isReady()) {
            viewModelScope.launch { orchestrator.handleScreenshotCapture() }
        } else {
            pendingCapture.set(true)
            screenCaptureBridge.requestPermission()
        }
    }

    fun startVoiceCapture() {
        viewModelScope.launch { voiceInputManager.start() }
    }

    fun stopVoiceCapture() {
        viewModelScope.launch { voiceInputManager.stop() }
    }

    fun cancelVoiceCapture() {
        viewModelScope.launch { voiceInputManager.cancel() }
    }

    fun submitVoiceTranscript(text: String) {
        viewModelScope.launch {
            orchestrator.handleVoiceCapture(text)
            voiceInputManager.reset()
        }
    }

    fun dismissWorkflowState() = orchestrator.resetState()
}

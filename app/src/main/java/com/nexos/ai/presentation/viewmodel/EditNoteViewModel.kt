package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.domain.usecase.SaveManualNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditNoteState(
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val isLoading: Boolean = false,
    val isExistingNote: Boolean = false,
    val error: String? = null,
    val attachments: List<com.nexos.ai.domain.model.NoteAttachment> = emptyList(),
    val isRecordingAudio: Boolean = false,
    val isAttachingLocation: Boolean = false,
    val backgroundId: Int = 0,
    val textAlignment: Int = 0,
    val bodyTextSizeSp: Int = 16,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val saveManualNote: SaveManualNoteUseCase,
    private val audioRecorder: com.nexos.ai.util.AudioRecorder,
    private val locationProvider: com.nexos.ai.util.LocationProvider,
    private val parseLocationLink: com.nexos.ai.domain.usecase.ParseLocationLinkUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: -1L

    private val _state = MutableStateFlow(EditNoteState(isLoading = noteId > 0, isExistingNote = noteId > 0))
    val state: StateFlow<EditNoteState> = _state.asStateFlow()

    // Single-undo + single-redo stacks for the body text. Cheap insurance against the
    // formatting toolbar's bulk insertions — the user can step back one transformation at
    // a time. Cap depth at 50 to keep memory bounded on huge notes.
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    private val historyCap = 50

    init {
        if (noteId > 0) {
            viewModelScope.launch {
                val note = repository.getNote(noteId)
                _state.update {
                    it.copy(
                        title = note?.title.orEmpty(),
                        content = note?.content.orEmpty(),
                        tags = note?.tags.orEmpty(),
                        isLoading = false,
                        isExistingNote = note != null,
                        attachments = note?.attachmentsJson?.let {
                            com.nexos.ai.data.local.NoteAttachmentCodec.decode(it)
                        }.orEmpty(),
                        backgroundId = note?.backgroundId ?: 0,
                        textAlignment = note?.textAlignment ?: 0,
                        bodyTextSizeSp = (note?.bodyTextSizeSp ?: 0).let { if (it <= 0) 16 else it },
                        createdAt = note?.timestamp ?: System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun setBackground(id: Int) { _state.update { it.copy(backgroundId = id) } }
    fun setAlignment(a: Int) { _state.update { it.copy(textAlignment = a.coerceIn(0, 2)) } }
    fun setBodyTextSize(spValue: Int) {
        _state.update { it.copy(bodyTextSizeSp = spValue.coerceIn(12, 28)) }
    }

    /**
     * Push the *previous* body onto the undo stack and apply the new one. The caller passes
     * the post-edit string; the previous value is whatever is currently in state. This is the
     * single mutation entry-point used by both the text field and the formatting toolbar.
     */
    fun onContentChange(value: String) {
        val previous = _state.value.content
        if (previous == value) return
        undoStack.addLast(previous)
        if (undoStack.size > historyCap) undoStack.removeFirst()
        // Any fresh edit invalidates the redo stack — standard editor behaviour.
        redoStack.clear()
        _state.update {
            it.copy(content = value, canUndo = undoStack.isNotEmpty(), canRedo = false)
        }
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(_state.value.content)
        _state.update {
            it.copy(
                content = previous,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(_state.value.content)
        _state.update {
            it.copy(
                content = next,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    /** Insert a doodle (PNG file path) as an image attachment without leaving the editor. */
    fun addDoodleAttachment(absolutePath: String) {
        val att = com.nexos.ai.domain.model.NoteAttachment.Image(
            id = "doodle-${System.currentTimeMillis()}",
            uri = "file://$absolutePath",
            mimeType = "image/png"
        )
        _state.update { it.copy(attachments = it.attachments + att) }
    }

    fun addImageAttachment(uri: String, mimeType: String) {
        val att = com.nexos.ai.domain.model.NoteAttachment.Image(
            id = "img-${System.currentTimeMillis()}",
            uri = uri,
            mimeType = mimeType.ifBlank { "image/*" }
        )
        _state.update { it.copy(attachments = it.attachments + att) }
    }

    fun startAudioRecording() {
        if (audioRecorder.isRecording) return
        if (audioRecorder.start() == null) {
            _state.update { it.copy(error = "Couldn't start the microphone — check the RECORD_AUDIO permission") }
            return
        }
        _state.update { it.copy(isRecordingAudio = true, error = null) }
    }

    fun stopAudioRecording() {
        val result = audioRecorder.stop()
        _state.update { it.copy(isRecordingAudio = false) }
        if (result == null) {
            _state.update { it.copy(error = "Recording was empty — try again") }
            return
        }
        val (file, durationMs) = result
        val att = com.nexos.ai.domain.model.NoteAttachment.Audio(
            id = "audio-${System.currentTimeMillis()}",
            filePath = file.absolutePath,
            durationMs = durationMs
        )
        _state.update { it.copy(attachments = it.attachments + att) }
    }

    fun cancelAudioRecording() {
        audioRecorder.cancel()
        _state.update { it.copy(isRecordingAudio = false) }
    }

    fun attachCurrentLocation() {
        if (_state.value.isAttachingLocation) return
        if (!locationProvider.hasPermission()) {
            _state.update { it.copy(error = "Grant location permission to attach where you are") }
            return
        }
        _state.update { it.copy(isAttachingLocation = true, error = null) }
        viewModelScope.launch {
            val loc = locationProvider.currentOrLast()
            if (loc == null) {
                _state.update { it.copy(isAttachingLocation = false, error = "Couldn't read your location right now") }
                return@launch
            }
            // Best-effort reverse geocode via Nominatim. Falls back to coords when the network
            // call fails — the attachment still works, just with a less friendly label.
            val labelResult = parseLocationLink("geo:${loc.latitude},${loc.longitude}")
            val label = labelResult.getOrNull()?.address
                ?: "%.4f, %.4f".format(loc.latitude, loc.longitude)
            val att = com.nexos.ai.domain.model.NoteAttachment.Location(
                id = "loc-${System.currentTimeMillis()}",
                latitude = loc.latitude,
                longitude = loc.longitude,
                label = label
            )
            _state.update {
                it.copy(isAttachingLocation = false, attachments = it.attachments + att)
            }
        }
    }

    fun removeAttachment(id: String) {
        _state.update { s -> s.copy(attachments = s.attachments.filterNot { it.id == id }) }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun onTagsChange(value: String) {
        _state.update { it.copy(tags = value) }
    }

    fun save(onSaved: (Long) -> Unit) {
        val current = _state.value
        if (current.title.isBlank() && current.content.isBlank() && current.attachments.isEmpty()) {
            _state.update { it.copy(error = "Nothing to save") }
            onSaved(-1)
            return
        }
        val attachmentsJson = com.nexos.ai.data.local.NoteAttachmentCodec.encode(current.attachments)
        viewModelScope.launch {
            if (noteId > 0) {
                val existing = repository.getNote(noteId) ?: return@launch
                repository.update(
                    existing.copy(
                        title = current.title.ifBlank { "Untitled note" },
                        content = current.content,
                        tags = current.tags,
                        attachmentsJson = attachmentsJson,
                        backgroundId = current.backgroundId,
                        textAlignment = current.textAlignment,
                        bodyTextSizeSp = current.bodyTextSizeSp
                    )
                )
                onSaved(noteId)
            } else {
                val saved = saveManualNote(
                    title = current.title,
                    content = current.content,
                    tags = current.tags
                ) ?: run {
                    onSaved(-1L)
                    return@launch
                }
                // Manual-save path can't carry the v5 fields; write them back via update.
                val needsExtras = current.attachments.isNotEmpty() ||
                    current.backgroundId > 0 ||
                    current.textAlignment != 0 ||
                    current.bodyTextSizeSp != 16
                if (needsExtras) {
                    repository.update(saved.copy(
                        attachmentsJson = attachmentsJson,
                        backgroundId = current.backgroundId,
                        textAlignment = current.textAlignment,
                        bodyTextSizeSp = current.bodyTextSizeSp
                    ))
                }
                onSaved(saved.id)
            }
        }
    }
}

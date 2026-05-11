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
    val error: String? = null
)

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val saveManualNote: SaveManualNoteUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: -1L

    private val _state = MutableStateFlow(EditNoteState(isLoading = noteId > 0, isExistingNote = noteId > 0))
    val state: StateFlow<EditNoteState> = _state.asStateFlow()

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
                        isExistingNote = note != null
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun onContentChange(value: String) {
        _state.update { it.copy(content = value) }
    }

    fun onTagsChange(value: String) {
        _state.update { it.copy(tags = value) }
    }

    fun save(onSaved: (Long) -> Unit) {
        val current = _state.value
        if (current.title.isBlank() && current.content.isBlank()) {
            _state.update { it.copy(error = "Nothing to save") }
            onSaved(-1)
            return
        }
        viewModelScope.launch {
            if (noteId > 0) {
                val existing = repository.getNote(noteId) ?: return@launch
                repository.update(
                    existing.copy(
                        title = current.title.ifBlank { "Untitled note" },
                        content = current.content,
                        tags = current.tags
                    )
                )
                onSaved(noteId)
            } else {
                val saved = saveManualNote(
                    title = current.title,
                    content = current.content,
                    tags = current.tags
                )
                onSaved(saved?.id ?: -1L)
            }
        }
    }
}

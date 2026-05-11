package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.domain.model.Note
import com.nexos.ai.domain.model.SourceType
import com.nexos.ai.orchestrator.NexosOrchestrator
import com.nexos.ai.presentation.navigation.NexosDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val orchestrator: NexosOrchestrator
) : ViewModel() {

    private val noteId: Long = savedStateHandle[NexosDestinations.ARG_NOTE_ID] ?: NexosDestinations.NEW_NOTE_ID

    val isNewNote: Boolean get() = noteId == NexosDestinations.NEW_NOTE_ID

    private val _form = MutableStateFlow(Form())
    val form: StateFlow<Form> = _form.asStateFlow()

    init {
        if (!isNewNote) {
            viewModelScope.launch {
                val existing = noteRepository.getNote(noteId)
                if (existing != null) {
                    _form.value = Form(
                        title = existing.title,
                        content = existing.content,
                        tagsCsv = existing.tags.joinToString(", ")
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _form.update { it.copy(title = value) }
    fun onContentChange(value: String) = _form.update { it.copy(content = value) }
    fun onTagsChange(value: String) = _form.update { it.copy(tagsCsv = value) }

    fun save(onSaved: () -> Unit) {
        val current = _form.value
        if (current.content.isBlank() && current.title.isBlank()) {
            onSaved()
            return
        }
        viewModelScope.launch {
            val tags = current.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (isNewNote) {
                orchestrator.saveManualNote(
                    title = current.title.trim(),
                    content = current.content.trim(),
                    tags = tags
                )
            } else {
                noteRepository.updateNote(
                    Note(
                        id = noteId,
                        title = current.title.trim().ifBlank { "Untitled" },
                        content = current.content.trim(),
                        sourceType = SourceType.Manual,
                        tags = tags,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            onSaved()
        }
    }

    data class Form(
        val title: String = "",
        val content: String = "",
        val tagsCsv: String = ""
    )
}

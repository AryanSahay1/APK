package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.local.NotebookCoverCodec
import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.domain.model.NotebookCover
import com.nexos.ai.util.NotebookPdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the notebook cover/back-page designer + PDF export.
 *
 * State machine:
 *   - `noteId` arg points at *any* Note. If it's not already flagged isNotebook, this VM will
 *     flag it as the cover when the user hits 'Mark complete'.
 *   - Pages = every Note row where notebookId == this noteId. The user attaches existing
 *     notes to the notebook through the [attachPage] / [detachPage] calls.
 *   - On 'Save and export', the cover row's coverDesignJson + isNotebookCompleted are
 *     persisted, then the exporter is invoked and the resulting URI surfaces in [exportUri].
 */
data class NotebookUiState(
    val cover: Note? = null,
    val pages: List<Note> = emptyList(),
    val design: NotebookCover = NotebookCover(),
    val isExporting: Boolean = false,
    val exportUri: String? = null,
    val errorMessage: String? = null,
    val availableNotes: List<Note> = emptyList()
)

@HiltViewModel
class NotebookViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val exporter: NotebookPdfExporter,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: -1L

    private val _state = MutableStateFlow(NotebookUiState())
    val state: StateFlow<NotebookUiState> = _state.asStateFlow()

    init { viewModelScope.launch { reload() } }

    private suspend fun reload() {
        val cover = repository.getNote(noteId) ?: return
        val design = NotebookCoverCodec.decode(cover.coverDesignJson)
        val pages = repository.getNotebookPages(noteId)
        val allNotes = repository.allNotes.first()
        _state.update {
            it.copy(
                cover = cover,
                design = design,
                pages = pages,
                availableNotes = allNotes.filter { n ->
                    n.id != cover.id && n.notebookId == 0L && !n.isNotebook
                }
            )
        }
    }

    fun updateDesign(transform: (NotebookCover) -> NotebookCover) {
        _state.update { it.copy(design = transform(it.design)) }
    }

    fun attachPage(page: Note) {
        viewModelScope.launch {
            repository.update(page.copy(notebookId = noteId))
            reload()
        }
    }

    fun detachPage(page: Note) {
        viewModelScope.launch {
            repository.update(page.copy(notebookId = 0L))
            reload()
        }
    }

    fun saveDesign() {
        val current = _state.value.cover ?: return
        viewModelScope.launch {
            repository.update(
                current.copy(
                    isNotebook = true,
                    coverDesignJson = NotebookCoverCodec.encode(_state.value.design)
                )
            )
            reload()
        }
    }

    fun markComplete() {
        val current = _state.value.cover ?: return
        viewModelScope.launch {
            repository.update(
                current.copy(
                    isNotebook = true,
                    isNotebookCompleted = true,
                    coverDesignJson = NotebookCoverCodec.encode(_state.value.design)
                )
            )
            reload()
        }
    }

    fun exportPdf() {
        val current = _state.value.cover ?: return
        if (_state.value.isExporting) return
        _state.update { it.copy(isExporting = true, errorMessage = null, exportUri = null) }
        viewModelScope.launch {
            val saved = current.copy(
                isNotebook = true,
                coverDesignJson = NotebookCoverCodec.encode(_state.value.design)
            )
            repository.update(saved)
            val result = exporter.export(context, saved, _state.value.pages)
            _state.update {
                if (result.isSuccess) {
                    it.copy(isExporting = false, exportUri = result.getOrNull()?.toString())
                } else {
                    it.copy(isExporting = false, errorMessage = result.exceptionOrNull()?.message
                        ?: "PDF export failed")
                }
            }
        }
    }

    fun consumeExportUri() { _state.update { it.copy(exportUri = null) } }
    fun consumeError() { _state.update { it.copy(errorMessage = null) } }
}

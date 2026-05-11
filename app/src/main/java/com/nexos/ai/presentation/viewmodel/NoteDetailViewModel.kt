package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.domain.model.Note
import com.nexos.ai.presentation.navigation.NexosDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val noteId: Long = savedStateHandle[NexosDestinations.ARG_NOTE_ID] ?: -1L

    val note: StateFlow<Note?> = noteRepository.observeNote(noteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            if (noteId > 0) noteRepository.deleteNote(noteId)
            onDeleted()
        }
    }
}

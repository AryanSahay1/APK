package com.nexos.ai.domain.usecase

import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Reactive note list. Returns a Flow so the UI updates instantly on insert/delete (Law 2).
 */
class GetAllNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(query: String = ""): Flow<List<Note>> =
        if (query.isBlank()) repository.allNotes else repository.searchNotes(query)
}

package com.nexos.ai.data.repository

import com.nexos.ai.data.local.dao.NoteDao
import com.nexos.ai.data.mapper.toDomain
import com.nexos.ai.data.mapper.toEntity
import com.nexos.ai.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges the rest of the app to Room. All writes route through [Dispatchers.IO];
 * reads are exposed as cold [Flow]s and let Room itself dispatch.
 */
@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes().map { rows -> rows.map { it.toDomain() } }

    fun observeNote(id: Long): Flow<Note?> =
        noteDao.observeNoteById(id).map { it?.toDomain() }

    fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query.trim()).map { rows -> rows.map { it.toDomain() } }

    fun recentNotes(limit: Int): Flow<List<Note>> =
        noteDao.getRecentNotes(limit).map { rows -> rows.map { it.toDomain() } }

    suspend fun insertNote(note: Note): Long = withContext(Dispatchers.IO) {
        noteDao.insert(note.toEntity())
    }

    suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.update(note.toEntity())
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteById(id)
    }

    suspend fun getNote(id: Long): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)?.toDomain()
    }

    suspend fun count(): Int = withContext(Dispatchers.IO) { noteDao.getNoteCount() }
}

package com.nexos.ai.data.repository

import com.nexos.ai.data.local.dao.NoteDao
import com.nexos.ai.data.local.entity.Note
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO


    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query.trim())

    fun observeNoteById(id: Long): Flow<Note?> = noteDao.observeNoteById(id)

    suspend fun getNote(id: Long): Note? = withContext(ioDispatcher) { noteDao.getNoteById(id) }

    suspend fun insert(note: Note): Long = withContext(ioDispatcher) { noteDao.insert(note) }

    suspend fun update(note: Note) = withContext(ioDispatcher) { noteDao.update(note) }

    suspend fun delete(note: Note) = withContext(ioDispatcher) { noteDao.delete(note) }

    suspend fun deleteById(id: Long) = withContext(ioDispatcher) { noteDao.deleteById(id) }

    suspend fun count(): Int = withContext(ioDispatcher) { noteDao.getNoteCount() }

    val notebooks: Flow<List<Note>> = noteDao.observeNotebooks()

    fun observeNotebookPages(coverId: Long): Flow<List<Note>> = noteDao.observeNotebookPages(coverId)

    suspend fun getNotebookPages(coverId: Long): List<Note> =
        withContext(ioDispatcher) { noteDao.getNotebookPages(coverId) }
}

package com.nexos.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nexos.ai.data.local.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notes")
    suspend fun clearAll()

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): Note?

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeNoteById(id: Long): Flow<Note?>

    @Query(
        "SELECT * FROM notes WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' " +
            "OR LOWER(content) LIKE '%' || LOWER(:query) || '%' " +
            "OR LOWER(summary) LIKE '%' || LOWER(:query) || '%' " +
            "ORDER BY timestamp DESC"
    )
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentNotes(limit: Int): Flow<List<Note>>

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNoteCount(): Int

    @Query("SELECT * FROM notes WHERE isNotebook = 1 ORDER BY timestamp DESC")
    fun observeNotebooks(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE notebookId = :coverId ORDER BY timestamp ASC")
    suspend fun getNotebookPages(coverId: Long): List<Note>

    @Query("SELECT * FROM notes WHERE notebookId = :coverId ORDER BY timestamp ASC")
    fun observeNotebookPages(coverId: Long): Flow<List<Note>>
}

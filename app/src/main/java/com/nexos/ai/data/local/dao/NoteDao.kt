package com.nexos.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nexos.ai.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO. Every read returns [Flow] so UI updates automatically when the
 * underlying table changes; every write is a `suspend` function callable
 * only from a coroutine.
 */
@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeNoteById(id: Long): Flow<NoteEntity?>

    @Query(
        "SELECT * FROM notes WHERE " +
            "title LIKE '%' || :query || '%' OR " +
            "content LIKE '%' || :query || '%' OR " +
            "summary LIKE '%' || :query || '%' " +
            "ORDER BY timestamp DESC"
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentNotes(limit: Int): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNoteCount(): Int
}

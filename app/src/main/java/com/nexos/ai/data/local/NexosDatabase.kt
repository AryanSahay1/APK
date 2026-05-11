package com.nexos.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nexos.ai.data.local.dao.NoteDao
import com.nexos.ai.data.local.entity.NoteEntity

/**
 * The single Room database for NexOS. Provided as a Hilt @Singleton — never
 * instantiate manually outside the DI graph or the database file may corrupt.
 */
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NexosDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val DB_NAME = "nexos_database"
    }
}

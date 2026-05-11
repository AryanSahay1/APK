package com.nexos.ai.di

import android.content.Context
import androidx.room.Room
import com.nexos.ai.data.local.NexosDatabase
import com.nexos.ai.data.local.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the singleton Room database and its DAOs.
 * `fallbackToDestructiveMigration()` is intentionally not used — schema
 * changes must ship a real [androidx.room.migration.Migration].
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NexosDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            NexosDatabase::class.java,
            NexosDatabase.DB_NAME
        ).build()

    @Provides
    @Singleton
    fun provideNoteDao(database: NexosDatabase): NoteDao = database.noteDao()
}

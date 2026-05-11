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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NexosDatabase =
        Room.databaseBuilder(
            context,
            NexosDatabase::class.java,
            NexosDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideNoteDao(database: NexosDatabase): NoteDao = database.noteDao()
}

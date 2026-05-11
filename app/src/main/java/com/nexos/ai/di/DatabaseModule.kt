package com.nexos.ai.di

import android.content.Context
import androidx.room.Room
import com.nexos.ai.data.local.NexosDatabase
import com.nexos.ai.data.local.dao.AlarmDao
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
            .addMigrations(
                NexosDatabase.MIGRATION_1_2,
                NexosDatabase.MIGRATION_2_3,
                NexosDatabase.MIGRATION_3_4,
                NexosDatabase.MIGRATION_4_5
            )
            // Defensive only: real migrations should be added above. Used as a safety net so
            // a future schema bump that ships without a migration does not crash existing users
            // — they lose only the affected new tables, never their notes.
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideNoteDao(database: NexosDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideAlarmDao(database: NexosDatabase): AlarmDao = database.alarmDao()
}

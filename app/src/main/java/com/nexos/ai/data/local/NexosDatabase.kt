package com.nexos.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nexos.ai.data.local.dao.AlarmDao
import com.nexos.ai.data.local.dao.NoteDao
import com.nexos.ai.data.local.entity.Alarm
import com.nexos.ai.data.local.entity.Note

@Database(
    entities = [Note::class, Alarm::class],
    version = 5,
    exportSchema = false
)
abstract class NexosDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        const val DATABASE_NAME = "nexos.db"

        /**
         * v1 → v2: adds the `alarms` table. Notes table is untouched, so existing user
         * notes survive the upgrade. This is the path Play Store auto-updates will take.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS alarms (
                      id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      title TEXT NOT NULL,
                      rawRequest TEXT NOT NULL,
                      triggerAt INTEGER NOT NULL,
                      isEnabled INTEGER NOT NULL DEFAULT 1,
                      isFired INTEGER NOT NULL DEFAULT 0,
                      createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * v2 → v3: add the recurDaysMask column to support the new in-built alarm-clock UI
         * (time + days-of-week chips). Default 0 = "fire once and stop", matching existing
         * natural-language reminder behaviour.
         */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN recurDaysMask INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * v3 → v4: adds the notebook + attachments columns to the existing notes table. All
         * defaults are no-ops for pre-v4 notes; existing data flows through unchanged.
         */
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN attachmentsJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE notes ADD COLUMN isNotebook INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN notebookId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN coverDesignJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE notes ADD COLUMN isNotebookCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * v4 → v5: per-note background + simple body presentation settings introduced for
         * the Vivo-style notepad redesign in v1.5. All defaults are no-ops; pre-v5 notes
         * render exactly as before (backgroundId=0 means "no background").
         */
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN backgroundId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN textAlignment INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN bodyTextSizeSp INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}

package com.nexos.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room persistence model for a NexOS note.
 *
 * Source of truth for the schema is `SKILL.md §9 Module 01`. The `summary`
 * field is empty when no AI provider is configured; `sourceType` is one of
 * "screenshot" | "voice" | "manual"; `rawImagePath` points to the cache
 * file containing the original screenshot bitmap if applicable.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val summary: String = "",
    val sourceType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tags: String = "",
    val isSynced: Boolean = false,
    val rawImagePath: String = ""
) {
    companion object {
        const val SOURCE_SCREENSHOT = "screenshot"
        const val SOURCE_VOICE = "voice"
        const val SOURCE_MANUAL = "manual"
    }
}

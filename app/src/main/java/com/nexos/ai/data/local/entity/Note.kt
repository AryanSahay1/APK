package com.nexos.ai.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a stored note. Single table for the MVP — all sources (screenshot, voice,
 * manual) live here so search and listing stay simple.
 *
 * Note: this is also the surface model returned to the UI. We deliberately do not split entity
 * vs domain in the MVP — the table mirrors the conceptual note 1:1, no transformations needed.
 * If we add server sync later, we'll introduce a separate domain model.
 */
@Immutable
@Entity(tableName = "notes")
data class Note(
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
    val tagList: List<String>
        get() = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

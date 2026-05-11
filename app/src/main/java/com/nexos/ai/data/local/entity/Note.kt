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
    val rawImagePath: String = "",
    /**
     * JSON-encoded list of [com.nexos.ai.domain.model.NoteAttachment]. Stored as a string
     * column rather than a separate join table so the Note row is self-contained and
     * Room v3 → v4 migration is just an ADD COLUMN.
     */
    val attachmentsJson: String = "",
    /**
     * When true, this Note is the cover of a multi-page notebook the user is building. The
     * notebook's individual pages are stored as ordinary Notes with this row's `id` in
     * their [notebookId] field. When the user marks the notebook complete, they design a
     * cover/back page in [coverDesignJson] and can export the whole thing as a PDF.
     */
    val isNotebook: Boolean = false,
    /** Foreign key to the cover note when this row is a page inside a notebook (0 = none). */
    val notebookId: Long = 0L,
    /** JSON-encoded cover + back page design (background, title, motif). */
    val coverDesignJson: String = "",
    /** True once the user has marked the notebook 'finished' — locks editing of pages. */
    val isNotebookCompleted: Boolean = false,
    /**
     * Identifier of the panda-themed background applied to this note (0 = no background,
     * uses the default theme surface). See [com.nexos.ai.presentation.ui.components
     * .PandaBackgrounds] for the catalogue of 1..30.
     */
    val backgroundId: Int = 0,
    /**
     * Text alignment of the note body: 0 = start, 1 = center, 2 = end. Applied uniformly
     * across the body — paragraph-level alignment is out of scope.
     */
    val textAlignment: Int = 0,
    /**
     * Default body text size in `sp`. Range clamped to 12..28 in the editor. Falls back
     * to MaterialTheme.typography.bodyLarge.fontSize when 0.
     */
    val bodyTextSizeSp: Int = 0
) {
    val tagList: List<String>
        get() = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

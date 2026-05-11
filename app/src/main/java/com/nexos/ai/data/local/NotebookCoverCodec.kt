package com.nexos.ai.data.local

import com.google.gson.Gson
import com.nexos.ai.domain.model.NotebookCover

/**
 * Tiny Gson wrapper for NotebookCover. Standalone (not part of NoteAttachmentCodec) because
 * the cover is a single object, not a list — Gson handles that case fine without our manual
 * polymorphism dance.
 */
object NotebookCoverCodec {
    private val gson = Gson()

    fun encode(cover: NotebookCover): String = gson.toJson(cover)

    fun decode(json: String): NotebookCover {
        if (json.isBlank()) return NotebookCover()
        return runCatching { gson.fromJson(json, NotebookCover::class.java) }
            .getOrNull() ?: NotebookCover()
    }
}

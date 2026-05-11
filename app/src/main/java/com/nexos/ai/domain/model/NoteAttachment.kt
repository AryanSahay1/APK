package com.nexos.ai.domain.model

import androidx.compose.runtime.Immutable

/**
 * Anything a user can attach to a note alongside the text body. Stored as a JSON-encoded
 * list inside Note.attachmentsJson so we don't have to bump Room with a separate join table
 * (the relationship is strictly per-note, no cross-note sharing).
 *
 * Per attachment type:
 *   * [Image] — content:// URI persisted via ContentResolver.takePersistableUriPermission
 *               so the app can re-read it across launches. We never copy the bytes — the
 *               user's original file in MediaStore is the source of truth.
 *   * [Audio] — recorded voice memo stored in the app's filesDir. We own the bytes; the
 *               note's lifetime owns the file's lifetime.
 *   * [Location] — current device coords + reverse-geocoded label. Captures a point in
 *                  time; the user explicitly attaches it once.
 */
@Immutable
sealed class NoteAttachment {
    abstract val id: String

    @Immutable
    data class Image(
        override val id: String,
        val uri: String,
        val mimeType: String = "image/*",
        val capturedAt: Long = System.currentTimeMillis()
    ) : NoteAttachment()

    @Immutable
    data class Audio(
        override val id: String,
        val filePath: String,
        val durationMs: Long,
        val capturedAt: Long = System.currentTimeMillis()
    ) : NoteAttachment()

    @Immutable
    data class Location(
        override val id: String,
        val latitude: Double,
        val longitude: Double,
        val label: String,
        val capturedAt: Long = System.currentTimeMillis()
    ) : NoteAttachment()
}

package com.nexos.ai.domain.model

import androidx.compose.runtime.Immutable

/**
 * Cover + back-page design for a notebook. Stored as JSON in Note.coverDesignJson.
 *
 * Intentionally simple: the user picks a background color, an accent color, optionally types
 * a custom title (defaults to the note's title), and picks one of a handful of motifs that
 * get drawn behind the title. This lets us render the cover/back without a layout engine —
 * the PDF exporter and the on-screen preview share the same primitives.
 */
@Immutable
data class NotebookCover(
    val backgroundHex: String = "#0F0F14",
    val accentHex: String = "#00E676",
    val titleOverride: String = "",
    val subtitle: String = "",
    val motif: Motif = Motif.PandaLeaf,
    val backNote: String = "End of notebook · made with NexOS"
) {
    enum class Motif(val displayName: String) {
        PandaLeaf("Panda + leaf"),
        Gridlines("Grid lines"),
        Confetti("Panda confetti"),
        Plain("Plain — no motif")
    }
}

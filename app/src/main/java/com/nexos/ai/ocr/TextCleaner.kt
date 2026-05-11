package com.nexos.ai.ocr

/**
 * Post-processes raw OCR output before it is shown to the user or fed to
 * a language model. Removes the typical artefacts ML Kit leaves behind:
 * stray whitespace, repeated blank lines, mojibake-like single-character
 * lines, and trailing punctuation noise.
 *
 * The cleaner preserves a single blank line between paragraphs (collapses
 * runs of 2+ blank lines into exactly one) so downstream prompts retain
 * the document's natural structure.
 */
object TextCleaner {

    private val multiSpaces = Regex(" {2,}")
    private val zeroWidth = Regex("[\\u200B-\\u200D\\uFEFF]")

    fun clean(raw: String): String {
        if (raw.isBlank()) return ""

        val noZeroWidth = raw.replace(zeroWidth, "")
        val rawLines = noZeroWidth.split(Regex("\\r?\\n"))

        // Trim, collapse internal whitespace, and classify each line as either
        // "content" (kept) or "noise" (single non-alphanumeric → dropped).
        val processed = rawLines.map { line ->
            val trimmed = multiSpaces.replace(line.trim(), " ")
            when {
                trimmed.isEmpty() -> ""
                trimmed.length == 1 && !trimmed.first().isLetterOrDigit() -> ""
                else -> trimmed
            }
        }

        // Re-stitch: drop leading/trailing blanks and collapse interior runs
        // of blank lines into exactly one blank line.
        val builder = StringBuilder()
        var pendingBlank = false
        var firstWritten = false
        for (line in processed) {
            if (line.isEmpty()) {
                if (firstWritten) pendingBlank = true
            } else {
                if (firstWritten) {
                    builder.append(if (pendingBlank) "\n\n" else "\n")
                }
                builder.append(line)
                firstWritten = true
                pendingBlank = false
            }
        }
        return builder.toString().trimEnd('\n', ' ')
    }
}

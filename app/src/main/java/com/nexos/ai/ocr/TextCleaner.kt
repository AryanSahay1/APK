package com.nexos.ai.ocr

/**
 * Post-processes raw OCR output before it is shown to the user or fed to
 * a language model. Removes the typical artefacts ML Kit leaves behind:
 * stray whitespace, repeated blank lines, mojibake-like single-character
 * lines, and trailing punctuation noise.
 */
object TextCleaner {

    private val multiBlankLines = Regex("(\\r?\\n){3,}")
    private val multiSpaces = Regex(" {2,}")
    private val zeroWidth = Regex("[\\u200B-\\u200D\\uFEFF]")

    fun clean(raw: String): String {
        if (raw.isBlank()) return ""

        val noZeroWidth = raw.replace(zeroWidth, "")
        val lines = noZeroWidth
            .lineSequence()
            .map { it.trim() }
            .map { multiSpaces.replace(it, " ") }
            .filter { line -> line.length > 1 || line.singleOrNull()?.isLetterOrDigit() == true }
            .toList()

        return lines.joinToString("\n").let { multiBlankLines.replace(it, "\n\n") }.trim()
    }
}

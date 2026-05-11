package com.nexos.ai.ocr

/**
 * Pure-Kotlin OCR post-processor. Idempotent. No Android dependencies.
 *
 * Heuristics:
 * - Collapse runs of horizontal whitespace to a single space.
 * - Collapse 3+ consecutive blank lines to a single blank line.
 * - Trim every line.
 * - Strip stray control characters and zero-width invisibles.
 * - Drop "noise" lines that contain only punctuation or a single character.
 */
object TextCleaner {

    private val controlChars = Regex("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]")
    private val zeroWidth = Regex("[\u200B-\u200D\uFEFF]")
    private val multiSpace = Regex("[ \t\u00A0]+")
    private val multiBlankLines = Regex("\n{3,}")
    // Matches a single line of pure punctuation/decorative chars (1–3 chars). Blank lines pass
    // through to preserve paragraph structure; the multi-blank collapse below handles spacing.
    private val noiseOnly = Regex("^[\\p{Punct}•·\\-_=*]{1,3}\$")

    fun clean(raw: String): String {
        if (raw.isBlank()) return ""

        val deControlled = raw
            .replace(controlChars, "")
            .replace(zeroWidth, "")

        val perLine = deControlled
            .lineSequence()
            .map { it.replace(multiSpace, " ").trim() }
            .filterNot { it.matches(noiseOnly) }
            .joinToString("\n")

        return perLine
            .replace(multiBlankLines, "\n\n")
            .trim()
    }

    /**
     * Produce a short title from cleaned text — first meaningful line, capped at [maxWords] words.
     */
    fun deriveTitle(cleaned: String, maxWords: Int = 8): String {
        val firstLine = cleaned.lineSequence().firstOrNull { it.isNotBlank() } ?: return "Untitled note"
        val words = firstLine.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return "Untitled note"
        return words.take(maxWords).joinToString(" ").trim('.', ',', ':', ';', '·', '•', '-')
            .ifBlank { "Untitled note" }
    }
}

package com.nexos.ai.domain.model

/**
 * Structured output of [com.nexos.ai.ocr.OcrEngine.extractText].
 *
 * - [rawText] preserves the original ML Kit text (newlines intact) for debugging.
 * - [cleanText] is the [com.nexos.ai.ocr.TextCleaner] post-processed string used downstream.
 * - [blocks] is the list of recognised text blocks, useful for layout-aware features later.
 * - [confidence] is a coarse 0..1 score (ML Kit does not expose per-block confidence directly,
 *   so we approximate by block count vs character density).
 */
data class OcrResult(
    val rawText: String,
    val cleanText: String,
    val blocks: List<String>,
    val confidence: Float,
    val isSuccess: Boolean,
    val error: String? = null
) {
    val hasUsableText: Boolean get() = isSuccess && cleanText.isNotBlank()

    companion object {
        fun empty(error: String? = null): OcrResult = OcrResult(
            rawText = "",
            cleanText = "",
            blocks = emptyList(),
            confidence = 0f,
            isSuccess = false,
            error = error
        )
    }
}

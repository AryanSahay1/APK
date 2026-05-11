package com.nexos.ai.domain.model

/**
 * Result of running OCR over a captured bitmap. Both [rawText] and
 * [cleanText] are kept — `rawText` for debugging, `cleanText` for AI
 * prompts and direct note storage.
 */
data class OcrResult(
    val rawText: String,
    val cleanText: String,
    val blocks: List<String>,
    val confidence: Float,
    val isSuccess: Boolean,
    val error: String? = null
) {
    companion object {
        fun failure(message: String) = OcrResult(
            rawText = "",
            cleanText = "",
            blocks = emptyList(),
            confidence = 0f,
            isSuccess = false,
            error = message
        )
    }
}

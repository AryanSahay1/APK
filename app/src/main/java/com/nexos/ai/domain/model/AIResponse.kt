package com.nexos.ai.domain.model

/**
 * The single response envelope returned by every [com.nexos.ai.ai.AIProvider].
 * `isSuccess` MUST be checked before reading `text` — callers fall back to
 * raw OCR / transcript output whenever this is false.
 */
data class AIResponse(
    val text: String,
    val isSuccess: Boolean,
    val error: String? = null,
    val provider: String = "",
    val tokensUsed: Int = 0
) {
    companion object {
        fun failure(provider: String, message: String) =
            AIResponse(text = "", isSuccess = false, error = message, provider = provider)
    }
}

/**
 * Strongly-typed result of parsing the JSON the providers return.
 * Mirrors the contract defined in `SKILL.md §11`.
 */
data class ParsedNote(
    val title: String,
    val bullets: List<String>,
    val summary: String
) {
    fun toContent(): String = bullets.joinToString("\n") { "• $it" }
}

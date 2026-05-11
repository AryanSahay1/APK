package com.nexos.ai.ai

import com.nexos.ai.domain.model.AIResponse

/**
 * The single abstraction over every AI provider supported by NexOS.
 * Implementations live in `ai/providers/` and are routed by [AIRouter].
 *
 * Callers (NoteAIHelper, NexosOrchestrator) never know which provider is
 * active — they just call `complete` and inspect the [AIResponse.isSuccess]
 * flag. That keeps Law 1 ("must work without AI") trivial to enforce.
 */
interface AIProvider {
    val name: String
    val providerKey: String

    suspend fun complete(prompt: String, maxTokens: Int = 800): AIResponse
    suspend fun testConnection(): Boolean
}

/**
 * Stable keys used by the settings screen and SecureStorage. Adding a new
 * provider requires updating [AIProviders.ALL] and [AIRouter].
 */
object AIProviders {
    const val NONE = "none"
    const val OPENAI = "openai"
    const val GEMINI = "gemini"
    const val ANTHROPIC = "anthropic"
    const val GROQ = "groq"

    val ALL = listOf(NONE, GROQ, OPENAI, GEMINI, ANTHROPIC)

    fun displayName(key: String): String = when (key) {
        OPENAI -> "OpenAI"
        GEMINI -> "Google Gemini"
        ANTHROPIC -> "Anthropic Claude"
        GROQ -> "Groq (recommended free)"
        else -> "None (local only)"
    }
}

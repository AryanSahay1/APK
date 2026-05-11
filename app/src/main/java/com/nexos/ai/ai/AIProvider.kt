package com.nexos.ai.ai

import com.nexos.ai.domain.model.AIResponse

/**
 * Single interface that every AI provider implements.
 *
 * Architecture (Layer 4): callers depend only on this contract. The active implementation is
 * resolved by [AIRouter]. This means the orchestrator can be swapped between OpenAI, Gemini,
 * Anthropic, Groq, or [NoOpProvider] without any change to upstream code.
 */
interface AIProvider {
    val name: String
    val providerKey: String
    val isConfigured: Boolean

    suspend fun complete(prompt: String, maxTokens: Int = 800): AIResponse

    suspend fun testConnection(): Boolean
}

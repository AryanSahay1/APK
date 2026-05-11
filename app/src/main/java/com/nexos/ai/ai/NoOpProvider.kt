package com.nexos.ai.ai

import com.nexos.ai.domain.model.AIResponse
import com.nexos.ai.util.Constants

/**
 * Default provider when no API key is configured. Always returns failure so the orchestrator
 * falls back to raw OCR text. This is THE most important provider — Law 1: it must work
 * without AI.
 */
class NoOpProvider : AIProvider {
    override val name: String = "No AI (offline)"
    override val providerKey: String = Constants.PROVIDER_NONE
    override val isConfigured: Boolean = false

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse =
        AIResponse.failure(error = "No AI provider configured", provider = providerKey)

    override suspend fun testConnection(): Boolean = false
}

package com.nexos.ai.ai.providers

import com.nexos.ai.ai.AIProvider
import com.nexos.ai.ai.AIProviders
import com.nexos.ai.domain.model.AIResponse

/**
 * The most important provider in the app: this is what runs when the user
 * has not configured an API key. Always reports failure so the orchestrator
 * falls back to the raw OCR / transcript text and the user still gets a
 * useful note (Law 1 — "must work without AI").
 */
class NoOpProvider : AIProvider {
    override val name: String = "None"
    override val providerKey: String = AIProviders.NONE

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse =
        AIResponse(
            text = "",
            isSuccess = false,
            error = "No AI provider configured",
            provider = providerKey
        )

    override suspend fun testConnection(): Boolean = false
}

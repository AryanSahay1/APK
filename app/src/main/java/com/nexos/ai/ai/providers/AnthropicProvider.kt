package com.nexos.ai.ai.providers

import com.google.gson.JsonParser
import com.nexos.ai.ai.AIProvider
import com.nexos.ai.ai.AIProviders
import com.nexos.ai.domain.model.AIResponse

/**
 * Anthropic Messages provider (model: `claude-haiku-4-5-20251001`).
 *
 *   POST https://api.anthropic.com/v1/messages
 *   Headers: x-api-key: {key}, anthropic-version: 2023-06-01
 */
class AnthropicProvider(private val apiKey: String) : AIProvider {

    override val name: String = "Anthropic Claude"
    override val providerKey: String = AIProviders.ANTHROPIC

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse {
        if (apiKey.isBlank()) return AIResponse.failure(providerKey, "API key missing")

        val escaped = JsonEscape.escape(prompt)
        val body = """
            {
              "model": "claude-haiku-4-5-20251001",
              "max_tokens": $maxTokens,
              "temperature": 0.2,
              "system": "You produce strictly valid JSON. Never wrap output in markdown.",
              "messages": [{"role": "user", "content": "$escaped"}]
            }
        """.trimIndent()

        return HttpProviderUtils.safeJsonPost(
            provider = providerKey,
            url = "https://api.anthropic.com/v1/messages",
            bodyJson = body,
            headers = mapOf(
                "x-api-key" to apiKey,
                "anthropic-version" to "2023-06-01",
                "Content-Type" to "application/json"
            )
        ) { raw ->
            JsonParser.parseString(raw).asJsonObject
                .getAsJsonArray("content")
                ?.firstOrNull()?.asJsonObject
                ?.get("text")?.asString
        }
    }

    override suspend fun testConnection(): Boolean =
        complete("Reply with {\"ok\":true}.", maxTokens = 16).isSuccess
}

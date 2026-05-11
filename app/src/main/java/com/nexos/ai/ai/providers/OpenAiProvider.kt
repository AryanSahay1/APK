package com.nexos.ai.ai.providers

import com.google.gson.JsonParser
import com.nexos.ai.ai.AIProvider
import com.nexos.ai.ai.AIProviders
import com.nexos.ai.domain.model.AIResponse

/**
 * OpenAI Chat Completions provider. Targets `gpt-4o-mini` — the best
 * price/quality balance for users who bring their own key.
 *
 *   POST https://api.openai.com/v1/chat/completions
 *   Authorization: Bearer {key}
 */
class OpenAiProvider(private val apiKey: String) : AIProvider {

    override val name: String = "OpenAI"
    override val providerKey: String = AIProviders.OPENAI

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse {
        if (apiKey.isBlank()) return AIResponse.failure(providerKey, "API key missing")

        val escaped = JsonEscape.escape(prompt)
        val body = """
            {
              "model": "gpt-4o-mini",
              "messages": [
                {"role": "system", "content": "You produce strictly valid JSON. Never wrap output in markdown."},
                {"role": "user", "content": "$escaped"}
              ],
              "max_tokens": $maxTokens,
              "temperature": 0.2,
              "response_format": {"type": "json_object"}
            }
        """.trimIndent()

        return HttpProviderUtils.safeJsonPost(
            provider = providerKey,
            url = "https://api.openai.com/v1/chat/completions",
            bodyJson = body,
            headers = mapOf(
                "Authorization" to "Bearer $apiKey",
                "Content-Type" to "application/json"
            )
        ) { raw ->
            JsonParser.parseString(raw).asJsonObject
                .getAsJsonArray("choices")
                ?.firstOrNull()?.asJsonObject
                ?.getAsJsonObject("message")
                ?.get("content")?.asString
        }
    }

    override suspend fun testConnection(): Boolean =
        complete("Reply with {\"ok\":true}.", maxTokens = 16).isSuccess
}

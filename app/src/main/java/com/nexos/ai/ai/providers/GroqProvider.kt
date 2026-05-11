package com.nexos.ai.ai.providers

import com.google.gson.JsonParser
import com.nexos.ai.ai.AIProvider
import com.nexos.ai.ai.AIProviders
import com.nexos.ai.domain.model.AIResponse

/**
 * Groq provider — recommended starting point (generous free tier and the
 * same wire format as OpenAI, so it's the simplest to bring up).
 *
 *   POST https://api.groq.com/openai/v1/chat/completions
 *   Model: llama3-8b-8192
 */
class GroqProvider(private val apiKey: String) : AIProvider {

    override val name: String = "Groq"
    override val providerKey: String = AIProviders.GROQ

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse {
        if (apiKey.isBlank()) return AIResponse.failure(providerKey, "API key missing")

        val escaped = JsonEscape.escape(prompt)
        val body = """
            {
              "model": "llama3-8b-8192",
              "messages": [
                {"role": "system", "content": "You produce strictly valid JSON. Never wrap output in markdown."},
                {"role": "user", "content": "$escaped"}
              ],
              "max_tokens": $maxTokens,
              "temperature": 0.2
            }
        """.trimIndent()

        return HttpProviderUtils.safeJsonPost(
            provider = providerKey,
            url = "https://api.groq.com/openai/v1/chat/completions",
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

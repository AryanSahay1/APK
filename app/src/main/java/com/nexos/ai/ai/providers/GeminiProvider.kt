package com.nexos.ai.ai.providers

import com.google.gson.JsonParser
import com.nexos.ai.ai.AIProvider
import com.nexos.ai.ai.AIProviders
import com.nexos.ai.domain.model.AIResponse

/**
 * Google Gemini provider (model: `gemini-1.5-flash`).
 *
 *   POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={key}
 *   Auth via query param — Authorization header is not used.
 */
class GeminiProvider(private val apiKey: String) : AIProvider {

    override val name: String = "Google Gemini"
    override val providerKey: String = AIProviders.GEMINI

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse {
        if (apiKey.isBlank()) return AIResponse.failure(providerKey, "API key missing")

        val escaped = JsonEscape.escape(prompt)
        val body = """
            {
              "contents": [{ "parts": [{ "text": "$escaped" }] }],
              "generationConfig": {
                "maxOutputTokens": $maxTokens,
                "temperature": 0.2,
                "responseMimeType": "application/json"
              }
            }
        """.trimIndent()

        return HttpProviderUtils.safeJsonPost(
            provider = providerKey,
            url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey",
            bodyJson = body,
            headers = mapOf("Content-Type" to "application/json")
        ) { raw ->
            JsonParser.parseString(raw).asJsonObject
                .getAsJsonArray("candidates")
                ?.firstOrNull()?.asJsonObject
                ?.getAsJsonObject("content")
                ?.getAsJsonArray("parts")
                ?.firstOrNull()?.asJsonObject
                ?.get("text")?.asString
        }
    }

    override suspend fun testConnection(): Boolean =
        complete("Reply with {\"ok\":true}.", maxTokens = 16).isSuccess
}

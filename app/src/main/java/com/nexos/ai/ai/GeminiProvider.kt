package com.nexos.ai.ai

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nexos.ai.domain.model.AIResponse
import com.nexos.ai.util.Constants
import okhttp3.Request

/**
 * Google Gemini 1.5 Flash — generateContent endpoint.
 * Auth is via ?key= query parameter (Google's pattern, not Bearer header).
 */
class GeminiProvider(private val apiKey: String) : AIProvider {

    override val name: String = "Gemini · 1.5 Flash"
    override val providerKey: String = Constants.PROVIDER_GEMINI
    override val isConfigured: Boolean = apiKey.isNotBlank()

    private val baseUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse {
        if (!isConfigured) return AIResponse.failure("Missing Gemini key", providerKey)

        val body = JsonObject().apply {
            val contents = JsonArray()
            val message = JsonObject()
            val parts = JsonArray()
            parts.add(JsonObject().apply { addProperty("text", prompt) })
            message.add("parts", parts)
            message.addProperty("role", "user")
            contents.add(message)
            add("contents", contents)

            val genConfig = JsonObject().apply {
                addProperty("maxOutputTokens", maxTokens)
                addProperty("temperature", 0.2)
            }
            add("generationConfig", genConfig)
        }.toString()

        // Use the x-goog-api-key header instead of the ?key= query parameter so the API key
        // never appears in URLs — important because OkHttp's logging interceptor records
        // request URLs even at BASIC level.
        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
        return HttpAIClient.execute(providerKey, request, body) { raw ->
            val obj = JsonParser.parseString(raw).asJsonObject
            val candidates = obj.getAsJsonArray("candidates") ?: return@execute ""
            if (candidates.size() == 0) return@execute ""
            val content = candidates[0].asJsonObject.getAsJsonObject("content") ?: return@execute ""
            val parts = content.getAsJsonArray("parts") ?: return@execute ""
            if (parts.size() == 0) return@execute ""
            parts[0].asJsonObject.get("text")?.asString.orEmpty()
        }
    }

    override suspend fun testConnection(): Boolean =
        complete("Reply with the single word: OK", maxTokens = 16).isSuccess
}

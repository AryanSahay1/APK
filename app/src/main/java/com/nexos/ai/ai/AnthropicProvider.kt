package com.nexos.ai.ai

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nexos.ai.domain.model.AIResponse
import com.nexos.ai.util.Constants
import okhttp3.Request

/**
 * Anthropic Messages API.
 * Default model: claude-haiku-4-5 (latest Haiku tier per architecture doc).
 */
class AnthropicProvider(private val apiKey: String) : AIProvider {

    override val name: String = "Anthropic · Claude Haiku 4.5"
    override val providerKey: String = Constants.PROVIDER_ANTHROPIC
    override val isConfigured: Boolean = apiKey.isNotBlank()

    private val endpoint = "https://api.anthropic.com/v1/messages"
    private val model = "claude-haiku-4-5-20251001"

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse {
        if (!isConfigured) return AIResponse.failure("Missing Anthropic key", providerKey)

        val body = JsonObject().apply {
            addProperty("model", model)
            addProperty("max_tokens", maxTokens)
            addProperty("temperature", 0.2)
            val messages = JsonArray()
            val message = JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", prompt)
            }
            messages.add(message)
            add("messages", messages)
        }.toString()

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")

        return HttpAIClient.execute(providerKey, request, body) { raw ->
            val obj = JsonParser.parseString(raw).asJsonObject
            val content = obj.getAsJsonArray("content") ?: return@execute ""
            if (content.size() == 0) return@execute ""
            content[0].asJsonObject.get("text")?.asString.orEmpty()
        }
    }

    override suspend fun testConnection(): Boolean =
        complete("Reply with the single word: OK", maxTokens = 16).isSuccess
}

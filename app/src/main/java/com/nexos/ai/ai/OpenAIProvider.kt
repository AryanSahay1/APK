package com.nexos.ai.ai

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nexos.ai.domain.model.AIResponse
import com.nexos.ai.util.Constants
import okhttp3.Request

/**
 * OpenAI Chat Completions provider. Model: gpt-4o-mini.
 *
 * Uses the same wire format as Groq, so [GroqProvider] reuses the OpenAI body builder below.
 */
class OpenAIProvider(private val apiKey: String) : AIProvider {

    override val name: String = "OpenAI · gpt-4o-mini"
    override val providerKey: String = Constants.PROVIDER_OPENAI
    override val isConfigured: Boolean = apiKey.isNotBlank()

    private val endpoint = "https://api.openai.com/v1/chat/completions"
    private val model = "gpt-4o-mini"

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse {
        if (!isConfigured) return AIResponse.failure("Missing OpenAI key", providerKey)
        val body = buildOpenAiBody(model, prompt, maxTokens)
        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
        return HttpAIClient.execute(providerKey, request, body) { raw ->
            extractOpenAiText(raw)
        }
    }

    override suspend fun testConnection(): Boolean =
        complete("Reply with the single word: OK", maxTokens = 16).isSuccess

    companion object {
        fun buildOpenAiBody(model: String, prompt: String, maxTokens: Int): String {
            val root = JsonObject()
            root.addProperty("model", model)
            root.addProperty("temperature", 0.2)
            root.addProperty("max_tokens", maxTokens)
            val messages = com.google.gson.JsonArray()
            val msg = JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", prompt)
            }
            messages.add(msg)
            root.add("messages", messages)
            return root.toString()
        }

        fun extractOpenAiText(raw: String): String {
            val obj = JsonParser.parseString(raw).asJsonObject
            val choices = obj.getAsJsonArray("choices") ?: return ""
            if (choices.size() == 0) return ""
            val first = choices[0].asJsonObject
            val message = first.getAsJsonObject("message") ?: return ""
            return message.get("content")?.asString.orEmpty()
        }
    }
}

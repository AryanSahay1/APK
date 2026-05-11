package com.nexos.ai.ai

import com.nexos.ai.domain.model.AIResponse
import com.nexos.ai.util.Constants
import okhttp3.Request

/**
 * Groq Cloud — OpenAI-compatible Chat Completions endpoint.
 * Default model: llama3-8b-8192 (fast, generous free tier).
 *
 * Recommended starting provider for development per architecture document.
 */
class GroqProvider(private val apiKey: String) : AIProvider {

    override val name: String = "Groq · llama3-8b-8192"
    override val providerKey: String = Constants.PROVIDER_GROQ
    override val isConfigured: Boolean = apiKey.isNotBlank()

    private val endpoint = "https://api.groq.com/openai/v1/chat/completions"
    private val model = "llama3-8b-8192"

    override suspend fun complete(prompt: String, maxTokens: Int): AIResponse {
        if (!isConfigured) return AIResponse.failure("Missing Groq key", providerKey)
        val body = OpenAIProvider.buildOpenAiBody(model, prompt, maxTokens)
        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
        return HttpAIClient.execute(providerKey, request, body) { raw ->
            OpenAIProvider.extractOpenAiText(raw)
        }
    }

    override suspend fun testConnection(): Boolean =
        complete("Reply with the single word: OK", maxTokens = 16).isSuccess
}

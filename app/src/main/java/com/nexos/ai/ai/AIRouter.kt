package com.nexos.ai.ai

import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.util.Constants
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the active [AIProvider] from settings + SecureStorage.
 *
 * Returns [NoOpProvider] when:
 * - The user has selected "none"
 * - No API key is stored for the selected provider
 *
 * Callers must always call [getActive] right before use — the user can change providers
 * from Settings at any time.
 */
@Singleton
class AIRouter @Inject constructor(
    private val secureStorage: SecureStorage,
    private val settings: SettingsRepository
) {

    private val noOp = NoOpProvider()

    suspend fun getActive(): AIProvider {
        val key = settings.aiProvider.first()
        return resolve(key)
    }

    fun resolve(providerKey: String): AIProvider {
        val apiKey = secureStorage.getApiKey(providerKey).orEmpty()
        return when (providerKey) {
            Constants.PROVIDER_OPENAI -> if (apiKey.isNotBlank()) OpenAIProvider(apiKey) else noOp
            Constants.PROVIDER_GEMINI -> if (apiKey.isNotBlank()) GeminiProvider(apiKey) else noOp
            Constants.PROVIDER_ANTHROPIC -> if (apiKey.isNotBlank()) AnthropicProvider(apiKey) else noOp
            Constants.PROVIDER_GROQ -> if (apiKey.isNotBlank()) GroqProvider(apiKey) else noOp
            else -> noOp
        }
    }

    suspend fun isAiEnabled(): Boolean = getActive().isConfigured

    /** Returns a provider instance for a key + raw apiKey (used by Settings to test connections). */
    fun resolveWithKey(providerKey: String, apiKey: String): AIProvider {
        if (apiKey.isBlank()) return noOp
        return when (providerKey) {
            Constants.PROVIDER_OPENAI -> OpenAIProvider(apiKey)
            Constants.PROVIDER_GEMINI -> GeminiProvider(apiKey)
            Constants.PROVIDER_ANTHROPIC -> AnthropicProvider(apiKey)
            Constants.PROVIDER_GROQ -> GroqProvider(apiKey)
            else -> noOp
        }
    }

    fun availableProviders(): List<ProviderInfo> = listOf(
        ProviderInfo(Constants.PROVIDER_NONE, "No AI (offline)", "Save raw OCR / transcripts. No network calls.", configurable = false),
        ProviderInfo(Constants.PROVIDER_GROQ, "Groq · llama3-8b-8192", "Fast, free tier. Easiest starting point.", configurable = true),
        ProviderInfo(Constants.PROVIDER_OPENAI, "OpenAI · gpt-4o-mini", "Best balance of cost and quality.", configurable = true),
        ProviderInfo(Constants.PROVIDER_GEMINI, "Gemini · 1.5 Flash", "Google's fast model. Cheap.", configurable = true),
        ProviderInfo(Constants.PROVIDER_ANTHROPIC, "Anthropic · Claude Haiku", "Anthropic's fastest tier.", configurable = true)
    )

    data class ProviderInfo(
        val key: String,
        val displayName: String,
        val description: String,
        val configurable: Boolean
    )
}

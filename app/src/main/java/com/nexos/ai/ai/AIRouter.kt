package com.nexos.ai.ai

import com.nexos.ai.ai.providers.AnthropicProvider
import com.nexos.ai.ai.providers.GeminiProvider
import com.nexos.ai.ai.providers.GroqProvider
import com.nexos.ai.ai.providers.NoOpProvider
import com.nexos.ai.ai.providers.OpenAiProvider
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.data.secure.SecureStorage
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Picks the active [AIProvider] based on the user's saved provider preference
 * and the API key stored for that provider. Returns [NoOpProvider] whenever
 * the preference is `none` or the key is missing — that is what gives NexOS
 * its "works without AI" guarantee (Law 1).
 *
 * Providers are instantiated on demand to keep cold-start cheap. Each call
 * to [getActiveProvider] re-reads the key so revoked / rotated keys take
 * effect immediately.
 */
@Singleton
class AIRouter @Inject constructor(
    private val secureStorage: SecureStorage,
    private val settingsRepository: SettingsRepository
) {

    suspend fun isAiEnabled(): Boolean {
        val provider = settingsRepository.aiProvider.first()
        if (provider == AIProviders.NONE) return false
        return secureStorage.hasApiKey(provider)
    }

    suspend fun getActiveProvider(): AIProvider {
        val providerKey = settingsRepository.aiProvider.first()
        if (providerKey == AIProviders.NONE) return NoOpProvider()
        val apiKey = secureStorage.getApiKey(providerKey).orEmpty()
        if (apiKey.isBlank()) return NoOpProvider()
        return buildProvider(providerKey, apiKey)
    }

    fun providerFor(key: String, apiKey: String): AIProvider =
        if (apiKey.isBlank()) NoOpProvider() else buildProvider(key, apiKey)

    private fun buildProvider(key: String, apiKey: String): AIProvider = when (key) {
        AIProviders.OPENAI -> OpenAiProvider(apiKey)
        AIProviders.GROQ -> GroqProvider(apiKey)
        AIProviders.GEMINI -> GeminiProvider(apiKey)
        AIProviders.ANTHROPIC -> AnthropicProvider(apiKey)
        else -> NoOpProvider()
    }
}

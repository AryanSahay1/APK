package com.nexos.ai.ai.providers

import com.nexos.ai.domain.model.AIResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Shared OkHttp client and request helpers used by every HTTP-backed
 * [com.nexos.ai.ai.AIProvider]. All calls are wrapped in `withTimeout(30s)`
 * per SKILL.md §11.
 */
internal object HttpProviderUtils {

    val JSON = "application/json; charset=utf-8".toMediaType()

    private const val NETWORK_TIMEOUT_S = 30L
    const val OVERALL_TIMEOUT_MS = 30_000L

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(NETWORK_TIMEOUT_S, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_S, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_S, TimeUnit.SECONDS)
            .build()
    }

    suspend fun safeJsonPost(
        provider: String,
        url: String,
        bodyJson: String,
        headers: Map<String, String>,
        extract: (String) -> String?
    ): AIResponse = try {
        withTimeout(OVERALL_TIMEOUT_MS) {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(url)
                    .post(bodyJson.toRequestBody(JSON))
                    .apply { headers.forEach { (k, v) -> header(k, v) } }
                    .build()

                client.newCall(request).execute().use { resp ->
                    parseResponse(provider, resp, extract)
                }
            }
        }
    } catch (e: TimeoutCancellationException) {
        AIResponse.failure(provider, "Request timed out")
    } catch (e: IOException) {
        AIResponse.failure(provider, "Network error: ${e.localizedMessage ?: "unknown"}")
    } catch (e: Exception) {
        AIResponse.failure(provider, "Unexpected error: ${e.localizedMessage ?: e.javaClass.simpleName}")
    }

    private fun parseResponse(
        provider: String,
        response: Response,
        extract: (String) -> String?
    ): AIResponse {
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            return AIResponse.failure(provider, "HTTP ${response.code}: ${response.message}")
        }
        val text = extract(body) ?: return AIResponse.failure(
            provider,
            "Could not parse provider response"
        )
        return AIResponse(text = text, isSuccess = true, provider = provider)
    }
}

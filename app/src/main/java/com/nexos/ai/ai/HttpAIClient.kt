package com.nexos.ai.ai

import android.util.Log
import com.nexos.ai.BuildConfig
import com.nexos.ai.domain.model.AIResponse
import com.nexos.ai.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Internal helper shared by every HTTP-backed AI provider. Centralises:
 * - OkHttp client construction (debug logging only)
 * - Timeout enforcement (Law 2 / Layer 4 — 30s max)
 * - Uniform error → [AIResponse] mapping
 */
internal object HttpAIClient {

    private const val TAG = "NexOS/HttpAIClient"
    val jsonMedia = "application/json".toMediaType()

    val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor { msg -> Log.d(TAG, msg) }.apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                }
            }
            .build()
    }

    suspend fun execute(
        provider: String,
        builder: Request.Builder,
        bodyJson: String?,
        parse: (String) -> String
    ): AIResponse = withContext(Dispatchers.IO) {
        try {
            withTimeout(Constants.AI_REQUEST_TIMEOUT_MS) {
                if (bodyJson != null) {
                    builder.post(bodyJson.toRequestBody(jsonMedia))
                }
                okHttp.newCall(builder.build()).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withTimeout AIResponse.failure(
                            error = "HTTP ${response.code}: ${raw.take(200)}",
                            provider = provider
                        )
                    }
                    val text = runCatching { parse(raw) }
                        .getOrElse { e -> return@withTimeout AIResponse.failure("Parse error: ${e.message}", provider) }

                    if (text.isBlank()) {
                        AIResponse.failure("Empty response from $provider", provider)
                    } else {
                        AIResponse(text = text, isSuccess = true, provider = provider)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            AIResponse.failure("Request timed out after 30s", provider)
        } catch (e: IOException) {
            AIResponse.failure("Network error: ${e.message}", provider)
        } catch (t: Throwable) {
            AIResponse.failure("Unexpected error: ${t.message}", provider)
        }
    }
}

package com.nexos.ai.data.repository

import com.nexos.ai.ai.SecureStorage
import com.nexos.ai.data.remote.api.NewsApi
import com.nexos.ai.data.remote.dto.ArticleDto
import com.nexos.ai.domain.model.Article
import com.nexos.ai.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [NewsApi] with timeout enforcement, error normalisation, and DTO → domain mapping.
 *
 * Architecture compliance:
 * - 30s timeout (Layer 4 rule) — applied even though NewsAPI isn't the AI layer, because
 *   the same network-quality concerns apply.
 * - All work on Dispatchers.IO
 * - Returns Result<List<Article>>; never throws to ViewModel
 */
@Singleton
class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val secureStorage: SecureStorage
) {

    fun hasApiKey(): Boolean = !secureStorage.getApiKey(Constants.PROVIDER_NEWS_API).isNullOrBlank()

    suspend fun topHeadlines(category: String?): Result<List<Article>> =
        runRemote { apiKey ->
            newsApi.topHeadlines(apiKey = apiKey, category = category?.takeIf { it.isNotBlank() })
        }

    suspend fun search(query: String): Result<List<Article>> = runRemote { apiKey ->
        newsApi.search(apiKey = apiKey, query = query)
    }

    private suspend fun runRemote(
        block: suspend (String) -> retrofit2.Response<com.nexos.ai.data.remote.dto.NewsResponseDto>
    ): Result<List<Article>> = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getApiKey(Constants.PROVIDER_NEWS_API)
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(NewsException.MissingKey)
        }
        try {
            val response = withTimeout(Constants.AI_REQUEST_TIMEOUT_MS) { block(apiKey) }
            if (!response.isSuccessful) {
                val errBody = response.errorBody()?.string().orEmpty().take(200)
                return@withContext Result.failure(NewsException.Http(response.code(), errBody))
            }
            val body = response.body()
                ?: return@withContext Result.failure(NewsException.Empty)
            if (body.status != null && body.status != "ok") {
                return@withContext Result.failure(
                    NewsException.Provider(body.code ?: "unknown", body.message ?: "")
                )
            }
            val articles = body.articles.mapNotNull { it.toDomain() }
            Result.success(articles)
        } catch (e: TimeoutCancellationException) {
            Result.failure(NewsException.Timeout)
        } catch (e: IOException) {
            Result.failure(NewsException.Network(e.message ?: ""))
        } catch (t: Throwable) {
            Result.failure(NewsException.Unknown(t.message ?: ""))
        }
    }

    private fun ArticleDto.toDomain(): Article? {
        val safeTitle = title?.trim().orEmpty()
        val safeUrl = url?.trim().orEmpty()
        if (safeTitle.isBlank() || safeUrl.isBlank() || safeTitle == "[Removed]") return null
        return Article(
            title = safeTitle,
            description = description?.trim().orEmpty(),
            content = content?.trim().orEmpty(),
            source = source?.name?.trim().orEmpty().ifBlank { "Unknown source" },
            author = author?.trim().orEmpty(),
            publishedAt = publishedAt?.trim().orEmpty(),
            url = safeUrl,
            imageUrl = urlToImage?.takeIf { it.startsWith("http") }
        )
    }
}

sealed class NewsException(message: String) : Exception(message) {
    data object MissingKey : NewsException("No NewsAPI key configured")
    data object Empty : NewsException("Empty response from NewsAPI")
    data object Timeout : NewsException("Request timed out")
    data class Http(val code: Int, val body: String) : NewsException("HTTP $code")
    data class Provider(val errorCode: String, val errorMessage: String) :
        NewsException("$errorCode: $errorMessage")
    data class Network(val detail: String) : NewsException("Network error: $detail")
    data class Unknown(val detail: String) : NewsException("Unknown error: $detail")
}

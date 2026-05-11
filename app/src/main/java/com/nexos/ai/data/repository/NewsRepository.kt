package com.nexos.ai.data.repository

import com.nexos.ai.ai.SecureStorage
import com.nexos.ai.data.remote.api.GNewsApi
import com.nexos.ai.data.remote.dto.GNewsArticleDto
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
 * Wraps the GNews API ([GNewsApi]) with timeout enforcement, error normalisation, and DTO →
 * domain mapping.
 *
 * The GNews key is seeded automatically on first launch (see
 * [com.nexos.ai.data.secure.ApiKeySeeder]) so the user does not have to paste anything to
 * start reading news. They can still rotate or remove the key from Settings → News.
 *
 * Why GNews and not NewsAPI.org:
 *   1. The user supplied a GNews key in a previous session (committed in branch
 *      cursor/news-fix-b8bc commit 48b9882).
 *   2. GNews's free tier (100 req/day) is comparable to NewsAPI's.
 *   3. GNews returns the image URL inline (`image`) — NewsAPI's `urlToImage` is sometimes
 *      empty even when an image exists at the source.
 */
@Singleton
class NewsRepository @Inject constructor(
    private val gnewsApi: GNewsApi,
    private val secureStorage: SecureStorage
) {

    fun hasApiKey(): Boolean = !secureStorage.getApiKey(Constants.PROVIDER_NEWS_API).isNullOrBlank()

    suspend fun topHeadlines(category: String?): Result<List<Article>> =
        runRemote { apiKey ->
            // GNews uses "nation" for country-level news, not NewsAPI's "general"
            val c = category?.takeIf { it.isNotBlank() && it in GNewsApi.CATEGORIES }
            gnewsApi.topHeadlines(apiKey = apiKey, category = c)
        }

    suspend fun search(query: String): Result<List<Article>> = runRemote { apiKey ->
        gnewsApi.search(apiKey = apiKey, query = query)
    }

    private suspend fun runRemote(
        block: suspend (String) -> retrofit2.Response<com.nexos.ai.data.remote.dto.GNewsResponseDto>
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
            val body = response.body() ?: return@withContext Result.failure(NewsException.Empty)
            if (!body.errors.isNullOrEmpty()) {
                return@withContext Result.failure(NewsException.Provider("gnews", body.errors.joinToString("; ")))
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

    private fun GNewsArticleDto.toDomain(): Article? {
        val safeTitle = title?.trim().orEmpty()
        val safeUrl = url?.trim().orEmpty()
        if (safeTitle.isBlank() || safeUrl.isBlank()) return null
        return Article(
            title = safeTitle,
            description = description?.trim().orEmpty(),
            content = content?.trim().orEmpty(),
            source = source?.name?.trim().orEmpty().ifBlank { "Unknown source" },
            author = "",
            publishedAt = publishedAt?.trim().orEmpty(),
            url = safeUrl,
            imageUrl = image?.takeIf { it.startsWith("http") }
        )
    }
}

sealed class NewsException(message: String) : Exception(message) {
    data object MissingKey : NewsException("No GNews key configured")
    data object Empty : NewsException("Empty response from GNews")
    data object Timeout : NewsException("Request timed out")
    data class Http(val code: Int, val body: String) : NewsException("HTTP $code")
    data class Provider(val provider: String, val errorMessage: String) :
        NewsException("$provider: $errorMessage")
    data class Network(val detail: String) : NewsException("Network error: $detail")
    data class Unknown(val detail: String) : NewsException("Unknown error: $detail")
}

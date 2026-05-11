package com.nexos.ai.data.remote.api

import com.nexos.ai.data.remote.dto.GNewsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * GNews (gnews.io) news API. Free developer tier — 100 requests/day. The user's key was
 * supplied during Phase 4 onboarding and is auto-seeded by [com.nexos.ai.data.secure.ApiKeySeeder]
 * on first launch.
 *
 * Auth: GNews only supports the apikey as a **query parameter** (it does not accept a header).
 * This is the leak vector mentioned in PRIVACY.md for AI providers, but mitigated here by:
 *   1. The [com.nexos.ai.di.QueryParamRedactingInterceptor] that masks `apikey=` in any
 *      logged URL before it can reach Logcat in debug builds.
 *   2. Network security config that already forces HTTPS-only.
 */
interface GNewsApi {

    @GET("api/v4/top-headlines")
    suspend fun topHeadlines(
        @Query("apikey") apiKey: String,
        @Query("category") category: String? = null,
        @Query("lang") language: String = "en",
        @Query("country") country: String = "us",
        @Query("max") max: Int = 10
    ): Response<GNewsResponseDto>

    @GET("api/v4/search")
    suspend fun search(
        @Query("apikey") apiKey: String,
        @Query("q") query: String,
        @Query("lang") language: String = "en",
        @Query("max") max: Int = 10,
        @Query("in") inScope: String = "title,description"
    ): Response<GNewsResponseDto>

    companion object {
        const val BASE_URL = "https://gnews.io/"

        /** Categories supported by `/top-headlines?category=…`. */
        val CATEGORIES = listOf(
            "general", "world", "nation", "business",
            "technology", "entertainment", "sports", "science", "health"
        )
    }
}

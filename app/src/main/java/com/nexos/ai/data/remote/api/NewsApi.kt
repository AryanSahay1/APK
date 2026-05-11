package com.nexos.ai.data.remote.api

import com.nexos.ai.data.remote.dto.NewsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * NewsAPI top-headlines endpoint. The API key travels in the `X-Api-Key` header
 * (NewsAPI also accepts `?apiKey=` but query-string keys leak into logs, so we use the
 * header — same rationale as Gemini in [com.nexos.ai.ai.GeminiProvider]).
 *
 * NewsAPI developer tier is free up to 100 requests/day per key.
 */
interface NewsApi {

    @GET("v2/top-headlines")
    suspend fun topHeadlines(
        @Header("X-Api-Key") apiKey: String,
        @Query("category") category: String? = null,
        @Query("country") country: String = "us",
        @Query("pageSize") pageSize: Int = 30,
        @Query("page") page: Int = 1
    ): Response<NewsResponseDto>

    @GET("v2/everything")
    suspend fun search(
        @Header("X-Api-Key") apiKey: String,
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 30
    ): Response<NewsResponseDto>

    companion object {
        const val BASE_URL = "https://newsapi.org/"
    }
}

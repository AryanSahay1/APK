package com.nexos.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * GNews (gnews.io) response shape. The free developer tier returns 10 articles per request,
 * up to 100 requests/day. See https://gnews.io/docs/v4.
 *
 * GNews's schema is similar to NewsAPI.org's but the field names differ slightly
 * (`urlToImage` → `image`, `articles` is nested at the top level with `totalArticles`).
 */
data class GNewsResponseDto(
    val totalArticles: Int = 0,
    val articles: List<GNewsArticleDto> = emptyList(),
    val errors: List<String>? = null
)

data class GNewsArticleDto(
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
    val url: String? = null,
    @SerializedName("image") val image: String? = null,
    val publishedAt: String? = null,
    val source: GNewsSourceDto? = null
)

data class GNewsSourceDto(
    val name: String? = null,
    val url: String? = null
)

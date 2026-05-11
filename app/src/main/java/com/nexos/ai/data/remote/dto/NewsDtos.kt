package com.nexos.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * NewsAPI response shape. We only deserialise the fields the UI actually uses to keep
 * memory pressure low when the response includes 100+ articles.
 *
 * See https://newsapi.org/docs/endpoints/top-headlines for the full schema.
 */
data class NewsResponseDto(
    val status: String? = null,
    val totalResults: Int = 0,
    val articles: List<ArticleDto> = emptyList(),
    val code: String? = null,
    val message: String? = null
)

data class ArticleDto(
    val source: SourceDto? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    @SerializedName("urlToImage") val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null
)

data class SourceDto(
    val id: String? = null,
    val name: String? = null
)

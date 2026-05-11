package com.nexos.ai.domain.model

import androidx.compose.runtime.Immutable

/**
 * Cleaned domain shape for a news article. Mapped from [com.nexos.ai.data.remote.dto.ArticleDto]
 * — non-null guarantees here mean the UI never has to null-check at render time.
 */
@Immutable
data class Article(
    val title: String,
    val description: String,
    val content: String,
    val source: String,
    val author: String,
    val publishedAt: String,
    val url: String,
    val imageUrl: String?
)

package com.nexos.ai.domain.usecase

import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.domain.model.Article
import com.nexos.ai.util.Constants
import javax.inject.Inject

/**
 * Save a [Article] into the user's notes. If the user has an AI provider configured we run the
 * article body through it (re-using [SummarizeWithAIUseCase]); otherwise we store the raw
 * description + content verbatim.
 */
class SaveArticleAsNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val summarizeWithAI: SummarizeWithAIUseCase
) {

    suspend operator fun invoke(article: Article): Note {
        val rawBody = buildString {
            if (article.description.isNotBlank()) {
                appendLine(article.description)
                appendLine()
            }
            if (article.content.isNotBlank()) appendLine(article.content)
            appendLine()
            append("Source: ${article.source}")
            if (article.url.isNotBlank()) {
                appendLine()
                append(article.url)
            }
        }.trim()

        val parsed = runCatching { summarizeWithAI(rawBody, isVoice = false) }.getOrNull()
        val title = parsed?.title?.takeIf { it.isNotBlank() } ?: article.title
        val content = parsed?.toMarkdownContent()?.takeIf { it.isNotBlank() } ?: rawBody
        val summary = parsed?.summary.orEmpty()

        val note = Note(
            title = title.take(160),
            content = content,
            summary = summary,
            sourceType = Constants.SOURCE_NEWS,
            tags = listOfNotNull(article.source.takeIf { it.isNotBlank() }, "news").joinToString(",")
        )
        val id = noteRepository.insert(note)
        return note.copy(id = id)
    }
}

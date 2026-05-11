package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.repository.NewsRepository
import com.nexos.ai.domain.model.Article
import com.nexos.ai.domain.usecase.SaveArticleAsNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsUiState(
    val category: String = "general",
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedArticleUrl: String? = null,
    val savedNoteId: Long = -1L,
    val error: String? = null,
    val hasApiKey: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val saveArticleAsNote: SaveArticleAsNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewsUiState(hasApiKey = newsRepository.hasApiKey()))
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    val categories: List<String> = listOf(
        "general", "technology", "business", "science", "sports", "health", "entertainment"
    )

    init {
        if (_state.value.hasApiKey) refresh()
    }

    fun selectCategory(category: String) {
        _state.update { it.copy(category = category, searchQuery = "") }
        refresh()
    }

    fun onSearchChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun submitSearch() {
        val q = _state.value.searchQuery.trim()
        if (q.isBlank()) {
            refresh()
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            newsRepository.search(q)
                .onSuccess { articles ->
                    _state.update { it.copy(isLoading = false, articles = articles) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
                }
        }
    }

    fun refresh() {
        val current = _state.value
        if (!current.hasApiKey) {
            _state.update { it.copy(error = "Set a NewsAPI key in Settings to load headlines.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            newsRepository.topHeadlines(current.category)
                .onSuccess { articles ->
                    _state.update { it.copy(isLoading = false, articles = articles) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
                }
        }
    }

    fun saveAsNote(article: Article) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, savedArticleUrl = null) }
            val note = saveArticleAsNote(article)
            _state.update {
                it.copy(isSaving = false, savedArticleUrl = article.url, savedNoteId = note.id)
            }
        }
    }

    fun apiKeyChanged() {
        _state.update { it.copy(hasApiKey = newsRepository.hasApiKey()) }
        if (newsRepository.hasApiKey()) refresh()
    }
}

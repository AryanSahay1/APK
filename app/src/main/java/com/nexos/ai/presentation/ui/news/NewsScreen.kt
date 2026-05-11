package com.nexos.ai.presentation.ui.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.domain.model.Article
import com.nexos.ai.presentation.ui.components.EmptyState
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosBorder
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft
import com.nexos.ai.presentation.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onOpenSettings: () -> Unit,
    onOpenSavedNote: (Long) -> Unit,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    // Refresh whenever the screen recomposes after the key is added in Settings.
    LaunchedEffect(Unit) { viewModel.apiKeyChanged() }

    LaunchedEffect(state.savedArticleUrl) {
        if (!state.savedArticleUrl.isNullOrBlank() && state.savedNoteId > 0) {
            val result = snackbar.showSnackbar(
                message = "Saved as note",
                actionLabel = "Open",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onOpenSavedNote(state.savedNoteId)
            }
        }
    }

    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = { Text("News") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexosBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) { Snackbar(it) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!state.hasApiKey) {
                NoKeyHero(onOpenSettings = onOpenSettings)
                return@Scaffold
            }

            SearchRow(
                query = state.searchQuery,
                onChange = viewModel::onSearchChange,
                onSubmit = viewModel::submitSearch
            )

            CategoryRow(
                categories = viewModel.categories,
                selected = state.category,
                onSelect = viewModel::selectCategory
            )

            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NexosPrimary)
                }
                state.error != null -> EmptyState(
                    title = "Couldn't load news",
                    subtitle = state.error.orEmpty(),
                    icon = Icons.AutoMirrored.Rounded.OpenInNew
                )
                state.articles.isEmpty() -> EmptyState(
                    title = "No articles",
                    subtitle = "Try a different category or search term.",
                    icon = Icons.Rounded.Search
                )
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.articles, key = { it.url }) { article ->
                        ArticleCard(
                            article = article,
                            isSaving = state.isSaving,
                            onSave = { viewModel.saveAsNote(article) },
                            onOpen = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                runCatching { context.startActivity(intent) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoKeyHero(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Add your NewsAPI key",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "NewsAPI offers a free developer tier (100 requests/day). " +
                "Get a key at https://newsapi.org and paste it in Settings → News.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onOpenSettings) { Text("Open Settings") }
    }
}

@Composable
private fun SearchRow(query: String, onChange: (String) -> Unit, onSubmit: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        placeholder = { Text("Search headlines…") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotBlank()) TextButton(onClick = onSubmit) { Text("Go") }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = NexosPrimary,
            unfocusedIndicatorColor = NexosBorder
        )
    )
}

@Composable
private fun CategoryRow(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSel = cat == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSel) NexosPrimary else NexosPrimarySoft)
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    cat.replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSel) NexosBackground else NexosPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    isSaving: Boolean,
    onSave: () -> Unit,
    onOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NexosBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onOpen)
            .padding(16.dp)
    ) {
        Text(
            article.source,
            style = MaterialTheme.typography.labelSmall,
            color = NexosPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            article.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (article.description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                article.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(10.dp))
        Row {
            TextButton(onClick = onOpen) {
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text(" Read")
            }
            Spacer(Modifier.fillMaxWidth(0.04f))
            TextButton(onClick = onSave, enabled = !isSaving) {
                Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = NexosPrimary)
                Spacer(Modifier.height(0.dp))
                Text(if (isSaving) " Saving…" else " Save as note", color = NexosPrimary)
            }
        }
    }
}

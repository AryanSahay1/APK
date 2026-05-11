package com.nexos.ai.presentation.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.util.canDrawOverlays
import com.nexos.ai.presentation.ui.components.EmptyState
import com.nexos.ai.presentation.ui.components.NoteCard
import com.nexos.ai.presentation.ui.components.WorkflowBanner
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft
import com.nexos.ai.presentation.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onOpenNote: (Long) -> Unit,
    onAddNote: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVoice: () -> Unit,
    onRequestScreenCapture: () -> Unit,
    onOpenAssistant: () -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val workflowState by viewModel.workflowState.collectAsStateWithLifecycle(
        initialValue = com.nexos.ai.domain.model.WorkflowState.Idle
    )

    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onOpenAssistant)
                    ) {
                        com.nexos.ai.presentation.ui.components.PandaMascot(
                            size = 32.dp,
                            hasLeaf = true
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("NexOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Tap the panda to chat",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexosBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNote,
                containerColor = NexosPrimary,
                contentColor = NexosBackground,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New note", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Live weather strip — pinned above everything per the user's spec
            // "always showing the real time update above panda on the main page".
            com.nexos.ai.presentation.ui.components.WeatherStrip()

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search notes…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = NexosPrimary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = workflowState !is com.nexos.ai.domain.model.WorkflowState.Idle &&
                        workflowState !is com.nexos.ai.domain.model.WorkflowState.Done,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    WorkflowBanner(state = workflowState)
                }
            }

            QuickActionsRow(
                onCapture = onRequestScreenCapture,
                onVoice = onOpenVoice,
                onManual = onAddNote
            )

            if (notes.isEmpty()) {
                EmptyState(
                    title = if (query.isBlank()) "Your panda is napping" else "No matches",
                    subtitle = if (query.isBlank())
                        "Capture a screen, speak an idea, or write a note. Everything you save lives only on this device."
                    else "Try a different search.",
                    showPanda = true,
                    pandaSleeping = query.isBlank(),
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(note = note, onClick = { onOpenNote(note.id) })
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onCapture: () -> Unit,
    onVoice: () -> Unit,
    onManual: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickAction(
            label = "Capture",
            icon = Icons.Rounded.CenterFocusStrong,
            onClick = {
                // One-shot flow: arm floating button → user taps → screenshot → note → button vanishes.
                // Requires overlay permission and an active MediaProjection grant. If either is
                // missing, we fall back to the original onCapture handler which prompts for the
                // missing piece via MainActivity.
                if (context.canDrawOverlays() &&
                    com.nexos.ai.service.ScreenshotProjectionState.isGranted) {
                    com.nexos.ai.service.FloatingButtonService.startOneShot(context)
                    // Minimise NexOS so the user can capture whatever app they want
                    val home = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                        addCategory(android.content.Intent.CATEGORY_HOME)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching { context.startActivity(home) }
                } else {
                    onCapture()
                }
            },
            modifier = Modifier.weight(1f)
        )
        QuickAction("Voice", Icons.Rounded.Mic, onVoice, Modifier.weight(1f))
        QuickAction("Write", Icons.Rounded.Add, onManual, Modifier.weight(1f))
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NexosPrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = NexosPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

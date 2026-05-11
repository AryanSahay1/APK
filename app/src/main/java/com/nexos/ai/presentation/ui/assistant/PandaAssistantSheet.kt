package com.nexos.ai.presentation.ui.assistant

import android.content.Intent
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.domain.model.AssistantContext
import com.nexos.ai.domain.model.ChatAttachment
import com.nexos.ai.domain.model.ChatMessage
import com.nexos.ai.domain.model.ChatRole
import com.nexos.ai.presentation.ui.components.FluffyPanda
import com.nexos.ai.presentation.viewmodel.PandaAssistantViewModel
import com.nexos.ai.util.DeepLinks

/**
 * Full-screen modal bottom sheet that hosts the panda assistant. The sheet covers the
 * application (per the user's spec: "the panda sits on the screen covering the application")
 * with a giant FluffyPanda at the top holding a tablet, the chat history below, and the
 * input row at the bottom.
 *
 * Why ModalBottomSheet (Material3) rather than a full Compose route?
 *   - Built-in drag-to-dismiss and IME insets handling.
 *   - Returning to the underlying screen is a single state toggle, no NavController surgery.
 *   - The user can swipe down to dismiss without losing the chat history if [onDismiss]
 *     keeps the underlying ViewModel alive (which we do — it's @HiltViewModel scoped to the
 *     hosting Activity).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PandaAssistantSheet(
    initialContext: AssistantContext,
    onDismiss: () -> Unit,
    onOpenGmailComposeWith: (to: String, subject: String, body: String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    viewModel: PandaAssistantViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(initialContext) { viewModel.openWith(initialContext) }

    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: context title + close
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    state.context.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            // Hero: fluffy panda holding the tablet
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FluffyPanda(
                    size = 200.dp,
                    context = state.context,
                    weatherCondition = state.weatherCondition
                )
            }

            // Chat history (the panda's "display")
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onOpenSwiggy = { cuisine -> DeepLinks.launchSwiggy(context, cuisine) },
                        onSendEmail = { draft ->
                            onOpenGmailComposeWith(draft.to, draft.subject, draft.body)
                        },
                        onOpenMaps = { address ->
                            DeepLinks.launchMapsSearch(context, address)
                        },
                        onCopy = { text ->
                            val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as? android.content.ClipboardManager
                            cm?.setPrimaryClip(android.content.ClipData.newPlainText("NexOS panda", text))
                        }
                    )
                }
                if (state.isTyping) {
                    item { TypingIndicator() }
                }
            }

            // Quick-context chips — let the user pivot the panda without closing the sheet
            ContextChipsRow(active = state.context, onSelect = viewModel::openWith)

            // Input row
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = viewModel::onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(placeholderFor(state.context)) },
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = viewModel::send,
                    enabled = state.input.isNotBlank() && !state.isTyping,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onOpenSwiggy: (String) -> Unit,
    onSendEmail: (ChatAttachment.EmailDraft) -> Unit,
    onOpenMaps: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    val isUser = message.role == ChatRole.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .heightIn(min = 0.dp)
        ) {
            Text(
                message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            // Render action button for any attachment the panda produced
            message.attachment?.let { att ->
                Spacer(Modifier.height(8.dp))
                when (att) {
                    is ChatAttachment.FoodSuggestion -> AttachmentAction(
                        label = "Open Swiggy with \"${att.cuisine}\""
                    ) { onOpenSwiggy(att.cuisine) }

                    is ChatAttachment.EmailDraft -> AttachmentAction(
                        label = "Open Gmail with this draft"
                    ) { onSendEmail(att) }

                    is ChatAttachment.ParsedAddress -> Column {
                        AttachmentAction(label = "Copy address") { onCopy(att.address) }
                        Spacer(Modifier.height(6.dp))
                        AttachmentAction(label = "Open in Google Maps") { onOpenMaps(att.address) }
                    }

                    is ChatAttachment.AlarmPending -> AttachmentAction(
                        label = "Already scheduled ✓",
                        enabled = false
                    ) {}

                    is ChatAttachment.WeatherSummary -> Unit
                    is ChatAttachment.NewsHeadlines -> Unit
                }
            }
        }
    }
}

@Composable
private fun AttachmentAction(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                "Panda is thinking…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContextChipsRow(
    active: AssistantContext,
    onSelect: (AssistantContext) -> Unit
) {
    val all = AssistantContext.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        all.forEach { ctx ->
            val selected = ctx == active
            val container = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface
            val content = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(container)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                    .clickable { onSelect(ctx) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    chipLabelFor(ctx),
                    style = MaterialTheme.typography.labelMedium,
                    color = content,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

private fun chipLabelFor(ctx: AssistantContext): String = when (ctx) {
    AssistantContext.Default -> "Chat"
    AssistantContext.Weather -> "Weather"
    AssistantContext.News -> "News"
    AssistantContext.Email -> "Email"
    AssistantContext.Alarm -> "Alarm"
    AssistantContext.Map -> "Map link"
    AssistantContext.Food -> "Food / mood"
    AssistantContext.Notes -> "Notes"
}

private fun placeholderFor(ctx: AssistantContext): String = when (ctx) {
    AssistantContext.Default -> "Ask anything…"
    AssistantContext.Weather -> "Ask about weather, or just say 'weather'"
    AssistantContext.News -> "Topic or 'top news'"
    AssistantContext.Email -> "Who to email and about what"
    AssistantContext.Alarm -> "Remind me at 8am tomorrow…"
    AssistantContext.Map -> "Paste a Maps / WhatsApp location link"
    AssistantContext.Food -> "Describe your mood"
    AssistantContext.Notes -> "Paste text to summarise or rewrite"
}

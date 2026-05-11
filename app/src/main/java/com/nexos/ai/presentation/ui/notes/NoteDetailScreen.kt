package com.nexos.ai.presentation.ui.notes

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft
import com.nexos.ai.presentation.viewmodel.NoteDetailViewModel
import com.nexos.ai.util.toFormattedDateTime

/**
 * @param noteId The note being displayed. Read by the ViewModel via SavedStateHandle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    @Suppress("UNUSED_PARAMETER") noteId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenNotebook: () -> Unit = {},
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val note by viewModel.note.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = { Text("Note", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = NexosPrimary)
                    }
                    IconButton(onClick = {
                        val n = note ?: return@IconButton
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, n.title)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                buildString {
                                    appendLine("# ${n.title}")
                                    if (n.summary.isNotBlank()) {
                                        appendLine()
                                        appendLine(n.summary)
                                    }
                                    appendLine()
                                    append(n.content)
                                }
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, "Share note"))
                    }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexosBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val current = note
            if (current == null) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "This note no longer exists.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    current.title.ifBlank { "Untitled note" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(NexosPrimarySoft)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            current.sourceType.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = NexosPrimary
                        )
                    }
                    Spacer(Modifier.fillMaxWidth(0.02f))
                    Text(
                        " · ${current.timestamp.toFormattedDateTime()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (current.summary.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NexosPrimarySoft)
                            .padding(16.dp)
                    ) {
                        Text(
                            current.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    current.content.ifBlank { "(no content)" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (current.tagList.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        current.tagList.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(NexosPrimarySoft)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("#$tag", style = MaterialTheme.typography.labelSmall, color = NexosPrimary)
                            }
                        }
                    }
                }

                // Notebook entry point — turns this note into the cover of a multi-page
                // notebook, or opens the existing notebook for editing.
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onOpenNotebook)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (current.isNotebook) "Open notebook designer" else "Make this a notebook cover",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (current.isNotebookCompleted)
                                "Marked complete · tap to re-export"
                            else
                                "Design a cover + back page, attach other notes as pages, export as PDF",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Rounded.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Attachments — images, audio (playable), and location pins
                val attachments = remember(current.attachmentsJson) {
                    com.nexos.ai.data.local.NoteAttachmentCodec.decode(current.attachmentsJson)
                }
                if (attachments.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Attachments",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    attachments.forEach { att ->
                        DetailAttachment(att = att)
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this note?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete(onBack)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Renders one of the three NoteAttachment kinds. Extracted from NoteDetailScreen so the
 * scaffold reads cleanly. Image previews use BitmapFactory through the content resolver —
 * intentionally no Coil dep, which would add ~1 MB for a feature used at most once per note.
 */
@Composable
private fun DetailAttachment(att: com.nexos.ai.domain.model.NoteAttachment) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    when (att) {
        is com.nexos.ai.domain.model.NoteAttachment.Image -> {
            val uri = remember(att.uri) { android.net.Uri.parse(att.uri) }
            var bitmap by remember { androidx.compose.runtime.mutableStateOf<android.graphics.Bitmap?>(null) }
            androidx.compose.runtime.LaunchedEffect(att.uri) {
                bitmap = runCatching {
                    ctx.contentResolver.openInputStream(uri)?.use {
                        android.graphics.BitmapFactory.decodeStream(it)
                    }
                }.getOrNull()
            }
            val bmp = bitmap
            if (bmp != null) {
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Attached image",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                AttachmentPlaceholder("Image — couldn't load preview")
            }
        }
        is com.nexos.ai.domain.model.NoteAttachment.Audio -> {
            AudioPlayerRow(filePath = att.filePath, durationMs = att.durationMs)
        }
        is com.nexos.ai.domain.model.NoteAttachment.Location -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        com.nexos.ai.util.DeepLinks.launchMapsSearch(
                            ctx,
                            "${att.latitude},${att.longitude}"
                        )
                    }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        androidx.compose.material.icons.Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(att.label, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    "%.5f, %.5f · tap to open in Maps".format(att.latitude, att.longitude),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AttachmentPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AudioPlayerRow(filePath: String, durationMs: Long) {
    var isPlaying by remember { androidx.compose.runtime.mutableStateOf(false) }
    val player = remember { android.media.MediaPlayer() }
    androidx.compose.runtime.DisposableEffect(filePath) {
        runCatching {
            player.reset()
            player.setDataSource(filePath)
            player.prepare()
            player.setOnCompletionListener { isPlaying = false }
        }
        onDispose { runCatching { player.release() } }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                if (isPlaying) {
                    runCatching { player.pause() }
                    isPlaying = false
                } else {
                    runCatching { player.start() }
                    isPlaying = true
                }
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isPlaying) androidx.compose.material.icons.Icons.Rounded.Stop
            else androidx.compose.material.icons.Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Voice memo", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Text("%.1f s".format(durationMs / 1000.0),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


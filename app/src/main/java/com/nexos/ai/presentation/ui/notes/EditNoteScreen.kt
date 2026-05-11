package com.nexos.ai.presentation.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.viewmodel.EditNoteViewModel

/**
 * @param noteId The note being edited. Passed by the nav route as `editNote/{noteId}`. The
 *               ViewModel reads it via SavedStateHandle, so this composable parameter is
 *               informational only — Compose Nav requires it as a key for backstack stability.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    @Suppress("UNUSED_PARAMETER") noteId: Long,
    onSaved: (Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: EditNoteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isExistingNote) "Edit note" else "New note",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = noteFieldColors()
            )
            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp),
                label = { Text("Content") },
                shape = RoundedCornerShape(12.dp),
                colors = noteFieldColors()
            )
            OutlinedTextField(
                value = state.tags,
                onValueChange = viewModel::onTagsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tags (comma separated)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = noteFieldColors()
            )

            // Attachments row + list
            AttachmentToolbar(
                isRecording = state.isRecordingAudio,
                isAttachingLocation = state.isAttachingLocation,
                onPickImage = { uri, mime -> viewModel.addImageAttachment(uri, mime) },
                onStartRecord = viewModel::startAudioRecording,
                onStopRecord = viewModel::stopAudioRecording,
                onCancelRecord = viewModel::cancelAudioRecording,
                onAttachLocation = viewModel::attachCurrentLocation
            )

            if (state.attachments.isNotEmpty()) {
                AttachmentsList(
                    attachments = state.attachments,
                    onRemove = viewModel::removeAttachment
                )
            }

            Button(
                onClick = { viewModel.save { id -> onSaved(id) } },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NexosPrimary, contentColor = NexosBackground)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text(" Save note", fontWeight = FontWeight.SemiBold)
            }

            state.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AttachmentToolbar(
    isRecording: Boolean,
    isAttachingLocation: Boolean,
    onPickImage: (uri: String, mime: String) -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onCancelRecord: () -> Unit,
    onAttachLocation: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val mime = context.contentResolver.getType(uri) ?: "image/*"
            onPickImage(uri.toString(), mime)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AttachChip(
            icon = Icons.Rounded.Image,
            label = "Image",
            onClick = {
                imagePicker.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
        AttachChip(
            icon = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
            label = if (isRecording) "Stop" else "Voice",
            highlighted = isRecording,
            onClick = if (isRecording) onStopRecord else onStartRecord
        )
        AttachChip(
            icon = Icons.Rounded.LocationOn,
            label = if (isAttachingLocation) "Locating…" else "Location",
            enabled = !isAttachingLocation,
            onClick = onAttachLocation
        )
        if (isRecording) {
            AttachChip(
                icon = Icons.Rounded.Close,
                label = "Cancel",
                onClick = onCancelRecord
            )
        }
    }
}

@Composable
private fun AttachChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val container = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val content = if (highlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .let { mod -> if (enabled) mod.clickable(onClick = onClick) else mod }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = content, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AttachmentsList(
    attachments: List<com.nexos.ai.domain.model.NoteAttachment>,
    onRemove: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        attachments.forEach { att -> AttachmentRow(att, onRemove = { onRemove(att.id) }) }
    }
}

@Composable
private fun AttachmentRow(
    att: com.nexos.ai.domain.model.NoteAttachment,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, label, sub) = when (att) {
            is com.nexos.ai.domain.model.NoteAttachment.Image -> Triple(
                Icons.Rounded.Image, "Image", att.mimeType
            )
            is com.nexos.ai.domain.model.NoteAttachment.Audio -> Triple(
                Icons.Rounded.Mic,
                "Voice memo",
                "%.1f s · tap to play".format(att.durationMs / 1000.0)
            )
            is com.nexos.ai.domain.model.NoteAttachment.Location -> Triple(
                Icons.Rounded.LocationOn, "Location", att.label
            )
        }
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Rounded.Close, contentDescription = "Remove")
        }
    }
}

@Composable
private fun noteFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = NexosPrimary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = NexosPrimary,
    cursorColor = NexosPrimary
)

package com.nexos.ai.presentation.ui.notes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.FormatAlignLeft
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.automirrored.rounded.FormatAlignRight
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.FormatStrikethrough
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.domain.model.NoteAttachment
import com.nexos.ai.presentation.ui.components.PandaBackground
import com.nexos.ai.presentation.ui.components.PandaBackgroundCanvas
import com.nexos.ai.presentation.ui.components.PandaBackgroundThumb
import com.nexos.ai.presentation.ui.components.PandaBackgrounds
import com.nexos.ai.presentation.viewmodel.EditNoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Vivo-style notes editor (v1.5).
 *
 * Layout, top-to-bottom:
 *   1. Top app bar — back / undo / redo / save  (icons match the screenshot 1:1).
 *   2. Title — large placeholder "Title", single-line, no underline.
 *   3. Meta row — "<day, date> at <time> | <n> characters".
 *   4. Body — multi-line plain-text editor that supports markdown-ish
 *      formatting tokens injected by the toolbar (**bold**, *italic*, etc.).
 *      Renders over the selected [PandaBackground] if any.
 *   5. Attachments list (images / audio / location / doodle).
 *   6. Bottom toolbar — Aa, alignment+lists, checkbox, reminder/clock,
 *      image+camera, mic, more (...). 'More' opens a menu for doodle,
 *      table, location, and the background picker.
 *
 * Rich text strategy:
 *   We don't fork the platform TextField. Instead, the toolbar inserts
 *   markdown tokens around the current selection (or at the cursor), and
 *   the detail view renders the saved markdown. Bold = **…**, italic = *…*,
 *   underline = __…__, strikethrough = ~~…~~, highlight = ==…==. This
 *   round-trips cleanly and keeps the editor a plain TextField — undo /
 *   redo / IME work without surprises.
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
    val context = LocalContext.current

    // The TextFieldValue gives us cursor position + selection ranges, which we need to
    // surround user-selected text with markdown tokens from the formatting toolbar.
    var bodyValue by remember { mutableStateOf(TextFieldValue(text = state.content)) }
    LaunchedEffect(state.content) {
        if (bodyValue.text != state.content) {
            bodyValue = bodyValue.copy(text = state.content)
        }
    }

    // Sheet visibility
    var textStyleSheet by remember { mutableStateOf(false) }
    var alignmentSheet by remember { mutableStateOf(false) }
    var backgroundSheet by remember { mutableStateOf(false) }
    var moreMenu by remember { mutableStateOf(false) }
    var showDoodle by remember { mutableStateOf(false) }

    // Image picker (gallery / camera roll). Camera capture is in v5-7.
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val mime = context.contentResolver.getType(uri) ?: "image/*"
            viewModel.addImageAttachment(uri.toString(), mime)
        }
    }

    if (showDoodle) {
        com.nexos.ai.presentation.ui.components.DoodlePadScreen(
            onBack = { showDoodle = false },
            onSaved = { absolutePath ->
                viewModel.addDoodleAttachment(absolutePath)
                showDoodle = false
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background — full-bleed behind everything else when picked.
        if (state.backgroundId > 0) {
            PandaBackgroundCanvas(
                backgroundId = state.backgroundId,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
            )
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
        ) {

            // ----- Top app bar -----
            TopBar(
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onBack = onCancel,
                onUndo = {
                    viewModel.undo()
                    bodyValue = TextFieldValue(viewModel.state.value.content,
                        selection = TextRange(viewModel.state.value.content.length))
                },
                onRedo = {
                    viewModel.redo()
                    bodyValue = TextFieldValue(viewModel.state.value.content,
                        selection = TextRange(viewModel.state.value.content.length))
                },
                onSave = { viewModel.save(onSaved) }
            )

            // ----- Body (title + meta + editor + attachments), scrollable -----
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                TitleField(
                    value = state.title,
                    onChange = viewModel::onTitleChange
                )

                Spacer(Modifier.height(8.dp))
                MetaRow(
                    timestamp = state.createdAt,
                    charCount = state.content.length
                )
                Spacer(Modifier.height(20.dp))

                // Body editor — plain text with markdown tokens injected by the toolbar.
                BodyEditor(
                    value = bodyValue,
                    onValueChange = { tfv ->
                        bodyValue = tfv
                        if (tfv.text != state.content) viewModel.onContentChange(tfv.text)
                    },
                    fontSizeSp = state.bodyTextSizeSp,
                    textAlign = when (state.textAlignment) {
                        1 -> TextAlign.Center
                        2 -> TextAlign.End
                        else -> TextAlign.Start
                    }
                )

                if (state.attachments.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    AttachmentsStrip(
                        attachments = state.attachments,
                        onRemove = viewModel::removeAttachment
                    )
                }

                Spacer(Modifier.height(48.dp))
            }

            // ----- Bottom toolbar -----
            BottomToolbar(
                isRecording = state.isRecordingAudio,
                onOpenTextStyle = { textStyleSheet = true },
                onOpenAlignment = { alignmentSheet = true },
                onInsertCheckbox = {
                    bodyValue = insertAtCaret(bodyValue, "\n[ ] ")
                    viewModel.onContentChange(bodyValue.text)
                },
                onSetReminder = { /* defers to More menu / Alarms tab */ moreMenu = true },
                onPickImage = {
                    imagePicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onToggleRecord = {
                    if (state.isRecordingAudio) viewModel.stopAudioRecording()
                    else viewModel.startAudioRecording()
                },
                onOpenMore = { moreMenu = true }
            )

            // The moreMenu DropdownMenu anchors to a hidden Box near the toolbar — kept
            // outside the toolbar Composable so anchoring is stable across recomposition.
            Box {
                DropdownMenu(
                    expanded = moreMenu,
                    onDismissRequest = { moreMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Doodle / Drawing") },
                        leadingIcon = { Icon(Icons.Rounded.Brush, contentDescription = null) },
                        onClick = { moreMenu = false; showDoodle = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Insert table") },
                        leadingIcon = { Icon(Icons.Rounded.TableChart, contentDescription = null) },
                        onClick = {
                            moreMenu = false
                            bodyValue = insertAtCaret(bodyValue, "\n${defaultTableMarkdown()}\n")
                            viewModel.onContentChange(bodyValue.text)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Background theme") },
                        leadingIcon = { Icon(Icons.Rounded.Wallpaper, contentDescription = null) },
                        onClick = { moreMenu = false; backgroundSheet = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Attach location") },
                        leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
                        onClick = { moreMenu = false; viewModel.attachCurrentLocation() }
                    )
                    DropdownMenuItem(
                        text = { Text("Take photo (camera)") },
                        leadingIcon = { Icon(Icons.Rounded.CameraAlt, contentDescription = null) },
                        onClick = {
                            moreMenu = false
                            // Camera capture is wired in v5-7; this fires via the
                            // ActivityResultContracts.TakePicture launcher held by the
                            // CameraCaptureLauncher composable below.
                            cameraLaunchRequested.value = true
                        }
                    )
                }
            }
        }

        // Camera capture machinery — separate launcher to keep the More menu lean.
        CameraCaptureLauncher(
            requestState = cameraLaunchRequested,
            onCaptured = { uri, mime -> viewModel.addImageAttachment(uri, mime) }
        )

        // Bottom sheets
        if (textStyleSheet) {
            TextStyleSheet(
                fontSizeSp = state.bodyTextSizeSp,
                onChangeFontSize = viewModel::setBodyTextSize,
                onBold = { applyWrap(bodyValue, "**") { v -> bodyValue = v; viewModel.onContentChange(v.text) } },
                onItalic = { applyWrap(bodyValue, "*") { v -> bodyValue = v; viewModel.onContentChange(v.text) } },
                onUnderline = { applyWrap(bodyValue, "__") { v -> bodyValue = v; viewModel.onContentChange(v.text) } },
                onStrike = { applyWrap(bodyValue, "~~") { v -> bodyValue = v; viewModel.onContentChange(v.text) } },
                onHighlight = { applyWrap(bodyValue, "==") { v -> bodyValue = v; viewModel.onContentChange(v.text) } },
                onDismiss = { textStyleSheet = false }
            )
        }
        if (alignmentSheet) {
            AlignmentSheet(
                currentAlignment = state.textAlignment,
                onAlignment = viewModel::setAlignment,
                onBullet = {
                    bodyValue = insertAtCaret(bodyValue, "\n• ")
                    viewModel.onContentChange(bodyValue.text)
                },
                onNumbered = {
                    bodyValue = insertAtCaret(bodyValue, "\n1. ")
                    viewModel.onContentChange(bodyValue.text)
                },
                onDismiss = { alignmentSheet = false }
            )
        }
        if (backgroundSheet) {
            BackgroundPickerSheet(
                currentId = state.backgroundId,
                onPick = { id -> viewModel.setBackground(id); backgroundSheet = false },
                onClearBackground = { viewModel.setBackground(0); backgroundSheet = false },
                onDismiss = { backgroundSheet = false }
            )
        }

        state.error?.let { err ->
            AlertDialog(
                onDismissRequest = { /* clear by re-trigger */ },
                title = { Text("Couldn't do that") },
                text = { Text(err) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onContentChange(state.content) /* harmless ping */ }) { Text("OK") }
                }
            )
        }
    }
}

// One mutable bit shared between the More menu and CameraCaptureLauncher; declared at file
// scope so launching the camera survives recomposition of the menu.
private val cameraLaunchRequested = androidx.compose.runtime.mutableStateOf(false)

// -------------------------------------------------------------------------------------------
// Sub-composables
// -------------------------------------------------------------------------------------------

@Composable
private fun TopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo",
                tint = if (canUndo) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
        IconButton(onClick = onRedo, enabled = canRedo) {
            Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = "Redo",
                tint = if (canRedo) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
        IconButton(onClick = onSave) {
            Icon(Icons.Rounded.Check, contentDescription = "Save",
                tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TitleField(
    value: String,
    onChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    "Title",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            inner()
        }
    )
}

@Composable
private fun MetaRow(timestamp: Long, charCount: Int) {
    val dateLabel = remember(timestamp) {
        SimpleDateFormat("EEEE, MMM d 'at' HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            dateLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
        Spacer(Modifier.width(8.dp))
        Text(
            "$charCount characters",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BodyEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    fontSizeSp: Int,
    textAlign: TextAlign
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = LocalTextStyle.current.copy(
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.45f).sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = textAlign
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
    )
}

@Composable
private fun AttachmentsStrip(
    attachments: List<NoteAttachment>,
    onRemove: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        attachments.forEach { att ->
            val (icon, label, sub) = when (att) {
                is NoteAttachment.Image -> Triple(Icons.Rounded.Image, "Image", att.mimeType)
                is NoteAttachment.Audio -> Triple(Icons.Rounded.Mic, "Voice memo",
                    "%.1f s".format(att.durationMs / 1000.0))
                is NoteAttachment.Location -> Triple(Icons.Rounded.LocationOn, "Location", att.label)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(sub, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onRemove(att.id) }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Remove")
                }
            }
        }
    }
}

@Composable
private fun BottomToolbar(
    isRecording: Boolean,
    onOpenTextStyle: () -> Unit,
    onOpenAlignment: () -> Unit,
    onInsertCheckbox: () -> Unit,
    onSetReminder: () -> Unit,
    onPickImage: () -> Unit,
    onToggleRecord: () -> Unit,
    onOpenMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarIcon(Icons.Rounded.TextFields, "Text style", onOpenTextStyle)
        ToolbarIcon(Icons.AutoMirrored.Rounded.FormatAlignLeft, "Alignment & lists", onOpenAlignment)
        ToolbarIcon(Icons.Rounded.CheckBox, "Checkbox", onInsertCheckbox)
        ToolbarIcon(Icons.Rounded.AccessTime, "Reminder", onSetReminder)
        ToolbarIcon(Icons.Rounded.PhotoLibrary, "Insert image", onPickImage)
        ToolbarIcon(
            if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
            if (isRecording) "Stop recording" else "Record voice",
            onToggleRecord,
            highlighted = isRecording
        )
        ToolbarIcon(Icons.Rounded.MoreVert, "More", onOpenMore)
    }
}

@Composable
private fun ToolbarIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    highlighted: Boolean = false
) {
    val bg = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val tint = if (highlighted) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = tint)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextStyleSheet(
    fontSizeSp: Int,
    onChangeFontSize: (Int) -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onStrike: () -> Unit,
    onHighlight: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Text style", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("A", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(8.dp))
                Slider(
                    value = fontSizeSp.toFloat(),
                    onValueChange = { onChangeFontSize(it.toInt()) },
                    valueRange = 12f..28f,
                    steps = 15,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text("A", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Text("${fontSizeSp}sp", style = MaterialTheme.typography.labelMedium)
            }
            ThinDivider()
            Text("Inline formatting", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StyleChip(Icons.Rounded.FormatBold, "Bold", onBold)
                StyleChip(Icons.Rounded.FormatItalic, "Italic", onItalic)
                StyleChip(Icons.Rounded.FormatUnderlined, "Underline", onUnderline)
                StyleChip(Icons.Rounded.FormatStrikethrough, "Strike", onStrike)
                StyleChip(Icons.Rounded.FormatColorFill, "Highlight", onHighlight)
            }
            Text(
                "Tip: inline formatting is rendered when you open the saved note.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StyleChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlignmentSheet(
    currentAlignment: Int,
    onAlignment: (Int) -> Unit,
    onBullet: () -> Unit,
    onNumbered: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Alignment & lists", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AlignButton(Icons.AutoMirrored.Rounded.FormatAlignLeft, "Left", 0,
                    selected = currentAlignment == 0, onClick = { onAlignment(0) })
                AlignButton(Icons.Rounded.FormatAlignCenter, "Center", 1,
                    selected = currentAlignment == 1, onClick = { onAlignment(1) })
                AlignButton(Icons.AutoMirrored.Rounded.FormatAlignRight, "Right", 2,
                    selected = currentAlignment == 2, onClick = { onAlignment(2) })
            }
            ThinDivider()
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StyleChip(Icons.AutoMirrored.Rounded.FormatListBulleted, "Bullets", onBullet)
                StyleChip(Icons.Rounded.FormatListNumbered, "Numbered", onNumbered)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AlignButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    @Suppress("UNUSED_PARAMETER") id: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface)
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackgroundPickerSheet(
    currentId: Int,
    onPick: (Int) -> Unit,
    onClearBackground: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Background theme", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClearBackground) { Text("Clear") }
            }
            Text(
                "30 panda-themed surfaces. Tap to apply.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            BoxWithConstraints {
                val cellW = (maxWidth - 20.dp * 3) / 3
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(420.dp)
                ) {
                    items(PandaBackgrounds.all, key = { it.id }) { bg ->
                        BackgroundCell(
                            design = bg,
                            isSelected = bg.id == currentId,
                            cellWidth = cellW,
                            onClick = { onPick(bg.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundCell(
    design: PandaBackground,
    isSelected: Boolean,
    cellWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Column(modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(width = cellWidth, height = cellWidth * 1.3f)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(12.dp)
                )
        ) {
            PandaBackgroundThumb(design = design, modifier = Modifier.fillMaxSize())
        }
        Text(
            "${design.id}. ${design.displayName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun CameraCaptureLauncher(
    requestState: androidx.compose.runtime.MutableState<Boolean>,
    onCaptured: (uri: String, mime: String) -> Unit
) {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        pendingUri = null
        if (success && uri != null) {
            onCaptured(uri.toString(), "image/jpeg")
        }
    }
    LaunchedEffect(requestState.value) {
        if (requestState.value) {
            requestState.value = false
            val outDir = java.io.File(context.filesDir, "captures").also { it.mkdirs() }
            val outFile = java.io.File(outDir, "photo_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            pendingUri = uri
            runCatching { cameraLauncher.launch(uri) }
        }
    }
}

// -------------------------------------------------------------------------------------------
// Pure helpers — kept stateless so they're easy to unit-test.
// -------------------------------------------------------------------------------------------

/** Wraps the current selection with [token] on both sides, or inserts a placeholder + token. */
private fun applyWrap(
    value: TextFieldValue,
    token: String,
    apply: (TextFieldValue) -> Unit
) {
    val sel = value.selection
    val text = value.text
    if (sel.start == sel.end) {
        // No selection — drop the token pair at the caret with the caret between them.
        val before = text.substring(0, sel.start)
        val after = text.substring(sel.start)
        val newText = before + token + token + after
        apply(value.copy(text = newText, selection = TextRange(before.length + token.length)))
    } else {
        val before = text.substring(0, sel.min)
        val middle = text.substring(sel.min, sel.max)
        val after = text.substring(sel.max)
        val newText = before + token + middle + token + after
        apply(value.copy(
            text = newText,
            selection = TextRange(
                before.length + token.length,
                before.length + token.length + middle.length
            )
        ))
    }
}

private fun insertAtCaret(value: TextFieldValue, snippet: String): TextFieldValue {
    val text = value.text
    val pos = value.selection.start.coerceIn(0, text.length)
    val before = text.substring(0, pos)
    val after = text.substring(pos)
    val newText = before + snippet + after
    return value.copy(text = newText, selection = TextRange(before.length + snippet.length))
}

private fun defaultTableMarkdown(): String = """
| Column A | Column B | Column C |
| -------- | -------- | -------- |
| row 1A   | row 1B   | row 1C   |
| row 2A   | row 2B   | row 2C   |
""".trimIndent()

/**
 * 1-dp divider used in the bottom sheets. Re-implemented locally because Material3 1.1.x
 * (the BOM we pin to) does not yet expose HorizontalDivider — that landed in 1.2.0.
 */
@Composable
private fun ThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    )
}

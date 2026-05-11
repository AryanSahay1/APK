package com.nexos.ai.presentation.ui.notes

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.domain.model.NotebookCover
import com.nexos.ai.presentation.viewmodel.NotebookViewModel

/**
 * Cover + back-page designer for a notebook, plus the page list and PDF export action.
 *
 * Layout:
 *   - Live preview at the top (renders the cover the same way NotebookPdfExporter will).
 *   - Background color, accent color, title, subtitle inputs.
 *   - Motif radio (Panda + leaf / Gridlines / Confetti / Plain).
 *   - Attached pages, with an "Attach a note" picker pulled from notes that aren't already
 *     part of any notebook.
 *   - Bottom action bar: 'Save design' + 'Export as PDF' (+ 'Mark complete' badge).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
    onBack: () -> Unit,
    viewModel: NotebookViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pickerOpen by remember { mutableStateOf(false) }

    // Once an export finishes, immediately show a share dialog so the user can pull the
    // PDF out of NexOS into their preferred reader / cloud drive.
    state.exportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { viewModel.consumeExportUri() },
            title = { Text("Notebook exported") },
            text = { Text("Your notebook PDF has been saved to Documents/NexOS. Open it now?") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(android.net.Uri.parse(uri), "application/pdf")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching { context.startActivity(intent) }
                    viewModel.consumeExportUri()
                }) { Text("Open") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.consumeExportUri() }) { Text("Later") }
            }
        )
    }

    state.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.consumeError() },
            title = { Text("Export failed") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.consumeError() }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Design notebook") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CoverPreview(design = state.design, fallbackTitle = state.cover?.title.orEmpty())

            DesignerFields(
                design = state.design,
                onDesignChanged = viewModel::updateDesign
            )

            // Pages section
            Text(
                "Pages (${state.pages.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            state.pages.forEach { page ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(page.title.ifBlank { "Untitled note" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        Text(page.content.take(80),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.detachPage(page) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Remove page")
                    }
                }
            }

            OutlinedButton(
                onClick = { pickerOpen = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Attach a note as a page")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::saveDesign,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Save design")
                }
                Button(
                    onClick = viewModel::exportPdf,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isExporting && state.cover != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isExporting) "Exporting…" else "Export PDF")
                }
            }

            if (!(state.cover?.isNotebookCompleted ?: false)) {
                TextButton(onClick = viewModel::markComplete) {
                    Text("Mark notebook complete")
                }
            } else {
                Text(
                    "✓ Marked complete",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (pickerOpen) {
        AlertDialog(
            onDismissRequest = { pickerOpen = false },
            title = { Text("Attach a page") },
            text = {
                if (state.availableNotes.isEmpty()) {
                    Text("No unattached notes available. Create a note first, then attach it here.")
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        state.availableNotes.forEach { n ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.attachPage(n)
                                        pickerOpen = false
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(n.title.ifBlank { "Untitled" },
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickerOpen = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun CoverPreview(design: NotebookCover, fallbackTitle: String) {
    val bg = parseColor(design.backgroundHex, Color(0xFF0F0F14))
    val accent = parseColor(design.accentHex, Color(0xFF00E676))
    val titleText = design.titleOverride.ifBlank { fallbackTitle }.ifBlank { "Notebook" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            titleText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (bg.relativeLuminance() > 0.55f) Color(0xFF0F0F14) else Color(0xFFFAFAFA),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (design.subtitle.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(design.subtitle, style = MaterialTheme.typography.bodyMedium, color = accent)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Motif: ${design.motif.displayName}",
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun DesignerFields(
    design: NotebookCover,
    onDesignChanged: ((NotebookCover) -> NotebookCover) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = design.titleOverride,
            onValueChange = { v -> onDesignChanged { it.copy(titleOverride = v) } },
            label = { Text("Cover title (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = design.subtitle,
            onValueChange = { v -> onDesignChanged { it.copy(subtitle = v) } },
            label = { Text("Subtitle") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Background", style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold)
        SwatchRow(
            current = design.backgroundHex,
            options = listOf("#0F0F14", "#FAFAFA", "#1F2937", "#FFF8E1", "#E0F7FA", "#3B0764"),
            onPick = { hex -> onDesignChanged { it.copy(backgroundHex = hex) } }
        )

        Text("Accent", style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold)
        SwatchRow(
            current = design.accentHex,
            options = listOf("#00E676", "#FF9800", "#F06292", "#42A5F5", "#FFEE58", "#BA68C8"),
            onPick = { hex -> onDesignChanged { it.copy(accentHex = hex) } }
        )

        Text("Motif", style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            NotebookCover.Motif.entries.forEach { motif ->
                val selected = design.motif == motif
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onDesignChanged { it.copy(motif = motif) } }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(motif.displayName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        OutlinedTextField(
            value = design.backNote,
            onValueChange = { v -> onDesignChanged { it.copy(backNote = v) } },
            label = { Text("Back-page message") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SwatchRow(
    current: String,
    options: List<String>,
    onPick: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { hex ->
            val selected = hex.equals(current, ignoreCase = true)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(parseColor(hex, Color.Gray))
                    .border(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
                    .clickable { onPick(hex) }
            )
        }
    }
}

private fun parseColor(hex: String, fallback: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(fallback)

/**
 * Quick CIE-style luminance approximation — we only use it to pick black vs off-white text
 * over the cover background, so a 3-channel weighted average is enough.
 */
private fun Color.relativeLuminance(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue

package com.nexos.ai.presentation.ui.settings

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
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.ai.AIRouter
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosBorder
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft
import com.nexos.ai.presentation.viewmodel.SettingsViewModel
import com.nexos.ai.service.FloatingButtonService
import com.nexos.ai.util.canDrawOverlays
import com.nexos.ai.util.openOverlaySettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val keyDrafts = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(state.testResult) {
        state.testResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearTestResult()
        }
    }

    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexosBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionTitle("AI provider")
            Text(
                "NexOS works without AI. Add a key only to upgrade notes with summaries. " +
                    "Keys are encrypted with the Android Keystore and never leave your device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.providers.forEach { info ->
                ProviderCard(
                    info = info,
                    isActive = info.key == state.activeProvider,
                    hasKey = state.keyStatuses[info.key] == true,
                    draftKey = keyDrafts[info.key].orEmpty(),
                    onSelect = { viewModel.selectProvider(info.key) },
                    onKeyChange = { keyDrafts[info.key] = it },
                    onKeySave = {
                        viewModel.setApiKey(info.key, keyDrafts[info.key].orEmpty())
                        keyDrafts[info.key] = ""
                    }
                )
            }

            if (state.activeProvider != "none") {
                TextButton(
                    onClick = { viewModel.testActiveProvider() },
                    enabled = !state.isTesting
                ) {
                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = NexosPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isTesting) "Testing…" else "Test connection")
                }
            }

            SectionTitle("Behavior")
            ToggleRow(
                title = "Auto-summarize captures",
                subtitle = "Run AI on screenshots and voice notes when a key is set.",
                checked = state.autoSummarize,
                onCheckedChange = viewModel::setAutoSummarize
            )
            ToggleRow(
                title = "Show floating button",
                subtitle = "Always-on overlay so you can capture from any app.",
                checked = state.showFloatingButton,
                onCheckedChange = { value ->
                    if (value) {
                        if (!context.canDrawOverlays()) {
                            context.openOverlaySettings()
                        } else {
                            FloatingButtonService.startCompat(
                                context,
                                Intent(context, FloatingButtonService::class.java)
                            )
                        }
                    } else {
                        context.stopService(Intent(context, FloatingButtonService::class.java))
                    }
                    viewModel.setShowFloatingButton(value)
                }
            )

            SectionTitle("Storage")
            TextButton(onClick = { viewModel.clearImageCache() }) {
                Text("Clear image cache")
            }

            SectionTitle("About")
            Text(
                "NexOS MVP · v1.0\n" +
                    "All processing happens on this device. The optional AI provider you select " +
                    "only sees the text you choose to send to it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SectionTitle(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = NexosPrimary,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
    )
}

@Composable
private fun ProviderCard(
    info: AIRouter.ProviderInfo,
    isActive: Boolean,
    hasKey: Boolean,
    draftKey: String,
    onSelect: () -> Unit,
    onKeyChange: (String) -> Unit,
    onKeySave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) NexosPrimary else NexosBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    info.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    info.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(NexosPrimarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = "Active", tint = NexosPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (info.configurable) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = draftKey,
                onValueChange = onKeyChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (hasKey) "Key stored — replace to update" else "Paste API key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NexosBackground,
                    unfocusedContainerColor = NexosBackground,
                    focusedIndicatorColor = NexosPrimary,
                    unfocusedIndicatorColor = NexosBorder
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (hasKey) "Key stored securely." else "No key stored yet.",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasKey) NexosPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onKeySave, enabled = draftKey.isNotBlank()) {
                    Text(if (hasKey) "Replace" else "Save")
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = NexosPrimary,
                checkedThumbColor = NexosBackground,
                uncheckedTrackColor = NexosBorder,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

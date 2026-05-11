package com.nexos.ai.presentation.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.R
import com.nexos.ai.ai.AIProviders
import com.nexos.ai.presentation.viewmodel.SettingsViewModel
import com.nexos.ai.util.canDrawOverlays
import com.nexos.ai.util.hasPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val provider by viewModel.provider.collectAsStateWithLifecycle()
    val autoSummarize by viewModel.autoSummarize.collectAsStateWithLifecycle()
    val autoSave by viewModel.autoSave.collectAsStateWithLifecycle()
    val showFloating by viewModel.showFloatingButton.collectAsStateWithLifecycle()
    val keyInput by viewModel.apiKeyInput.collectAsStateWithLifecycle()
    val hasStoredKey by viewModel.hasStoredKey.collectAsStateWithLifecycle()
    val toast by viewModel.toasts.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val micGranted = context.hasPermission(Manifest.permission.RECORD_AUDIO)
    val overlayGranted = context.canDrawOverlays()
    val notificationGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)

    LaunchedEffect(toast) {
        val msg = toast
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeToast()
        }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.screen_settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Section(title = stringResource(R.string.settings_section_capture)) {
                SwitchRow(
                    title = stringResource(R.string.settings_show_button),
                    subtitle = stringResource(R.string.settings_show_button_sub),
                    checked = showFloating,
                    onChange = viewModel::setShowFloatingButton
                )
                SwitchRow(
                    title = stringResource(R.string.settings_auto_save),
                    subtitle = stringResource(R.string.settings_auto_save_sub),
                    checked = autoSave,
                    onChange = viewModel::setAutoSave
                )
                SwitchRow(
                    title = stringResource(R.string.settings_auto_summarize),
                    subtitle = stringResource(R.string.settings_auto_summarize_sub),
                    checked = autoSummarize,
                    onChange = viewModel::setAutoSummarize
                )
            }

            Section(title = stringResource(R.string.settings_section_ai)) {
                Text(
                    text = stringResource(R.string.settings_provider),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AIProviders.ALL.forEach { key ->
                    ProviderOption(
                        label = AIProviders.displayName(key),
                        selected = provider == key,
                        onClick = { viewModel.setProvider(key) }
                    )
                }

                if (provider != AIProviders.NONE) {
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = viewModel::onApiKeyInputChange,
                        label = { Text(stringResource(R.string.settings_api_key)) },
                        placeholder = { Text(stringResource(R.string.settings_api_key_hint)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (hasStoredKey) {
                        Text(
                            text = "A key is currently saved for ${AIProviders.displayName(provider)}.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = viewModel::saveApiKey,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Text(stringResource(R.string.settings_api_key_save)) }
                        OutlinedButton(onClick = viewModel::testConnection) {
                            Text(stringResource(R.string.settings_test_connection))
                        }
                        if (hasStoredKey) {
                            TextButton(onClick = viewModel::clearApiKey) {
                                Text("Forget key", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Section(title = stringResource(R.string.settings_section_permissions)) {
                PermissionRow(
                    title = stringResource(R.string.permission_overlay),
                    subtitle = stringResource(R.string.permission_overlay_sub),
                    granted = overlayGranted,
                    onGrant = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                )
                PermissionRow(
                    title = stringResource(R.string.permission_microphone),
                    subtitle = stringResource(R.string.permission_microphone_sub),
                    granted = micGranted,
                    onGrant = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )
                PermissionRow(
                    title = stringResource(R.string.permission_notifications),
                    subtitle = stringResource(R.string.permission_notifications_sub),
                    granted = notificationGranted,
                    onGrant = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                PermissionRow(
                    title = stringResource(R.string.permission_battery),
                    subtitle = stringResource(R.string.permission_battery_sub),
                    granted = false,
                    onGrant = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        runCatching { context.startActivity(intent) }
                    }
                )
            }

            Section(title = stringResource(R.string.settings_section_storage)) {
                OutlinedButton(
                    onClick = viewModel::clearImageCache,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_clear_cache))
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ProviderOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (granted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = stringResource(R.string.permission_granted),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        } else {
            OutlinedButton(onClick = onGrant) {
                Text(stringResource(R.string.permission_grant))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary
)

package com.nexos.ai.presentation.ui.settings

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.PhoneAndroid
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.ai.AIRouter
import com.nexos.ai.data.repository.ThemeMode
import com.nexos.ai.presentation.ui.components.PandaMascot
import com.nexos.ai.presentation.ui.components.PandaMotion
import com.nexos.ai.presentation.ui.components.PandaSectionIcon
import com.nexos.ai.presentation.ui.components.PandaSectionKind
import com.nexos.ai.presentation.viewmodel.SettingsViewModel
import com.nexos.ai.presentation.viewmodel.ThemeViewModel
import com.nexos.ai.service.FloatingButtonService
import com.nexos.ai.util.canDrawOverlays
import com.nexos.ai.util.openOverlaySettings

/**
 * Settings — PRD F8 redesign. Every section header has its own animated panda glyph
 * (PandaSectionIcon). The Theme section gets a Sun/Panda/Moon segmented control that
 * persists the choice through ThemeViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val themeMode by themeViewModel.mode.collectAsStateWithLifecycle()
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PandaMascot(size = 28.dp, hasLeaf = false, motion = PandaMotion.Wiggle)
                        Spacer(Modifier.width(10.dp))
                        Text("Settings")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            // --- Theme ---
            SectionHeader(
                kind = PandaSectionKind.Theme,
                title = "THEME",
                subtitle = "How NexOS should look",
                isDark = themeMode == ThemeMode.Dark || (themeMode == ThemeMode.System && isSystemDark())
            )
            ThemeSegmentedControl(
                current = themeMode,
                onSelect = themeViewModel::setMode
            )

            // --- AI provider ---
            SectionHeader(PandaSectionKind.AiProvider, "AI PROVIDER",
                "NexOS works without AI — keys upgrade notes with summaries")
            Text(
                "Keys are encrypted with the Android Keystore and never leave your device. " +
                    "Headers only — never URL parameters.",
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
                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isTesting) "Testing…" else "Test connection")
                }
            }

            // --- API keys (news / GNews) ---
            SectionHeader(PandaSectionKind.ApiKeys, "GNEWS API KEY",
                "Pre-seeded on first launch · free at gnews.io")
            Text(
                "A free GNews developer key is already loaded so news works out-of-the-box. " +
                    "Paste your own key here to use a higher tier or rotate. Keys stay encrypted " +
                    "on this device and the value is masked in URLs before it reaches Logcat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ApiKeyCard(
                placeholder = "Paste GNews API key",
                hasKey = state.keyStatuses["newsapi"] == true,
                draftKey = keyDrafts["newsapi"].orEmpty(),
                onKeyChange = { keyDrafts["newsapi"] = it },
                onKeySave = {
                    viewModel.setApiKey("newsapi", keyDrafts["newsapi"].orEmpty())
                    keyDrafts["newsapi"] = ""
                }
            )

            // --- Weather provider ---
            SectionHeader(PandaSectionKind.News, "WEATHER PROVIDER",
                "OpenWeather (pre-seeded) → falls back to Open-Meteo")
            Text(
                "Forecast uses OpenWeather when a key is set, Open-Meteo otherwise. " +
                    "OpenWeather's free tier gives 60 req/min and current-weather accuracy at the named city. " +
                    "Clear the key to use the no-key Open-Meteo path.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ApiKeyCard(
                placeholder = "Paste OpenWeather API key",
                hasKey = state.keyStatuses["openweather"] == true,
                draftKey = keyDrafts["openweather"].orEmpty(),
                onKeyChange = { keyDrafts["openweather"] = it },
                onKeySave = {
                    viewModel.setApiKey("openweather", keyDrafts["openweather"].orEmpty())
                    keyDrafts["openweather"] = ""
                }
            )

            // --- Capture / floating button ---
            SectionHeader(PandaSectionKind.Floating, "CAPTURE",
                "Floating overlay button + screen-capture behaviour")
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
                        if (!context.canDrawOverlays()) context.openOverlaySettings()
                        else FloatingButtonService.startCompat(
                            context, Intent(context, FloatingButtonService::class.java)
                        )
                    } else {
                        context.stopService(Intent(context, FloatingButtonService::class.java))
                    }
                    viewModel.setShowFloatingButton(value)
                }
            )

            // --- Storage ---
            SectionHeader(PandaSectionKind.Storage, "STORAGE",
                "Screen-capture cache — cleared anytime")
            TextButton(onClick = { viewModel.clearImageCache() }) {
                Text("Clear image cache")
            }

            // --- About ---
            SectionHeader(PandaSectionKind.AboutMe, "ABOUT",
                "App version, licence, panda")
            Text(
                "NexOS · v${com.nexos.ai.BuildConfig.VERSION_NAME}\n" +
                    "All processing happens on this device. The optional AI provider you select " +
                    "only sees the text you choose to send to it. Apache 2.0 · free forever.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PandaMascot(size = 64.dp, hasLeaf = true, motion = PandaMotion.Bouncing)
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun isSystemDark(): Boolean = androidx.compose.foundation.isSystemInDarkTheme()

@Composable
private fun SectionHeader(
    kind: PandaSectionKind,
    title: String,
    subtitle: String,
    isDark: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PandaSectionIcon(kind = kind, size = 38.dp, isDarkMode = isDark)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeSegmentedControl(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeSegment(
            label = "Light", icon = Icons.Rounded.LightMode,
            selected = current == ThemeMode.Light,
            onClick = { onSelect(ThemeMode.Light) },
            modifier = Modifier.weight(1f)
        )
        ThemeSegment(
            label = "System", icon = Icons.Rounded.PhoneAndroid,
            selected = current == ThemeMode.System,
            onClick = { onSelect(ThemeMode.System) },
            modifier = Modifier.weight(1f)
        )
        ThemeSegment(
            label = "Dark", icon = Icons.Rounded.DarkMode,
            selected = current == ThemeMode.Dark,
            onClick = { onSelect(ThemeMode.Dark) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeSegment(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) MaterialTheme.colorScheme.primary
    else androidx.compose.ui.graphics.Color.Transparent
    val content = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "theme-segment-anim"
        ) { sel ->
            Icon(icon, contentDescription = label, tint = content, modifier = Modifier.size(if (sel) 22.dp else 18.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = content,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
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
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(info.displayName, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(info.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (hasKey) "Key stored securely." else "No key stored yet.",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasKey) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
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
private fun ApiKeyCard(
    placeholder: String,
    hasKey: Boolean,
    draftKey: String,
    onKeyChange: (String) -> Unit,
    onKeySave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = draftKey,
            onValueChange = onKeyChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(if (hasKey) "Key stored — replace to update" else placeholder) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (hasKey) "Key stored securely." else "No key stored yet.",
                style = MaterialTheme.typography.labelSmall,
                color = if (hasKey) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onKeySave, enabled = draftKey.isNotBlank()) {
                Text(if (hasKey) "Replace" else "Save")
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
            Text(title, style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

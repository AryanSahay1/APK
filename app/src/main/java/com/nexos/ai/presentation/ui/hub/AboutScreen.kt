package com.nexos.ai.presentation.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexos.ai.BuildConfig
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = { Text("About & privacy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.Start
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                com.nexos.ai.presentation.ui.components.PandaMascot(size = 56.dp, hasLeaf = true)
                Spacer(Modifier.height(0.dp))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("NexOS · v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Apache License 2.0 · Open source",
                        style = MaterialTheme.typography.labelMedium, color = NexosPrimary)
                }
            }
            Section("Free, forever") {
                "Every feature is free. Every future update is free. No subscriptions, no ads, no telemetry. " +
                    "If a feature needs a third-party service, you supply your own API key — NexOS only " +
                    "stores it on your device, encrypted with the Android Keystore."
            }
            Section("Your data stays here") {
                "Notes, screenshots, transcripts, alarms — all stored locally in Room. No backend. " +
                    "Audio is processed on-device by Android's built-in speech recognizer."
            }
            Section("Third parties used (optional)") {
                "AI summaries: OpenAI, Anthropic, Google Gemini, or Groq — only when you paste a key.\n" +
                    "News headlines: NewsAPI — only when you paste a key.\n" +
                    "Uber, Rapido, Zomato, Swiggy: public deep links only. NexOS never talks to their servers."
            }
            Section("Permissions explained") {
                "SYSTEM_ALERT_WINDOW — floating capture button.\n" +
                    "FOREGROUND_SERVICE_MEDIA_PROJECTION — screen capture.\n" +
                    "RECORD_AUDIO — voice notes (processed on-device).\n" +
                    "POST_NOTIFICATIONS — save confirmations + alarm rings.\n" +
                    "SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM — reminders that fire on time.\n" +
                    "INTERNET — only used when you enable an AI or news provider."
            }
            Section("Trademarks") {
                "Uber, Rapido, Zomato, Swiggy, OpenAI, Anthropic, Gemini, and Groq are trademarks of " +
                    "their respective owners. NexOS is unaffiliated with any of them."
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Section(title: String, body: () -> String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(body(), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

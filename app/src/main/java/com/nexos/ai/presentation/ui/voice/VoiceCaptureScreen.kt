package com.nexos.ai.presentation.ui.voice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.domain.model.VoiceState
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft
import com.nexos.ai.presentation.viewmodel.VoiceCaptureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCaptureScreen(
    onDone: () -> Unit,
    viewModel: VoiceCaptureViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var permissionGranted by remember { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
        if (granted) viewModel.startListening()
    }

    LaunchedEffect(Unit) {
        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(ui.saved) {
        if (ui.saved) onDone()
    }

    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = { Text("Voice note", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopListening()
                        onDone()
                    }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedMic(state = ui.state)
            Spacer(Modifier.height(32.dp))
            Text(
                text = statusLine(ui.state),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = ui.partial.ifBlank { ui.finalText.ifBlank { "Tap below to start speaking. Audio is processed on-device." } },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    if (ui.state is VoiceState.Listening || ui.state is VoiceState.Partial) {
                        viewModel.stopListening()
                    } else {
                        if (permissionGranted) viewModel.startListening()
                        else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexosPrimary, contentColor = NexosBackground)
            ) {
                Icon(Icons.Rounded.Mic, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(
                    when (ui.state) {
                        is VoiceState.Listening, is VoiceState.Partial -> "Stop"
                        else -> "Start listening"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AnimatedMic(state: VoiceState) {
    val active = state is VoiceState.Listening || state is VoiceState.Partial
    val transition = rememberInfiniteTransition(label = "mic-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (active) 1.15f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "mic-pulse-val"
    )
    Box(
        modifier = Modifier
            .size(160.dp)
            .scale(pulse)
            .clip(CircleShape)
            .background(NexosPrimarySoft),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(NexosPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Mic,
                contentDescription = null,
                tint = NexosBackground,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

private fun statusLine(state: VoiceState): String = when (state) {
    VoiceState.Idle -> "Ready to listen"
    VoiceState.Listening -> "Listening…"
    is VoiceState.Partial -> "Listening…"
    is VoiceState.Result -> "Saving note…"
    is VoiceState.Error -> state.message
}

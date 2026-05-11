package com.nexos.ai.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.nexos.ai.R
import com.nexos.ai.voice.VoiceState
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputBottomSheet(
    state: VoiceState,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state) {
        if (state is VoiceState.Result) {
            onSubmit(state.text)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (state) {
                    is VoiceState.Listening, is VoiceState.Partial -> stringRes(R.string.voice_listening)
                    is VoiceState.Error -> state.message
                    else -> stringRes(R.string.voice_idle)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Waveform(
                amplitude = (state as? VoiceState.Partial)?.amplitude ?: 0f,
                active = state is VoiceState.Listening || state is VoiceState.Partial
            )

            Spacer(modifier = Modifier.height(8.dp))
            val partial = (state as? VoiceState.Partial)?.text.orEmpty()
            if (partial.isNotBlank()) {
                Text(
                    text = partial,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringRes(R.string.cd_close_voice),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        if (state is VoiceState.Listening || state is VoiceState.Partial)
                            Icons.Outlined.Stop
                        else
                            Icons.Outlined.Mic,
                        contentDescription = stringRes(R.string.cd_microphone),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Waveform(amplitude: Float, active: Boolean) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200), repeatMode = RepeatMode.Restart
        ),
        label = "waveformPhase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        val primary = MaterialTheme.colorScheme.primary
        Canvas(modifier = Modifier.fillMaxWidth().height(72.dp)) {
            val bars = 28
            val w = size.width / bars
            val maxH = size.height
            val base = if (active) 0.15f else 0.08f
            val seedAmp = amplitude.coerceIn(0f, 1f)
            for (i in 0 until bars) {
                val noise = Random(i * 31 + (phase * 100).toInt()).nextFloat() * 0.15f
                val wave = (sin((phase + i * 0.4f).toDouble()).toFloat() + 1f) / 2f
                val h = (base + wave * (0.25f + seedAmp) + noise) * maxH
                val x = i * w + w / 2f
                drawLine(
                    brush = SolidColor(primary),
                    start = Offset(x, (maxH - h) / 2f),
                    end = Offset(x, (maxH + h) / 2f),
                    strokeWidth = w * 0.45f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun stringRes(id: Int): String = androidx.compose.ui.res.stringResource(id)

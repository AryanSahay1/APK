package com.nexos.ai.presentation.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexos.ai.presentation.ui.components.PandaMascot
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosBorder
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft

/**
 * Three-step onboarding:
 *   1. Welcome — meet the panda.
 *   2. Features — what NexOS does at a glance.
 *   3. Permissions — what we'll ask for and why.
 *
 * Always free, no paywall, accessible without granting anything — the final CTA is
 * always "Get started", which proceeds even if the user skipped optional grants.
 */
@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    onRequestScreenCapture: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val totalSteps = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexosBackground)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PandaMascot(size = 28.dp, hasLeaf = false)
                Spacer(Modifier.width(8.dp))
                Text("NexOS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            ProgressDots(current = step, total = totalSteps)
        }

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (slideInHorizontally(animationSpec = tween(280)) { it / 8 } + fadeIn()) togetherWith
                    (slideOutHorizontally(animationSpec = tween(180)) { -it / 8 } + fadeOut())
            },
            label = "onboarding-step"
        ) { current ->
            when (current) {
                0 -> WelcomeStep()
                1 -> FeaturesStep()
                else -> PermissionsStep(onRequestScreenCapture = onRequestScreenCapture)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                TextButton(onClick = { step -= 1 }) { Text("Back") }
            } else {
                Spacer(Modifier.width(72.dp))
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (step < totalSteps - 1) step += 1 else onContinue()
                },
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexosPrimary, contentColor = NexosBackground
                )
            ) {
                Text(
                    if (step < totalSteps - 1) "Continue" else "Get started",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProgressDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(width = if (i == current) 18.dp else 6.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (i == current) NexosPrimary else NexosBorder)
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PandaMascot(size = 140.dp, hasLeaf = true)
        Spacer(Modifier.height(28.dp))
        Text(
            "Meet your panda",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "An on-device AI layer that captures, transcribes, summarises, schedules, and books — all in one place. Forever free.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeaturesStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Everything in one place",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Tap a tile from the Hub or the floating button.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        FeatureBullet(Icons.Rounded.CenterFocusStrong, "Screen capture → AI summary",
            "Tap the floating button to OCR any screen and save a structured note.")
        FeatureBullet(Icons.Rounded.Mic, "Voice notes",
            "Long-press the floating button to dictate a thought; transcribed on-device.")
        FeatureBullet(Icons.AutoMirrored.Rounded.Article, "Personalised news",
            "Browse headlines by category. Save articles as notes with one tap.")
        FeatureBullet(Icons.Rounded.Alarm, "Natural-language reminders",
            "\"Remind me at 8am tomorrow\" — it just works.")
        FeatureBullet(Icons.Rounded.LocalDining, "Rides & food, one tap",
            "Uber, Rapido, Zomato, Swiggy — all from the Hub.")
    }
}

@Composable
private fun FeatureBullet(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NexosPrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NexosPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionsStep(onRequestScreenCapture: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Permissions, transparently",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "You can skip these — NexOS still works in local-only mode.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        PermBullet("Overlay", "Lets the floating capture button sit on top of any app.")
        PermBullet("Screen capture", "Required for the capture-to-note flow. You confirm each session.")
        PermBullet("Microphone", "Voice notes only. Audio is processed on-device and discarded.")
        PermBullet("Notifications", "Save confirmations + alarm rings.")
        PermBullet("Exact alarms", "Reminders fire on time even on aggressive OEMs.")
        OutlinedButton(
            onClick = onRequestScreenCapture,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Grant screen capture now (optional)") }
    }
}

@Composable
private fun PermBullet(title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.AutoAwesome, contentDescription = null,
            tint = NexosPrimary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

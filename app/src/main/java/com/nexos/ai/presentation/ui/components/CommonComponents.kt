package com.nexos.ai.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexos.ai.domain.model.WorkflowState
import com.nexos.ai.presentation.ui.theme.NexosError
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft
import com.nexos.ai.presentation.ui.theme.NexosSurfaceElevated

@Composable
fun WorkflowBanner(state: WorkflowState, modifier: Modifier = Modifier) {
    // Each state maps to (label, step index, isError). The step index drives the
    // "step 2 / 4" counter so the user always knows how much of the pipeline remains.
    data class BannerInfo(val label: String, val step: Int, val isError: Boolean = false)
    val totalSteps = 4
    val info: BannerInfo? = when (state) {
        WorkflowState.Idle -> null
        WorkflowState.Capturing -> BannerInfo("Capturing screen", 1)
        WorkflowState.ExtractingText -> BannerInfo("Extracting text", 2)
        WorkflowState.AiProcessing -> BannerInfo("Asking AI", 3)
        WorkflowState.Saving -> BannerInfo("Saving note", 4)
        is WorkflowState.Done -> null
        is WorkflowState.Failed -> BannerInfo(state.error, 0, isError = true)
    }
    AnimatedVisibility(visible = info != null, modifier = modifier) {
        val current = info ?: return@AnimatedVisibility
        val infinite = rememberInfiniteTransition(label = "banner-pulse")
        val pulse by infinite.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
            label = "banner-pulse-val"
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(NexosSurfaceElevated)
                .border(1.dp, NexosPrimarySoft, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = if (current.isError) NexosError else NexosPrimary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.alpha(pulse)) {
                Text(
                    text = current.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!current.isError) {
                    Text(
                        text = "Step ${current.step} of $totalSteps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    showPanda: Boolean = true,
    pandaSleeping: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showPanda) {
            PandaMascot(size = 96.dp, hasLeaf = true, sleeping = pandaSleeping)
        } else if (icon != null) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(NexosPrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NexosPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

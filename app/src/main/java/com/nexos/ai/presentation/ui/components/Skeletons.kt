package com.nexos.ai.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexos.ai.presentation.ui.theme.NexosBorder
import com.nexos.ai.presentation.ui.theme.NexosSurface
import com.nexos.ai.presentation.ui.theme.NexosSurfaceElevated

/**
 * A shimmering rectangle used as a loading placeholder. The shimmer is implemented as a
 * sliding linear gradient over a fixed-color box — the gradient never changes alpha so
 * the GPU keeps it on the compositor layer (per SKILL.md §24 performance rules).
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp,
    baseColor: Color = NexosSurface,
    highlightColor: Color = NexosSurfaceElevated
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(animation = tween(1400), repeatMode = RepeatMode.Restart),
        label = "shimmer-progress"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(baseColor)
            .drawWithCache {
                val width = size.width
                val gradient = Brush.linearGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    start = androidx.compose.ui.geometry.Offset(progress * width, 0f),
                    end = androidx.compose.ui.geometry.Offset((progress + 1f) * width, 0f)
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(brush = gradient)
                }
            }
    )
}

@Composable
fun NewsSkeletonList(itemCount: Int = 4, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            NewsSkeletonCard()
        }
    }
}

@Composable
private fun NewsSkeletonCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NexosSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShimmerBox(modifier = Modifier.width(70.dp).height(10.dp), cornerRadius = 4.dp)
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(18.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.85f).height(18.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerBox(modifier = Modifier.width(72.dp).height(28.dp), cornerRadius = 14.dp)
            ShimmerBox(modifier = Modifier.width(104.dp).height(28.dp), cornerRadius = 14.dp)
        }
    }
}

@Composable
fun NotesSkeletonList(itemCount: Int = 3, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(NexosSurface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(modifier = Modifier.size(34.dp), cornerRadius = 17.dp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(16.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(10.dp))
                }
            }
        }
    }
}

/** Helper for borders that want the standard NexOS border colour. */
internal val NexosBorderColor: Color get() = NexosBorder

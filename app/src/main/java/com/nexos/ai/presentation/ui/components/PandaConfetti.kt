package com.nexos.ai.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.sin
import kotlin.random.Random

/**
 * Scatters up to [count] small panda variants across the available area at random-but-stable
 * positions. Each panda bobs gently in place. Designed as a friendly background decoration
 * for hero screens (onboarding, empty states, hub header), NOT as a foreground UI element.
 *
 * Key design choices:
 *   - **Random-but-stable**: positions are derived from a [seed] and a per-panda Random.
 *     With the same seed the layout is identical across recompositions, so the pandas don't
 *     "jump around" when the parent re-composes. Pass a different seed to get a fresh
 *     arrangement (e.g. tied to the screen name).
 *   - **Not clustered**: the scatter uses a stratified-random distribution — a 6×5 grid is
 *     pre-divided, each panda is dropped into its own cell with random jitter inside the
 *     cell. Result: even spread, no two pandas overlap meaningfully.
 *   - **All different**: variants are drawn without replacement until the variant pool is
 *     exhausted, then it cycles.
 *   - **Lightweight motion**: each panda has a unique phase offset on a sin-driven vertical
 *     bob (only `transform` animations, no opacity, no layout invalidation).
 *
 * Pass the composable as a *background* layer inside a `Box`:
 * ```
 * Box(Modifier.fillMaxSize()) {
 *     PandaConfetti(count = 18, seed = 17)
 *     // foreground content goes here
 * }
 * ```
 */
@Composable
fun PandaConfetti(
    modifier: Modifier = Modifier,
    count: Int = 18,
    pandaSize: Dp = 28.dp,
    seed: Int = 0,
    alphaRange: ClosedFloatingPointRange<Float> = 0.18f..0.42f,
    variants: List<PandaVariant> = PandaVariant.entries
) {
    val transition = rememberInfiniteTransition(label = "panda-confetti")
    // 0..1 over 4 seconds; each panda samples this with a unique phase offset to bob.
    val tick by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Restart),
        label = "panda-confetti-tick"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxW = maxWidth
        val maxH = maxHeight
        // Build placements in dp-space (independent of pixel density). Convert to pixels at
        // render time via LocalDensity.
        val placements = remember(count, seed, variants.size, maxW, maxH) {
            buildPlacements(
                count = count,
                seed = seed,
                areaWidthDp = maxW.value,
                areaHeightDp = maxH.value,
                pandaSizeDp = pandaSize.value,
                variants = variants,
                alphaRange = alphaRange
            )
        }

        placements.forEach { placement ->
            val bob = sin((tick + placement.phase) * 2f * Math.PI.toFloat()).toFloat() * placement.bobAmpDp
            Box(
                modifier = Modifier
                    .offset {
                        with(density) {
                            IntOffset(
                                placement.xDp.dp.roundToPx(),
                                (placement.yDp + bob).dp.roundToPx()
                            )
                        }
                    }
                    .size(pandaSize)
                    .alpha(placement.alpha)
                    .rotate(placement.rotationDeg)
            ) {
                PandaVariantIcon(variant = placement.variant, size = pandaSize)
            }
        }
    }
}

private data class PandaPlacement(
    val variant: PandaVariant,
    val xDp: Float,
    val yDp: Float,
    val alpha: Float,
    val rotationDeg: Float,
    val phase: Float,
    val bobAmpDp: Float
)

private fun buildPlacements(
    count: Int,
    seed: Int,
    areaWidthDp: Float,
    areaHeightDp: Float,
    pandaSizeDp: Float,
    variants: List<PandaVariant>,
    alphaRange: ClosedFloatingPointRange<Float>
): List<PandaPlacement> {
    if (count <= 0 || areaWidthDp <= 0f || areaHeightDp <= 0f) return emptyList()

    val rng = Random(seed.toLong() or (areaWidthDp.toInt().toLong() shl 16))
    val aspect = areaWidthDp / areaHeightDp.coerceAtLeast(1f)
    val cols = kotlin.math.ceil(kotlin.math.sqrt(count * aspect.absoluteValue.coerceAtLeast(0.1f).toDouble()))
        .toInt().coerceAtLeast(1)
    val rows = kotlin.math.ceil(count.toDouble() / cols).toInt().coerceAtLeast(1)

    val cellWidth = areaWidthDp / cols
    val cellHeight = areaHeightDp / rows

    val shuffledVariants = variants.shuffled(rng)
    val placements = mutableListOf<PandaPlacement>()
    var index = 0

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            if (placements.size >= count) return placements
            val variant = shuffledVariants[index % shuffledVariants.size]
            index += 1

            val margin = (pandaSizeDp * 0.25f).coerceAtMost(cellWidth * 0.2f)
            val jitterX = rng.nextFloat() * (cellWidth - pandaSizeDp - margin * 2).coerceAtLeast(0f)
            val jitterY = rng.nextFloat() * (cellHeight - pandaSizeDp - margin * 2).coerceAtLeast(0f)
            val x = c * cellWidth + margin + jitterX
            val y = r * cellHeight + margin + jitterY

            placements += PandaPlacement(
                variant = variant,
                xDp = x,
                yDp = y,
                alpha = alphaRange.start + rng.nextFloat() * (alphaRange.endInclusive - alphaRange.start),
                rotationDeg = (rng.nextFloat() - 0.5f) * 20f,
                phase = rng.nextFloat(),
                bobAmpDp = 2f + rng.nextFloat() * 4f
            )
        }
    }
    return placements
}

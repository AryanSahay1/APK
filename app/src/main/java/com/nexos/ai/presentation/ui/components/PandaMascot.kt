package com.nexos.ai.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate as rotateScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

/**
 * Mascot motion variant — applied as Compose modifiers on top of the static Canvas drawing.
 *
 * All motions animate only `transform` and `opacity` (per SKILL.md §24 performance rule).
 * When the system "Remove animations" setting is active (animator scale = 0), motions
 * collapse to their static frame.
 */
enum class PandaMotion { None, Breathing, Wiggle, Wave, Bouncing, Loading }

/**
 * Hand-drawn panda doodle rendered in Compose so the mascot scales crisply and animates
 * without us having to bake every variant into XML drawables. Pure Canvas — no platform
 * Drawable inflation — which means the same primitive is used for the welcome hero,
 * the onboarding pager, and the about screen.
 *
 * The doodle is laid out on an internal 100x100 grid; the [size] modifier scales it uniformly.
 *
 * @param size diameter of the bounding box.
 * @param hasLeaf when true, draws the small green bamboo-leaf accent above the right ear.
 * @param sleeping when true, swaps the eyes for closed-eye arcs — used by skeleton/empty states.
 * @param motion which idle animation to apply. Defaults to [PandaMotion.Breathing] for a
 *               subtle "alive" feel everywhere the mascot appears.
 */
@Composable
fun PandaMascot(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    hasLeaf: Boolean = true,
    sleeping: Boolean = false,
    motion: PandaMotion = PandaMotion.Breathing,
    bodyColor: Color = Color(0xFFFAFAFA),
    inkColor: Color = Color(0xFF0F0F14),
    accentColor: Color = Color(0xFF00E676)
) {
    val reduceMotion = rememberReduceMotion()
    val effectiveMotion = if (reduceMotion) PandaMotion.None else motion
    val motionMod = pandaMotionModifier(effectiveMotion)
    val leafRotation = if (effectiveMotion == PandaMotion.Wave) leafWaveAngle() else 0f

    Box(modifier = modifier.then(motionMod).size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            drawPandaInto(
                drawScope = this,
                bodyColor = bodyColor,
                inkColor = inkColor,
                accentColor = accentColor,
                hasLeaf = hasLeaf,
                sleeping = sleeping,
                leafRotationDeg = leafRotation
            )
        }
    }
}

@Composable
private fun pandaMotionModifier(motion: PandaMotion): Modifier {
    return when (motion) {
        PandaMotion.None -> Modifier
        PandaMotion.Breathing -> {
            val t = rememberInfiniteTransition(label = "panda-breath")
            val scale by t.animateFloat(
                0.97f, 1.0f,
                animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
                label = "panda-breath-scale"
            )
            Modifier.scale(scale)
        }
        PandaMotion.Wiggle -> {
            val t = rememberInfiniteTransition(label = "panda-wiggle")
            val deg by t.animateFloat(
                -3f, 3f,
                animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                label = "panda-wiggle-deg"
            )
            Modifier.rotate(deg)
        }
        PandaMotion.Wave -> Modifier // handled inside Canvas via leaf rotation
        PandaMotion.Bouncing -> {
            val t = rememberInfiniteTransition(label = "panda-bounce")
            val y by t.animateFloat(
                0f, -6f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "panda-bounce-y"
            )
            Modifier.intOffset(0, y.toInt())
        }
        PandaMotion.Loading -> {
            val t = rememberInfiniteTransition(label = "panda-spin")
            val deg by t.animateFloat(
                0f, 360f,
                animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
                label = "panda-spin-deg"
            )
            Modifier.rotate(deg)
        }
    }
}

private fun Modifier.intOffset(x: Int, y: Int): Modifier =
    this.offset { IntOffset(x, y) }

@Composable
private fun leafWaveAngle(): Float {
    val t = rememberInfiniteTransition(label = "panda-leaf-wave")
    val deg by t.animateFloat(
        0f, 20f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "panda-leaf-wave-deg"
    )
    return deg
}

@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    val scale = android.provider.Settings.Global.getFloat(
        context.contentResolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )
    return scale == 0f
}

private fun drawPandaInto(
    drawScope: DrawScope,
    bodyColor: Color,
    inkColor: Color,
    accentColor: Color,
    hasLeaf: Boolean,
    sleeping: Boolean,
    leafRotationDeg: Float = 0f
) {
    with(drawScope) {
        val w = size.width
        // Internal grid: 100 units wide. Drawables are designed square so we only need w.
        val unit = w / 100f

        fun u(value: Float): Float = value * unit
        fun circle(cx: Float, cy: Float, r: Float, color: Color) =
            drawCircle(color = color, radius = u(r), center = Offset(u(cx), u(cy)))

        // Ears
        circle(24f, 28f, 12f, inkColor)
        circle(76f, 28f, 12f, inkColor)
        circle(24f, 28f, 5.5f, Color(0xFF2A2A36))
        circle(76f, 28f, 5.5f, Color(0xFF2A2A36))

        // Head
        circle(50f, 56f, 30f, bodyColor)

        // Eye patches — teardrops drawn as paths
        val leftPatch = Path().apply {
            moveTo(u(38f), u(54f))
            cubicTo(u(28f), u(54f), u(26f), u(60f), u(28f), u(66f))
            cubicTo(u(30f), u(72f), u(36f), u(74f), u(42f), u(72f))
            cubicTo(u(48f), u(70f), u(50f), u(64f), u(48f), u(58f))
            cubicTo(u(46f), u(54f), u(42f), u(54f), u(38f), u(54f))
            close()
        }
        val rightPatch = Path().apply {
            moveTo(u(62f), u(54f))
            cubicTo(u(72f), u(54f), u(74f), u(60f), u(72f), u(66f))
            cubicTo(u(70f), u(72f), u(64f), u(74f), u(58f), u(72f))
            cubicTo(u(52f), u(70f), u(50f), u(64f), u(52f), u(58f))
            cubicTo(u(54f), u(54f), u(58f), u(54f), u(62f), u(54f))
            close()
        }
        drawPath(leftPatch, inkColor)
        drawPath(rightPatch, inkColor)

        // Eyes
        if (sleeping) {
            // Closed-eye arcs
            val arcStroke = Stroke(width = u(1.4f))
            drawArc(
                color = bodyColor,
                startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(u(34f), u(60f)), size = Size(u(12f), u(8f)),
                style = arcStroke
            )
            drawArc(
                color = bodyColor,
                startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(u(54f), u(60f)), size = Size(u(12f), u(8f)),
                style = arcStroke
            )
        } else {
            circle(40f, 64f, 3f, bodyColor)
            circle(60f, 64f, 3f, bodyColor)
            circle(40.6f, 64f, 1.2f, inkColor)
            circle(60.6f, 64f, 1.2f, inkColor)
        }

        // Nose
        val nose = Path().apply {
            moveTo(u(50f), u(74f))
            cubicTo(u(46f), u(74f), u(44f), u(78f), u(46f), u(82f))
            cubicTo(u(48f), u(86f), u(50f), u(86f), u(50f), u(86f))
            cubicTo(u(50f), u(86f), u(52f), u(86f), u(54f), u(82f))
            cubicTo(u(56f), u(78f), u(54f), u(74f), u(50f), u(74f))
            close()
        }
        drawPath(nose, inkColor)

        // Smile
        val smile = Path().apply {
            moveTo(u(44f), u(86f))
            cubicTo(u(46f), u(90f), u(48f), u(91f), u(50f), u(91f))
            cubicTo(u(52f), u(91f), u(54f), u(90f), u(56f), u(86f))
        }
        drawPath(smile, inkColor, style = Stroke(width = u(1.6f)))

        // Bamboo leaf — pivots around its stem base when waving
        if (hasLeaf) {
            val pivot = Offset(u(82f), u(20f))
            rotateScope(degrees = leafRotationDeg, pivot = pivot) {
                val leaf = Path().apply {
                    moveTo(u(82f), u(14f))
                    cubicTo(u(90f), u(8f), u(96f), u(10f), u(98f), u(16f))
                    cubicTo(u(96f), u(22f), u(90f), u(24f), u(82f), u(22f))
                    cubicTo(u(81f), u(20f), u(81f), u(16f), u(82f), u(14f))
                    close()
                }
                drawPath(leaf, accentColor)
                drawLine(
                    color = accentColor,
                    start = Offset(u(80f), u(24f)),
                    end = Offset(u(72f), u(34f)),
                    strokeWidth = u(1.5f)
                )
            }
        }
    }
}

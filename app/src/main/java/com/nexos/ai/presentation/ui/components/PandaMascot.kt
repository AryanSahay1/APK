package com.nexos.ai.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
 */
@Composable
fun PandaMascot(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    hasLeaf: Boolean = true,
    sleeping: Boolean = false,
    bodyColor: Color = Color(0xFFFAFAFA),
    inkColor: Color = Color(0xFF0F0F14),
    accentColor: Color = Color(0xFF00E676)
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            drawPandaInto(
                drawScope = this,
                bodyColor = bodyColor,
                inkColor = inkColor,
                accentColor = accentColor,
                hasLeaf = hasLeaf,
                sleeping = sleeping
            )
        }
    }
}

private fun drawPandaInto(
    drawScope: DrawScope,
    bodyColor: Color,
    inkColor: Color,
    accentColor: Color,
    hasLeaf: Boolean,
    sleeping: Boolean
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

        // Bamboo leaf
        if (hasLeaf) {
            val leaf = Path().apply {
                moveTo(u(82f), u(14f))
                cubicTo(u(90f), u(8f), u(96f), u(10f), u(98f), u(16f))
                cubicTo(u(96f), u(22f), u(90f), u(24f), u(82f), u(22f))
                cubicTo(u(81f), u(20f), u(81f), u(16f), u(82f), u(14f))
                close()
            }
            drawPath(leaf, accentColor)
            // Leaf stem
            drawLine(
                color = accentColor,
                start = Offset(u(80f), u(24f)),
                end = Offset(u(72f), u(34f)),
                strokeWidth = u(1.5f)
            )
        }
    }
}

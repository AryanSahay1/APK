package com.nexos.ai.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexos.ai.domain.model.AssistantContext
import com.nexos.ai.domain.model.WeatherCondition

/**
 * The big, fluffy panda. Designed for the assistant bottom sheet — much larger than
 * [PandaMascot], with visible body, paws holding a tablet, and "fluff" texture around the
 * head (a ring of small dots radiating from the silhouette).
 *
 * The tablet's display content is a switch on [context] so the user can see at-a-glance what
 * the panda is focused on right now: sun/cloud/rain icon in Weather mode, newspaper in News
 * mode, envelope in Email mode, etc.
 *
 * @param size diameter of the full mascot bounding box (panda + body + tablet).
 * @param weatherCondition optional — when in Weather mode, drives the icon shown on the
 *                         display (sun / cloud / rain / etc.).
 */
@Composable
fun FluffyPanda(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    context: AssistantContext = AssistantContext.Default,
    weatherCondition: WeatherCondition? = null,
    bodyColor: Color = Color(0xFFFAFAFA),
    inkColor: Color = Color(0xFF0F0F14),
    accentColor: Color = Color(0xFF00E676)
) {
    // Subtle breathing — applies to the head only, not the tablet, so the chat content stays
    // readable.
    val transition = rememberInfiniteTransition(label = "fluffy-breath")
    val breath by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "fluffy-breath-scale"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val u = w / 100f

        // — Tablet body in the panda's paws — drawn FIRST so the panda's paws/arms render on top.
        drawTablet(u, accentColor, bodyColor, inkColor, context, weatherCondition)

        // — Fluffy ring around the head silhouette: 28 small dots set at slight outward
        //   offsets so the head reads as "fluffy" rather than "shiny".
        drawFluffRing(u, bodyColor.copy(alpha = 0.85f), centerX = 50f, centerY = 30f, radius = 22f)

        // Apply a subtle breath scale to the head only, drawn from its own center.
        val headCx = u * 50
        val headCy = u * 30
        // Draw the head + ears
        drawCircle(inkColor, radius = u * 13 * breath, center = Offset(u * 26, u * 12))
        drawCircle(inkColor, radius = u * 13 * breath, center = Offset(u * 74, u * 12))
        drawCircle(Color(0xFF2A2A36), radius = u * 5, center = Offset(u * 26, u * 12))
        drawCircle(Color(0xFF2A2A36), radius = u * 5, center = Offset(u * 74, u * 12))

        drawCircle(bodyColor, radius = u * 22 * breath, center = Offset(headCx, headCy))

        // Eye patches (slightly tilted teardrops)
        drawCircle(inkColor, radius = u * 7, center = Offset(u * 40, headCy))
        drawCircle(inkColor, radius = u * 7, center = Offset(u * 60, headCy))
        drawCircle(bodyColor, radius = u * 2.2f, center = Offset(u * 42, u * 30))
        drawCircle(bodyColor, radius = u * 2.2f, center = Offset(u * 62, u * 30))
        drawCircle(inkColor, radius = u * 0.9f, center = Offset(u * 42.5f, u * 30))
        drawCircle(inkColor, radius = u * 0.9f, center = Offset(u * 62.5f, u * 30))

        // Nose
        drawCircle(inkColor, radius = u * 2.6f, center = Offset(u * 50, u * 38))
        // Tiny smile
        drawArc(
            color = inkColor,
            startAngle = 0f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(u * 44, u * 42), size = Size(u * 12, u * 6),
            style = Stroke(width = u * 1.4f)
        )

        // — Body (small belly visible behind the tablet) —
        val bodyPath = Path().apply {
            moveTo(u * 30, u * 52)
            cubicTo(u * 18, u * 60, u * 18, u * 84, u * 38, u * 90)
            lineTo(u * 62, u * 90)
            cubicTo(u * 82, u * 84, u * 82, u * 60, u * 70, u * 52)
            close()
        }
        drawPath(bodyPath, bodyColor)

        // Belly accent
        drawCircle(Color(0xFFE2E2E0), radius = u * 12, center = Offset(u * 50, u * 80))

        // — Paws holding the tablet edges —
        drawPaw(u, inkColor, bodyColor, leftSide = true)
        drawPaw(u, inkColor, bodyColor, leftSide = false)

        // Bamboo leaf accent above right ear
        val leaf = Path().apply {
            moveTo(u * 80, u * 2)
            cubicTo(u * 88, u * -4, u * 96, u * 0, u * 98, u * 4)
            cubicTo(u * 96, u * 8, u * 88, u * 12, u * 80, u * 8)
            cubicTo(u * 79, u * 6, u * 79, u * 4, u * 80, u * 2)
            close()
        }
        drawPath(leaf, accentColor)
    }
}

/**
 * Draws a "fluff" effect: short white spokes radiating from the head silhouette so the panda
 * reads as soft. Cheaper than per-pixel noise and looks intentional rather than artefactual.
 */
private fun DrawScope.drawFluffRing(
    u: Float,
    color: Color,
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    val cx = u * centerX
    val cy = u * centerY
    val n = 28
    for (i in 0 until n) {
        val angle = (i.toDouble() / n) * Math.PI * 2
        val rInner = u * (radius - 0.5f)
        val rOuter = u * (radius + 2.8f)
        val sx = cx + (Math.cos(angle) * rInner).toFloat()
        val sy = cy + (Math.sin(angle) * rInner).toFloat()
        val ex = cx + (Math.cos(angle) * rOuter).toFloat()
        val ey = cy + (Math.sin(angle) * rOuter).toFloat()
        drawLine(color, Offset(sx, sy), Offset(ex, ey), strokeWidth = u * 0.8f)
    }
}

private fun DrawScope.drawPaw(u: Float, ink: Color, body: Color, leftSide: Boolean) {
    // Two black paws gripping the tablet's left/right edges. Drawn as filled rounded blobs.
    val xCenter = if (leftSide) u * 22 else u * 78
    drawCircle(ink, radius = u * 8, center = Offset(xCenter, u * 76))
    // Tiny pad highlights
    drawCircle(body, radius = u * 2.2f, center = Offset(xCenter, u * 74))
}

/**
 * Draw the rounded tablet the panda is "holding", and overlay the per-context glyph on its
 * display. Caller fills the chat content as a separate Compose overlay layered on top — this
 * just paints the body of the tablet so the panda's paws sit visually correctly.
 */
private fun DrawScope.drawTablet(
    u: Float,
    accent: Color,
    body: Color,
    ink: Color,
    context: AssistantContext,
    weatherCondition: WeatherCondition?
) {
    val x = u * 14
    val y = u * 54
    val width = u * 72
    val height = u * 30
    drawRoundRect(
        color = ink,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(u * 3, u * 3)
    )
    drawRoundRect(
        color = body,
        topLeft = Offset(x + u * 2, y + u * 2),
        size = Size(width - u * 4, height - u * 4),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(u * 2, u * 2)
    )

    // Per-context glyph in the centre of the tablet. The actual chat text overlays this
    // glyph in the Compose layer — the glyph reads through whenever the chat is empty.
    val gx = x + width / 2
    val gy = y + height / 2
    when (context) {
        AssistantContext.Weather -> drawWeatherGlyph(u, weatherCondition, accent, ink, gx, gy)
        AssistantContext.News -> drawNewsGlyph(u, accent, ink, gx, gy)
        AssistantContext.Email -> drawEnvelopeGlyph(u, accent, ink, gx, gy)
        AssistantContext.Alarm -> drawAlarmGlyph(u, accent, ink, gx, gy)
        AssistantContext.Map -> drawMapPinGlyph(u, accent, ink, gx, gy)
        AssistantContext.Food -> drawBowlGlyph(u, accent, ink, gx, gy)
        AssistantContext.Notes -> drawNoteGlyph(u, accent, ink, gx, gy)
        AssistantContext.Default -> drawChatBubbleGlyph(u, accent, ink, gx, gy)
    }
}

// ----- per-context glyphs -----

private fun DrawScope.drawWeatherGlyph(
    u: Float,
    condition: WeatherCondition?,
    accent: Color,
    ink: Color,
    cx: Float,
    cy: Float
) {
    when (condition) {
        WeatherCondition.Clear, WeatherCondition.MainlyClear -> {
            // Sun: filled circle + 8 rays
            drawCircle(accent, radius = u * 3, center = Offset(cx, cy))
            for (a in 0 until 8) {
                val rad = a * Math.PI.toFloat() / 4
                val sx = cx + kotlin.math.cos(rad) * u * 5
                val sy = cy + kotlin.math.sin(rad) * u * 5
                val ex = cx + kotlin.math.cos(rad) * u * 7.5f
                val ey = cy + kotlin.math.sin(rad) * u * 7.5f
                drawLine(accent, Offset(sx, sy), Offset(ex, ey), strokeWidth = u * 0.8f)
            }
        }
        WeatherCondition.Rain, WeatherCondition.Drizzle, WeatherCondition.Showers -> {
            drawCloud(u, ink, cx, cy - u * 2)
            for (i in -1..1) {
                drawLine(
                    accent,
                    Offset(cx + i * u * 2.4f, cy + u * 4),
                    Offset(cx + i * u * 2.4f - u * 1.2f, cy + u * 8),
                    strokeWidth = u * 0.9f
                )
            }
        }
        WeatherCondition.Snow -> {
            drawCloud(u, ink, cx, cy - u * 2)
            for (i in -1..1) {
                drawCircle(accent, radius = u * 0.8f, center = Offset(cx + i * u * 2.4f, cy + u * 6))
            }
        }
        WeatherCondition.Thunderstorm -> {
            drawCloud(u, ink, cx, cy - u * 2)
            val bolt = Path().apply {
                moveTo(cx - u * 1.4f, cy + u * 3)
                lineTo(cx + u * 1f, cy + u * 3)
                lineTo(cx - u * 0.5f, cy + u * 6)
                lineTo(cx + u * 2f, cy + u * 6)
                lineTo(cx - u * 1f, cy + u * 10)
                lineTo(cx, cy + u * 7)
                lineTo(cx - u * 2f, cy + u * 7)
                close()
            }
            drawPath(bolt, accent)
        }
        WeatherCondition.Fog -> {
            for (i in 0..2) {
                drawLine(
                    ink,
                    Offset(cx - u * 6, cy + (i - 1) * u * 3),
                    Offset(cx + u * 6, cy + (i - 1) * u * 3),
                    strokeWidth = u * 0.8f
                )
            }
        }
        else -> {
            // PartlyCloudy / Overcast / Unknown
            drawCircle(accent, radius = u * 2.6f, center = Offset(cx - u * 2, cy - u * 2))
            drawCloud(u, ink, cx + u * 1, cy + u * 1)
        }
    }
}

private fun DrawScope.drawCloud(u: Float, color: Color, cx: Float, cy: Float) {
    drawCircle(color, radius = u * 3, center = Offset(cx - u * 3, cy + u))
    drawCircle(color, radius = u * 4, center = Offset(cx, cy - u))
    drawCircle(color, radius = u * 3, center = Offset(cx + u * 3, cy + u))
    drawRect(color, Offset(cx - u * 4, cy + u), Size(u * 8, u * 2))
}

private fun DrawScope.drawNewsGlyph(u: Float, accent: Color, ink: Color, cx: Float, cy: Float) {
    drawRect(ink, Offset(cx - u * 7, cy - u * 5), Size(u * 14, u * 10))
    drawRect(Color.White, Offset(cx - u * 6, cy - u * 4), Size(u * 12, u * 8))
    drawLine(ink, Offset(cx - u * 5, cy - u * 3), Offset(cx + u * 5, cy - u * 3), strokeWidth = u * 0.6f)
    drawLine(ink, Offset(cx - u * 5, cy - u * 1.6f), Offset(cx + u * 5, cy - u * 1.6f), strokeWidth = u * 0.6f)
    drawLine(ink, Offset(cx - u * 5, cy - u * 0.2f), Offset(cx + u * 5, cy - u * 0.2f), strokeWidth = u * 0.6f)
    drawRect(accent, Offset(cx - u * 5, cy + u * 1), Size(u * 4, u * 2))
}

private fun DrawScope.drawEnvelopeGlyph(u: Float, accent: Color, ink: Color, cx: Float, cy: Float) {
    drawRect(ink, Offset(cx - u * 7, cy - u * 4), Size(u * 14, u * 9))
    val flap = Path().apply {
        moveTo(cx - u * 7, cy - u * 4)
        lineTo(cx, cy + u * 1.2f)
        lineTo(cx + u * 7, cy - u * 4)
        close()
    }
    drawPath(flap, accent)
}

private fun DrawScope.drawAlarmGlyph(u: Float, accent: Color, ink: Color, cx: Float, cy: Float) {
    drawCircle(accent, radius = u * 6, center = Offset(cx, cy))
    drawCircle(Color.White, radius = u * 5, center = Offset(cx, cy))
    drawLine(ink, Offset(cx, cy), Offset(cx, cy - u * 3), strokeWidth = u * 0.8f)
    drawLine(ink, Offset(cx, cy), Offset(cx + u * 2.4f, cy), strokeWidth = u * 0.8f)
    // bell feet
    drawLine(ink, Offset(cx - u * 4, cy - u * 5), Offset(cx - u * 6, cy - u * 7), strokeWidth = u * 0.8f)
    drawLine(ink, Offset(cx + u * 4, cy - u * 5), Offset(cx + u * 6, cy - u * 7), strokeWidth = u * 0.8f)
}

private fun DrawScope.drawMapPinGlyph(u: Float, accent: Color, ink: Color, cx: Float, cy: Float) {
    val pin = Path().apply {
        moveTo(cx, cy + u * 7)
        cubicTo(cx - u * 5, cy + u * 1, cx - u * 5, cy - u * 4, cx, cy - u * 6)
        cubicTo(cx + u * 5, cy - u * 4, cx + u * 5, cy + u * 1, cx, cy + u * 7)
        close()
    }
    drawPath(pin, accent)
    drawCircle(Color.White, radius = u * 1.6f, center = Offset(cx, cy - u * 2))
}

private fun DrawScope.drawBowlGlyph(u: Float, accent: Color, ink: Color, cx: Float, cy: Float) {
    // Bowl with chopsticks
    drawArc(
        accent, 0f, 180f, true,
        Offset(cx - u * 7, cy - u * 1), Size(u * 14, u * 8)
    )
    drawLine(ink, Offset(cx + u * 3, cy - u * 6), Offset(cx + u * 6, cy - u * 1), strokeWidth = u * 0.8f)
    drawLine(ink, Offset(cx + u * 5, cy - u * 6), Offset(cx + u * 7, cy - u * 1), strokeWidth = u * 0.8f)
    // steam
    drawArc(
        ink.copy(alpha = 0.4f), 200f, 120f, false,
        Offset(cx - u * 2, cy - u * 7), Size(u * 4, u * 4), style = Stroke(u * 0.6f)
    )
}

private fun DrawScope.drawNoteGlyph(u: Float, accent: Color, ink: Color, cx: Float, cy: Float) {
    drawRect(Color.White, Offset(cx - u * 5, cy - u * 6), Size(u * 10, u * 12))
    drawLine(ink, Offset(cx - u * 5, cy - u * 6), Offset(cx - u * 5, cy + u * 6), strokeWidth = u * 1.6f)
    for (i in 0..3) {
        val y = cy - u * 4 + i * u * 2
        drawLine(ink, Offset(cx - u * 3, y), Offset(cx + u * 4, y), strokeWidth = u * 0.5f)
    }
    drawCircle(accent, radius = u * 0.8f, center = Offset(cx - u * 5, cy + u * 4))
}

private fun DrawScope.drawChatBubbleGlyph(u: Float, accent: Color, ink: Color, cx: Float, cy: Float) {
    val p = Path().apply {
        moveTo(cx - u * 7, cy - u * 5)
        cubicTo(cx - u * 9, cy + u * 4, cx + u * 7, cy + u * 6, cx + u * 7, cy + u * 0)
        cubicTo(cx + u * 8, cy - u * 6, cx - u * 5, cy - u * 7, cx - u * 7, cy - u * 5)
        close()
    }
    drawPath(p, accent)
    // Three dots
    for (i in -1..1) {
        drawCircle(Color.White, radius = u * 0.8f, center = Offset(cx + i * u * 2.4f, cy))
    }
}

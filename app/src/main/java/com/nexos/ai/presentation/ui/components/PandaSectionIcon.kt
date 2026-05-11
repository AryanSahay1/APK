package com.nexos.ai.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Small panda + accessory glyph used as the leading icon for every Settings section.
 *
 * Each variant shares the panda head geometry from [PandaMascot] and overlays an accessory
 * (umbrella, sun/moon, broom, etc.). Sized 28–48 dp; container is a tinted rounded square so
 * the icon reads as a "section badge" in a list.
 *
 * All variants are deterministic Canvas paths — no XML drawable inflation per icon, no
 * GPU surface per icon — so a Settings list with 10+ section pandas costs as much as
 * 10 vector glyphs would.
 */
enum class PandaSectionKind {
    AboutMe,
    ApiKeys,
    Theme,
    Floating,
    Storage,
    News,
    Alarms,
    AiProvider,
    Privacy,
    Notifications
}

@Composable
fun PandaSectionIcon(
    kind: PandaSectionKind,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tintBg: Color = Color(0x3300E676),
    inkColor: Color = Color(0xFF0F0F14),
    bodyColor: Color = Color(0xFFFAFAFA),
    accentColor: Color = Color(0xFF00E676),
    isDarkMode: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 4))
            .background(tintBg),
        contentAlignment = Alignment.Center
    ) {
        if (kind == PandaSectionKind.Theme) {
            // Animated sun/moon swap that mirrors the actual theme state.
            AnimatedContent(
                targetState = isDarkMode,
                transitionSpec = {
                    (scaleIn(tween(220)) + fadeIn()) togetherWith
                        (scaleOut(tween(160)) + fadeOut())
                },
                label = "panda-theme-glyph"
            ) { dark ->
                PandaSectionCanvas(
                    kind = if (dark) PandaSectionKind.Theme else PandaSectionKindLight,
                    size = size,
                    inkColor = inkColor,
                    bodyColor = bodyColor,
                    accentColor = accentColor
                )
            }
        } else {
            PandaSectionCanvas(
                kind = kind,
                size = size,
                inkColor = inkColor,
                bodyColor = bodyColor,
                accentColor = accentColor
            )
        }
    }
}

private val PandaSectionKindLight = PandaSectionKind.Theme // sentinel, distinguished via isDarkMode

@Composable
private fun PandaSectionCanvas(
    kind: PandaSectionKind,
    size: Dp,
    inkColor: Color,
    bodyColor: Color,
    accentColor: Color
) {
    // Each kind that has an animated accessory subscribes to its own infinite transition here.
    val keyboardTap = if (kind == PandaSectionKind.ApiKeys) keyboardTapOffset() else 0f
    val broomSwing = if (kind == PandaSectionKind.Storage) broomSwingAngle() else 0f
    val clockHand = if (kind == PandaSectionKind.Alarms) clockHandAngle() else 0f
    val bubbleY = if (kind == PandaSectionKind.Floating) bubbleFloatOffset() else 0f

    Canvas(modifier = Modifier.size(size * 0.78f)) {
        val w = this.size.width
        val u = w / 100f
        // Base panda head — drawn at slightly compressed scale (head bigger relative to body)
        drawPandaHead(u, bodyColor, inkColor)
        // Accessory by kind
        when (kind) {
            PandaSectionKind.AboutMe -> drawQuestionCard(u, inkColor, accentColor)
            PandaSectionKind.ApiKeys -> drawKeyboard(u, inkColor, accentColor, keyboardTap)
            PandaSectionKind.Theme -> drawSunMoon(u, accentColor, dark = true)
            PandaSectionKind.Floating -> drawBubble(u, accentColor, bubbleY)
            PandaSectionKind.Storage -> drawBroom(u, inkColor, accentColor, broomSwing)
            PandaSectionKind.News -> drawNewspaper(u, inkColor)
            PandaSectionKind.Alarms -> drawClock(u, inkColor, accentColor, clockHand)
            PandaSectionKind.AiProvider -> drawChatBubble(u, accentColor)
            PandaSectionKind.Privacy -> drawShield(u, accentColor)
            PandaSectionKind.Notifications -> drawBell(u, accentColor)
        }
    }
}

@Composable
private fun keyboardTapOffset(): Float {
    val t = rememberInfiniteTransition(label = "key-tap")
    val v by t.animateFloat(
        0f, 1.6f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "key-tap-v"
    )
    return v
}

@Composable
private fun broomSwingAngle(): Float {
    val t = rememberInfiniteTransition(label = "broom-swing")
    val v by t.animateFloat(
        -12f, 12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "broom-swing-v"
    )
    return v
}

@Composable
private fun clockHandAngle(): Float {
    val t = rememberInfiniteTransition(label = "clock-tick")
    val v by t.animateFloat(
        0f, 360f,
        animationSpec = infiniteRepeatable(tween(6000)),
        label = "clock-tick-v"
    )
    return v
}

@Composable
private fun bubbleFloatOffset(): Float {
    val t = rememberInfiniteTransition(label = "bubble-float")
    val v by t.animateFloat(
        -1.4f, 1.4f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "bubble-float-v"
    )
    return v
}

// ---------- Drawing primitives ----------

private fun DrawScope.drawPandaHead(u: Float, body: Color, ink: Color) {
    // Ears
    drawCircle(ink, radius = u * 13, center = Offset(u * 24, u * 32))
    drawCircle(ink, radius = u * 13, center = Offset(u * 76, u * 32))
    drawCircle(Color(0xFF2A2A36), radius = u * 5, center = Offset(u * 24, u * 32))
    drawCircle(Color(0xFF2A2A36), radius = u * 5, center = Offset(u * 76, u * 32))
    // Head
    drawCircle(body, radius = u * 30, center = Offset(u * 50, u * 56))
    // Eye patches
    drawCircle(ink, radius = u * 9, center = Offset(u * 38, u * 56))
    drawCircle(ink, radius = u * 9, center = Offset(u * 62, u * 56))
    // Eyes
    drawCircle(body, radius = u * 3, center = Offset(u * 40, u * 56))
    drawCircle(body, radius = u * 3, center = Offset(u * 60, u * 56))
    drawCircle(ink, radius = u * 1.2f, center = Offset(u * 40.6f, u * 56))
    drawCircle(ink, radius = u * 1.2f, center = Offset(u * 60.6f, u * 56))
    // Nose
    drawCircle(ink, radius = u * 3.4f, center = Offset(u * 50, u * 70))
}

private fun DrawScope.drawQuestionCard(u: Float, ink: Color, accent: Color) {
    drawRoundRect(
        color = accent,
        topLeft = Offset(u * 60, u * 6),
        size = androidx.compose.ui.geometry.Size(u * 34, u * 26),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(u * 5, u * 5)
    )
    drawCircle(ink, radius = u * 1.4f, center = Offset(u * 77, u * 26))
    val p = Path().apply {
        moveTo(u * 71, u * 14)
        cubicTo(u * 71, u * 9, u * 82, u * 9, u * 82, u * 14)
        cubicTo(u * 82, u * 19, u * 77, u * 18, u * 77, u * 22)
    }
    drawPath(p, ink, style = Stroke(width = u * 2f))
}

private fun DrawScope.drawKeyboard(u: Float, ink: Color, accent: Color, tap: Float) {
    drawRoundRect(
        color = ink,
        topLeft = Offset(u * 18, u * 86),
        size = androidx.compose.ui.geometry.Size(u * 64, u * 12),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(u * 3, u * 3)
    )
    // 9 keys, one bouncing (tap)
    val baseY = u * (92f - tap)
    for (i in 0..8) {
        val cx = u * (22f + i * 7f)
        val keyY = if (i == 4) baseY else u * 92f
        drawCircle(accent, radius = u * 1.8f, center = Offset(cx, keyY))
    }
}

private fun DrawScope.drawSunMoon(u: Float, accent: Color, dark: Boolean) {
    // Right of head — sun or moon glyph (dark mode → moon, light mode → sun)
    val cx = u * 84
    val cy = u * 16
    if (dark) {
        // Crescent moon as two overlapping circles
        drawCircle(accent, radius = u * 8, center = Offset(cx, cy))
        drawCircle(Color(0x00000000), radius = u * 7, center = Offset(cx + u * 3, cy - u * 2))
    } else {
        drawCircle(accent, radius = u * 6, center = Offset(cx, cy))
        for (a in 0 until 8) {
            val rad = a * Math.PI.toFloat() / 4f
            val sx = cx + kotlin.math.cos(rad) * u * 10f
            val sy = cy + kotlin.math.sin(rad) * u * 10f
            val ex = cx + kotlin.math.cos(rad) * u * 14f
            val ey = cy + kotlin.math.sin(rad) * u * 14f
            drawLine(accent, Offset(sx, sy), Offset(ex, ey), strokeWidth = u * 1.6f)
        }
    }
}

private fun DrawScope.drawBubble(u: Float, accent: Color, y: Float) {
    val centerY = u * (12f + y)
    drawCircle(accent, radius = u * 7, center = Offset(u * 14, centerY), style = Stroke(width = u * 1.6f))
    drawCircle(accent.copy(alpha = 0.5f), radius = u * 2.2f, center = Offset(u * 11, centerY - u * 2))
}

private fun DrawScope.drawBroom(u: Float, ink: Color, accent: Color, angle: Float) {
    rotate(degrees = angle, pivot = Offset(u * 84, u * 84)) {
        drawLine(ink, Offset(u * 84, u * 84), Offset(u * 90, u * 60), strokeWidth = u * 2.2f)
        val bristles = Path().apply {
            moveTo(u * 86, u * 90)
            lineTo(u * 78, u * 100)
            lineTo(u * 94, u * 100)
            close()
        }
        drawPath(bristles, accent)
    }
}

private fun DrawScope.drawNewspaper(u: Float, ink: Color) {
    drawRoundRect(
        color = ink,
        topLeft = Offset(u * 62, u * 70),
        size = androidx.compose.ui.geometry.Size(u * 32, u * 24),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(u * 2, u * 2)
    )
    for (i in 0..3) {
        val y = u * (74f + i * 5f)
        drawLine(Color(0xFFFAFAFA), Offset(u * 65, y), Offset(u * 91, y), strokeWidth = u * 1.4f)
    }
}

private fun DrawScope.drawClock(u: Float, ink: Color, accent: Color, handAngle: Float) {
    val cx = u * 84
    val cy = u * 84
    drawCircle(accent, radius = u * 10, center = Offset(cx, cy))
    drawCircle(Color(0xFFFAFAFA), radius = u * 8, center = Offset(cx, cy))
    rotate(degrees = handAngle, pivot = Offset(cx, cy)) {
        drawLine(ink, Offset(cx, cy), Offset(cx, cy - u * 6), strokeWidth = u * 1.6f)
    }
    drawCircle(ink, radius = u * 1.6f, center = Offset(cx, cy))
}

private fun DrawScope.drawChatBubble(u: Float, accent: Color) {
    val p = Path().apply {
        moveTo(u * 64, u * 14)
        cubicTo(u * 64, u * 6, u * 86, u * 6, u * 86, u * 16)
        cubicTo(u * 86, u * 24, u * 76, u * 26, u * 70, u * 24)
        lineTo(u * 66, u * 28)
        lineTo(u * 68, u * 22)
        cubicTo(u * 64, u * 20, u * 64, u * 18, u * 64, u * 14)
        close()
    }
    drawPath(p, accent)
}

private fun DrawScope.drawShield(u: Float, accent: Color) {
    val p = Path().apply {
        moveTo(u * 84, u * 6)
        lineTo(u * 96, u * 12)
        lineTo(u * 96, u * 22)
        cubicTo(u * 96, u * 30, u * 90, u * 34, u * 84, u * 36)
        cubicTo(u * 78, u * 34, u * 72, u * 30, u * 72, u * 22)
        lineTo(u * 72, u * 12)
        close()
    }
    drawPath(p, accent)
}

private fun DrawScope.drawBell(u: Float, accent: Color) {
    val p = Path().apply {
        moveTo(u * 84, u * 8)
        cubicTo(u * 76, u * 10, u * 74, u * 18, u * 74, u * 24)
        lineTo(u * 94, u * 24)
        cubicTo(u * 94, u * 18, u * 92, u * 10, u * 84, u * 8)
        close()
    }
    drawPath(p, accent)
    drawCircle(accent, radius = u * 2, center = Offset(u * 84, u * 30))
}

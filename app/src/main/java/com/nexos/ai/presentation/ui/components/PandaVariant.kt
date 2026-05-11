package com.nexos.ai.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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

/**
 * 30 visually distinct panda variants. Each shares the same base head geometry from
 * [PandaMascot] and overlays one or more accessory paths drawn on the same Canvas. The
 * variants are deliberately tiny in code: each is a switch case + 5–15 lines of paths.
 *
 * Used by [PandaConfetti] to scatter friendly pandas across hero screens. NOT exhaustive of
 * actual emotions — they're meant to feel like a sticker pack, not a feelings wheel.
 */
enum class PandaVariant {
    Default,        // 0  plain panda
    Sleepy,         // 1  closed eyes + Zzz
    Coffee,         // 2  holding a coffee cup
    Headphones,     // 3  wearing headphones
    Reading,        // 4  reading a book in front
    Sunglasses,     // 5  big black sunglasses
    Heart,          // 6  heart eyes
    Bamboo,         // 7  eating bamboo stalk
    Crown,          // 8  little crown on top
    Balloon,        // 9  holding a balloon string
    Snorkel,        // 10 snorkel mask + tube
    Scarf,          // 11 winter scarf
    Chef,           // 12 chef's hat
    Sports,         // 13 sweatband
    Painter,        // 14 painter palette
    Wizard,         // 15 pointy wizard hat
    Detective,      // 16 magnifying glass
    Backpack,       // 17 with backpack strap
    Camera,         // 18 holding a camera
    Music,          // 19 musical notes around head
    Star,           // 20 sparkle star above
    Umbrella,       // 21 holding umbrella
    Cool,           // 22 aviator sunglasses
    Runner,         // 23 sweatband + speed lines
    Pirate,         // 24 pirate hat with skull
    Birthday,       // 25 party hat
    Glasses,        // 26 reading round glasses
    Phone,          // 27 holding a phone
    Sparkle,        // 28 sparkles around panda
    Bowtie          // 29 fancy bowtie
}

/**
 * Render a panda of the given [variant]. Internally falls through to [PandaMascot] with the
 * accessory layer painted on top.
 */
@Composable
fun PandaVariantIcon(
    variant: PandaVariant,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    bodyColor: Color = Color(0xFFFAFAFA),
    inkColor: Color = Color(0xFF0F0F14),
    accentColor: Color = Color(0xFF00E676)
) {
    Canvas(modifier = modifier.size(size)) {
        drawBaseHead(bodyColor, inkColor)
        drawVariantAccessory(variant, inkColor, accentColor, bodyColor)
    }
}

private fun DrawScope.drawBaseHead(body: Color, ink: Color) {
    val w = size.width
    val u = w / 100f
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
    // Smile (subtle)
    drawArc(
        color = ink,
        startAngle = 0f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(u * 44, u * 76), size = Size(u * 12, u * 6),
        style = Stroke(width = u * 1.4f)
    )
}

@Suppress("LongMethod") // accessory cases are short individually; one switch by design
private fun DrawScope.drawVariantAccessory(
    variant: PandaVariant,
    ink: Color,
    accent: Color,
    body: Color
) {
    val w = size.width
    val u = w / 100f
    when (variant) {
        PandaVariant.Default -> Unit

        PandaVariant.Sleepy -> {
            // Re-paint eyes as closed arcs + Zzz floating up-right
            drawCircle(ink, radius = u * 9, center = Offset(u * 38, u * 56))
            drawCircle(ink, radius = u * 9, center = Offset(u * 62, u * 56))
            drawArc(body, 200f, 140f, false, Offset(u * 34, u * 53), Size(u * 8, u * 5), style = Stroke(u * 1.4f))
            drawArc(body, 200f, 140f, false, Offset(u * 58, u * 53), Size(u * 8, u * 5), style = Stroke(u * 1.4f))
            drawZ(u, accent, baseX = 82f, baseY = 12f)
        }

        PandaVariant.Coffee -> {
            // Cup at bottom-right of head
            drawRect(ink, Offset(u * 76, u * 70), Size(u * 14, u * 14))
            drawRect(body, Offset(u * 78, u * 72), Size(u * 10, u * 10))
            drawCircle(ink, radius = u * 2.4f, center = Offset(u * 92, u * 75), style = Stroke(u * 1.4f))
            // Steam
            drawArc(accent, 200f, 120f, false, Offset(u * 80, u * 60), Size(u * 6, u * 6), style = Stroke(u * 1.2f))
        }

        PandaVariant.Headphones -> {
            // Band over head
            drawArc(accent, 200f, 140f, false, Offset(u * 14, u * 12), Size(u * 72, u * 32), style = Stroke(u * 2.4f))
            // Cups on ears
            drawCircle(ink, radius = u * 6, center = Offset(u * 14, u * 36))
            drawCircle(ink, radius = u * 6, center = Offset(u * 86, u * 36))
            drawCircle(accent, radius = u * 3, center = Offset(u * 14, u * 36))
            drawCircle(accent, radius = u * 3, center = Offset(u * 86, u * 36))
        }

        PandaVariant.Reading -> {
            // Open book covering lower face
            drawRect(ink, Offset(u * 28, u * 72), Size(u * 44, u * 18))
            drawRect(body, Offset(u * 30, u * 74), Size(u * 40, u * 14))
            drawLine(ink, Offset(u * 50, u * 74), Offset(u * 50, u * 88), strokeWidth = u * 0.8f)
            // Lines on each page
            for (i in 0..2) {
                val y = u * (76f + i * 4f)
                drawLine(ink, Offset(u * 32, y), Offset(u * 46, y), strokeWidth = u * 0.6f)
                drawLine(ink, Offset(u * 54, y), Offset(u * 68, y), strokeWidth = u * 0.6f)
            }
        }

        PandaVariant.Sunglasses -> {
            drawCircle(ink, radius = u * 11, center = Offset(u * 38, u * 56))
            drawCircle(ink, radius = u * 11, center = Offset(u * 62, u * 56))
            drawLine(ink, Offset(u * 47, u * 56), Offset(u * 53, u * 56), strokeWidth = u * 2.5f)
            // Glint
            drawCircle(body, radius = u * 1.8f, center = Offset(u * 34, u * 52))
            drawCircle(body, radius = u * 1.8f, center = Offset(u * 58, u * 52))
        }

        PandaVariant.Heart -> {
            // Heart eyes
            drawHeart(u, accent.copy(alpha = 1f), cx = 40f, cy = 56f, scale = 0.7f)
            drawHeart(u, accent.copy(alpha = 1f), cx = 60f, cy = 56f, scale = 0.7f)
        }

        PandaVariant.Bamboo -> {
            // Stalk emerging from side of mouth
            drawRect(accent, Offset(u * 64, u * 70), Size(u * 30, u * 6))
            drawLine(Color(0xFF008C4A), Offset(u * 64, u * 70), Offset(u * 94, u * 70), strokeWidth = u * 0.8f)
            drawLine(Color(0xFF008C4A), Offset(u * 64, u * 76), Offset(u * 94, u * 76), strokeWidth = u * 0.8f)
        }

        PandaVariant.Crown -> {
            val p = Path().apply {
                moveTo(u * 32, u * 18)
                lineTo(u * 38, u * 6); lineTo(u * 44, u * 14)
                lineTo(u * 50, u * 4); lineTo(u * 56, u * 14)
                lineTo(u * 62, u * 6); lineTo(u * 68, u * 18)
                close()
            }
            drawPath(p, accent)
        }

        PandaVariant.Balloon -> {
            drawCircle(accent, radius = u * 10, center = Offset(u * 84, u * 14))
            drawLine(ink, Offset(u * 84, u * 24), Offset(u * 78, u * 50), strokeWidth = u * 0.6f)
            drawLine(ink, Offset(u * 83, u * 22), Offset(u * 86, u * 26), strokeWidth = u * 0.8f)
        }

        PandaVariant.Snorkel -> {
            // Goggle ring
            drawCircle(accent, radius = u * 14, center = Offset(u * 50, u * 54), style = Stroke(u * 2f))
            // Tube
            drawLine(accent, Offset(u * 78, u * 44), Offset(u * 84, u * 14), strokeWidth = u * 2.5f)
            drawCircle(accent, radius = u * 2.5f, center = Offset(u * 84, u * 14))
        }

        PandaVariant.Scarf -> {
            drawRect(accent, Offset(u * 22, u * 76), Size(u * 56, u * 8))
            drawRect(accent, Offset(u * 70, u * 80), Size(u * 12, u * 18))
            for (i in 0..4) {
                val y = u * (80f + i * 0.8f)
                drawLine(Color(0xFF008C4A), Offset(u * 22, y), Offset(u * 80, y), strokeWidth = u * 0.4f)
            }
        }

        PandaVariant.Chef -> {
            // Toque
            drawRect(body, Offset(u * 28, u * 10), Size(u * 44, u * 14))
            drawCircle(body, radius = u * 8, center = Offset(u * 34, u * 8))
            drawCircle(body, radius = u * 9, center = Offset(u * 50, u * 4))
            drawCircle(body, radius = u * 8, center = Offset(u * 66, u * 8))
            drawLine(ink, Offset(u * 28, u * 22), Offset(u * 72, u * 22), strokeWidth = u * 0.6f)
        }

        PandaVariant.Sports -> {
            // Sweatband on forehead
            drawRect(accent, Offset(u * 22, u * 36), Size(u * 56, u * 6))
            // Stripe
            drawRect(body, Offset(u * 48, u * 36), Size(u * 4, u * 6))
        }

        PandaVariant.Painter -> {
            // Palette in bottom-right
            drawCircle(Color(0xFFD0A878), radius = u * 10, center = Offset(u * 82, u * 80))
            drawCircle(Color(0xFFE23744), radius = u * 1.6f, center = Offset(u * 76, u * 76))
            drawCircle(Color(0xFFFFC107), radius = u * 1.6f, center = Offset(u * 82, u * 74))
            drawCircle(Color(0xFF4DA6FF), radius = u * 1.6f, center = Offset(u * 88, u * 78))
            drawCircle(Color(0xFF34A853), radius = u * 1.6f, center = Offset(u * 84, u * 84))
        }

        PandaVariant.Wizard -> {
            val p = Path().apply {
                moveTo(u * 24, u * 24); lineTo(u * 50, u * 4); lineTo(u * 76, u * 24); close()
            }
            drawPath(p, Color(0xFF6A5ACD))
            // Stars on hat
            drawCircle(accent, radius = u * 1.4f, center = Offset(u * 44, u * 16))
            drawCircle(accent, radius = u * 1.2f, center = Offset(u * 56, u * 12))
        }

        PandaVariant.Detective -> {
            // Magnifying glass top-right
            drawCircle(ink, radius = u * 10, center = Offset(u * 80, u * 24), style = Stroke(u * 2f))
            drawLine(ink, Offset(u * 87, u * 31), Offset(u * 96, u * 40), strokeWidth = u * 2.4f)
            // Glint
            drawCircle(body, radius = u * 2f, center = Offset(u * 76, u * 20))
        }

        PandaVariant.Backpack -> {
            // Strap visible across right shoulder
            drawRect(accent, Offset(u * 70, u * 64), Size(u * 6, u * 30))
            drawCircle(accent, radius = u * 5, center = Offset(u * 90, u * 80))
        }

        PandaVariant.Camera -> {
            drawRect(ink, Offset(u * 30, u * 76), Size(u * 40, u * 18))
            drawRect(body, Offset(u * 32, u * 74), Size(u * 12, u * 4))
            drawCircle(accent, radius = u * 5, center = Offset(u * 50, u * 85))
            drawCircle(body, radius = u * 2, center = Offset(u * 50, u * 85))
        }

        PandaVariant.Music -> {
            // Two notes near ears
            drawNote(u, accent, cx = 86f, cy = 14f)
            drawNote(u, accent.copy(alpha = 0.7f), cx = 14f, cy = 22f)
        }

        PandaVariant.Star -> {
            drawStar(u, accent, cx = 50f, cy = 12f, r = 8f)
        }

        PandaVariant.Umbrella -> {
            // Canopy
            drawArc(accent, 180f, 180f, true, Offset(u * 14, u * 8), Size(u * 72, u * 22))
            // Stick
            drawLine(ink, Offset(u * 50, u * 30), Offset(u * 50, u * 56), strokeWidth = u * 1.4f)
            // Hook
            drawArc(ink, 0f, 180f, false, Offset(u * 50, u * 54), Size(u * 8, u * 8), style = Stroke(u * 1.4f))
        }

        PandaVariant.Cool -> {
            // Aviator-style sunglasses (rounder)
            drawCircle(ink, radius = u * 10, center = Offset(u * 38, u * 56))
            drawCircle(ink, radius = u * 10, center = Offset(u * 62, u * 56))
            // Frame highlight
            drawCircle(accent, radius = u * 10, center = Offset(u * 38, u * 56), style = Stroke(u * 1.4f))
            drawCircle(accent, radius = u * 10, center = Offset(u * 62, u * 56), style = Stroke(u * 1.4f))
            drawLine(accent, Offset(u * 48, u * 56), Offset(u * 52, u * 56), strokeWidth = u * 1.4f)
        }

        PandaVariant.Runner -> {
            // Speed lines on the left
            for (i in 0..2) {
                val y = u * (40f + i * 18f)
                drawLine(accent, Offset(u * 4, y), Offset(u * 18, y), strokeWidth = u * 1.6f)
            }
        }

        PandaVariant.Pirate -> {
            // Eyepatch over left eye
            drawLine(ink, Offset(u * 20, u * 48), Offset(u * 60, u * 56), strokeWidth = u * 1.2f)
            drawRect(ink, Offset(u * 28, u * 50), Size(u * 22, u * 14))
            // Pirate hat
            val hat = Path().apply {
                moveTo(u * 22, u * 22); cubicTo(u * 30, u * 6, u * 70, u * 6, u * 78, u * 22); close()
            }
            drawPath(hat, ink)
            drawCircle(body, radius = u * 1.8f, center = Offset(u * 50, u * 14))
        }

        PandaVariant.Birthday -> {
            // Cone hat
            val p = Path().apply {
                moveTo(u * 30, u * 22); lineTo(u * 50, u * 2); lineTo(u * 70, u * 22); close()
            }
            drawPath(p, accent)
            // Pompom
            drawCircle(body, radius = u * 3, center = Offset(u * 50, u * 2))
        }

        PandaVariant.Glasses -> {
            // Round reading glasses
            drawCircle(ink, radius = u * 8, center = Offset(u * 38, u * 56), style = Stroke(u * 2f))
            drawCircle(ink, radius = u * 8, center = Offset(u * 62, u * 56), style = Stroke(u * 2f))
            drawLine(ink, Offset(u * 46, u * 56), Offset(u * 54, u * 56), strokeWidth = u * 1.4f)
        }

        PandaVariant.Phone -> {
            // Smartphone in front
            drawRect(ink, Offset(u * 38, u * 70), Size(u * 24, u * 24))
            drawRect(body, Offset(u * 40, u * 72), Size(u * 20, u * 20))
            drawCircle(accent, radius = u * 2f, center = Offset(u * 50, u * 82))
        }

        PandaVariant.Sparkle -> {
            // Four small sparkles around the head
            drawSparkle(u, accent, 12f, 12f)
            drawSparkle(u, accent, 88f, 12f)
            drawSparkle(u, accent, 12f, 88f)
            drawSparkle(u, accent, 88f, 88f)
        }

        PandaVariant.Bowtie -> {
            // Bowtie below mouth
            val left = Path().apply {
                moveTo(u * 36, u * 88); lineTo(u * 30, u * 80); lineTo(u * 30, u * 96); close()
            }
            val right = Path().apply {
                moveTo(u * 64, u * 88); lineTo(u * 70, u * 80); lineTo(u * 70, u * 96); close()
            }
            drawPath(left, accent)
            drawPath(right, accent)
            drawCircle(accent, radius = u * 3, center = Offset(u * 50, u * 88))
        }
    }
}

// ---------- accessory primitives ----------

private fun DrawScope.drawZ(u: Float, c: Color, baseX: Float, baseY: Float) {
    val p = Path().apply {
        moveTo(u * baseX, u * baseY)
        lineTo(u * (baseX + 8), u * baseY)
        lineTo(u * baseX, u * (baseY + 8))
        lineTo(u * (baseX + 8), u * (baseY + 8))
    }
    drawPath(p, c, style = Stroke(u * 1.6f))
}

private fun DrawScope.drawHeart(u: Float, c: Color, cx: Float, cy: Float, scale: Float = 1f) {
    val s = scale
    val p = Path().apply {
        moveTo(u * cx, u * (cy + 3 * s))
        cubicTo(u * (cx - 4 * s), u * (cy - 2 * s), u * (cx - 4 * s), u * (cy - 6 * s), u * cx, u * (cy - 2 * s))
        cubicTo(u * (cx + 4 * s), u * (cy - 6 * s), u * (cx + 4 * s), u * (cy - 2 * s), u * cx, u * (cy + 3 * s))
        close()
    }
    drawPath(p, c)
}

private fun DrawScope.drawNote(u: Float, c: Color, cx: Float, cy: Float) {
    drawCircle(c, radius = u * 2.6f, center = Offset(u * cx, u * (cy + 6f)))
    drawLine(c, Offset(u * (cx + 2.6f), u * (cy + 6f)), Offset(u * (cx + 2.6f), u * cy), strokeWidth = u * 1f)
    drawLine(c, Offset(u * (cx + 2.6f), u * cy), Offset(u * (cx + 6f), u * (cy - 1.5f)), strokeWidth = u * 1f)
}

private fun DrawScope.drawStar(u: Float, c: Color, cx: Float, cy: Float, r: Float) {
    val p = Path()
    for (i in 0 until 5) {
        val angle = Math.PI * 2 * i / 5 - Math.PI / 2
        val x = cx + r * Math.cos(angle).toFloat()
        val y = cy + r * Math.sin(angle).toFloat()
        if (i == 0) p.moveTo(u * x, u * y) else p.lineTo(u * x, u * y)
        val innerAngle = angle + Math.PI / 5
        val ix = cx + r * 0.4f * Math.cos(innerAngle).toFloat()
        val iy = cy + r * 0.4f * Math.sin(innerAngle).toFloat()
        p.lineTo(u * ix, u * iy)
    }
    p.close()
    drawPath(p, c)
}

private fun DrawScope.drawSparkle(u: Float, c: Color, cx: Float, cy: Float) {
    drawLine(c, Offset(u * (cx - 3), u * cy), Offset(u * (cx + 3), u * cy), strokeWidth = u * 0.8f)
    drawLine(c, Offset(u * cx, u * (cy - 3)), Offset(u * cx, u * (cy + 3)), strokeWidth = u * 0.8f)
    drawCircle(c, radius = u * 0.8f, center = Offset(u * cx, u * cy))
}

package com.nexos.ai.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Catalogue of 30 panda-themed background designs the user can apply to a note. Each design
 * is a pure-function Compose drawing — no PNG assets bundled in the APK. The catalogue is
 * deterministic: the same `id` always produces the same pixels (every Random is seeded by
 * `id`), so applied notes look identical across sessions and devices.
 *
 * The full catalogue can be retrieved via [all], a single design by [byId]. A swatch
 * preview suitable for a 96 × 128 dp grid cell is rendered by [PandaBackgroundThumb].
 *
 * Design philosophy:
 *   * 30 visually distinct surfaces, NOT 30 minor variations. Each pairs a base palette
 *     (warm cream, mint, lavender, dusty rose, slate, etc.) with a distinct motif
 *     (sleeping panda, paw prints, leaves, bamboo, hearts, dots, lines, geometric shapes,
 *     gradients, etc.).
 *   * Drawn at low opacity so the note's text always reads cleanly on top.
 *   * Pure pattern → no copyrighted imagery, no asset licensing concerns.
 */
data class PandaBackground(
    val id: Int,
    val displayName: String,
    val baseColor: Color,
    val accentColor: Color,
    val motif: Motif
) {
    enum class Motif {
        SleepingPanda, PawPrints, BambooStalks, LeafScatter, HeartScatter,
        DottedGrid, DiagonalLines, CirclesScatter, TrianglesScatter, StarsScatter,
        WaveLines, ConcentricRings, CornerPanda, FullPandaFaceTile, MicroPandas,
        SoftGradient, GradientHorizon, BambooGrove, PawTrail, ZenCircles,
        Confetti, FallingLeaves, MoonAndStars, SunRays, RainDrops,
        Snowflakes, Cherries, Clouds, Boba, PandaSilhouettesRow
    }
}

object PandaBackgrounds {

    val all: List<PandaBackground> by lazy {
        listOf(
            PandaBackground(1, "Bamboo Cream", c(0xFFFFF8E1), c(0xFF558B2F), PandaBackground.Motif.BambooStalks),
            PandaBackground(2, "Sleeping Panda", c(0xFFE0F2F1), c(0xFF263238), PandaBackground.Motif.SleepingPanda),
            PandaBackground(3, "Paw Trail Mint", c(0xFFE8F5E9), c(0xFF2E7D32), PandaBackground.Motif.PawTrail),
            PandaBackground(4, "Lavender Leaves", c(0xFFEDE7F6), c(0xFF7B5BA1), PandaBackground.Motif.LeafScatter),
            PandaBackground(5, "Heart Notes", c(0xFFFCE4EC), c(0xFFC2185B), PandaBackground.Motif.HeartScatter),
            PandaBackground(6, "Dotted Grid", c(0xFFFAFAFA), c(0xFF607D8B), PandaBackground.Motif.DottedGrid),
            PandaBackground(7, "Diagonals", c(0xFFFFF3E0), c(0xFFE65100), PandaBackground.Motif.DiagonalLines),
            PandaBackground(8, "Bubbles", c(0xFFE3F2FD), c(0xFF1565C0), PandaBackground.Motif.CirclesScatter),
            PandaBackground(9, "Triangles", c(0xFFF3E5F5), c(0xFF6A1B9A), PandaBackground.Motif.TrianglesScatter),
            PandaBackground(10, "Starry", c(0xFF0F1B2D), c(0xFFFFE082), PandaBackground.Motif.StarsScatter),
            PandaBackground(11, "Ocean Wave", c(0xFFE0F7FA), c(0xFF006064), PandaBackground.Motif.WaveLines),
            PandaBackground(12, "Zen Rings", c(0xFFFFF8F0), c(0xFF5D4037), PandaBackground.Motif.ConcentricRings),
            PandaBackground(13, "Corner Panda", c(0xFFFFFAFA), c(0xFF263238), PandaBackground.Motif.CornerPanda),
            PandaBackground(14, "Panda Tile", c(0xFFFFFDE7), c(0xFF1A237E), PandaBackground.Motif.FullPandaFaceTile),
            PandaBackground(15, "Micro Pandas", c(0xFFFFF3E0), c(0xFF263238), PandaBackground.Motif.MicroPandas),
            PandaBackground(16, "Soft Peach", c(0xFFFFE0B2), c(0xFFFFCC80), PandaBackground.Motif.SoftGradient),
            PandaBackground(17, "Sunset Horizon", c(0xFFFFCCBC), c(0xFFFF7043), PandaBackground.Motif.GradientHorizon),
            PandaBackground(18, "Bamboo Grove", c(0xFFE8F5E9), c(0xFF1B5E20), PandaBackground.Motif.BambooGrove),
            PandaBackground(19, "Pink Paws", c(0xFFFFEBEE), c(0xFFD81B60), PandaBackground.Motif.PawPrints),
            PandaBackground(20, "Zen Circles", c(0xFFF5F5F5), c(0xFF424242), PandaBackground.Motif.ZenCircles),
            PandaBackground(21, "Confetti", c(0xFFFFFDE7), c(0xFF00838F), PandaBackground.Motif.Confetti),
            PandaBackground(22, "Falling Leaves", c(0xFFFFF3E0), c(0xFFBF360C), PandaBackground.Motif.FallingLeaves),
            PandaBackground(23, "Moon Night", c(0xFF1A237E), c(0xFFFFF59D), PandaBackground.Motif.MoonAndStars),
            PandaBackground(24, "Sun Rays", c(0xFFFFFDE7), c(0xFFFFB300), PandaBackground.Motif.SunRays),
            PandaBackground(25, "Rain Day", c(0xFFCFD8DC), c(0xFF455A64), PandaBackground.Motif.RainDrops),
            PandaBackground(26, "Snowflakes", c(0xFFECEFF1), c(0xFF90A4AE), PandaBackground.Motif.Snowflakes),
            PandaBackground(27, "Cherry Picnic", c(0xFFFFEBEE), c(0xFFB71C1C), PandaBackground.Motif.Cherries),
            PandaBackground(28, "Cloud Day", c(0xFFE1F5FE), c(0xFFB3E5FC), PandaBackground.Motif.Clouds),
            PandaBackground(29, "Boba Time", c(0xFFFFF3E0), c(0xFF6D4C41), PandaBackground.Motif.Boba),
            PandaBackground(30, "Panda Parade", c(0xFFE0F7FA), c(0xFF263238), PandaBackground.Motif.PandaSilhouettesRow)
        )
    }

    fun byId(id: Int): PandaBackground? = if (id <= 0) null else all.firstOrNull { it.id == id }

    private fun c(hex: Long): Color = Color(hex)
}

/**
 * Full-size background. Render BEHIND the note body. Set the receiving Modifier to fill the
 * available space; the drawing is scaled with the size of this Box, not absolute pixels.
 */
@Composable
fun PandaBackgroundCanvas(
    backgroundId: Int,
    modifier: Modifier = Modifier
) {
    val design = remember(backgroundId) { PandaBackgrounds.byId(backgroundId) } ?: return
    Canvas(modifier = modifier
        .fillMaxSize()
        .background(design.baseColor)
    ) {
        drawMotif(design)
    }
}

/** Square-ish thumbnail used by the background picker grid. */
@Composable
fun PandaBackgroundThumb(
    design: PandaBackground,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.background(design.baseColor)) {
        drawMotif(design)
    }
}

// -----------------------------------------------------------------------------------------
//  Motif renderers — keep each as a single, fully self-contained DrawScope call so the
//  caller can scale up/down without re-thinking layout. All randomness is seeded by `id`
//  so a given background always looks the same.
// -----------------------------------------------------------------------------------------

private fun DrawScope.drawMotif(d: PandaBackground) {
    val r = Random(d.id * 9176L + 7L)
    val accent = d.accentColor
    when (d.motif) {
        PandaBackground.Motif.SleepingPanda -> drawSleepingPanda(accent)
        PandaBackground.Motif.PawPrints -> drawPawPrints(accent, r)
        PandaBackground.Motif.BambooStalks -> drawBambooStalks(accent)
        PandaBackground.Motif.LeafScatter -> drawLeafScatter(accent, r)
        PandaBackground.Motif.HeartScatter -> drawHeartScatter(accent, r)
        PandaBackground.Motif.DottedGrid -> drawDottedGrid(accent)
        PandaBackground.Motif.DiagonalLines -> drawDiagonalLines(accent)
        PandaBackground.Motif.CirclesScatter -> drawCirclesScatter(accent, r)
        PandaBackground.Motif.TrianglesScatter -> drawTrianglesScatter(accent, r)
        PandaBackground.Motif.StarsScatter -> drawStarsScatter(accent, r)
        PandaBackground.Motif.WaveLines -> drawWaveLines(accent)
        PandaBackground.Motif.ConcentricRings -> drawConcentricRings(accent)
        PandaBackground.Motif.CornerPanda -> drawCornerPanda(accent)
        PandaBackground.Motif.FullPandaFaceTile -> drawPandaFaceTile(accent)
        PandaBackground.Motif.MicroPandas -> drawMicroPandas(accent, r)
        PandaBackground.Motif.SoftGradient -> drawSoftGradient(accent)
        PandaBackground.Motif.GradientHorizon -> drawGradientHorizon(d.baseColor, accent)
        PandaBackground.Motif.BambooGrove -> drawBambooGrove(accent, r)
        PandaBackground.Motif.PawTrail -> drawPawTrail(accent)
        PandaBackground.Motif.ZenCircles -> drawZenCircles(accent)
        PandaBackground.Motif.Confetti -> drawConfetti(accent, r)
        PandaBackground.Motif.FallingLeaves -> drawFallingLeaves(accent, r)
        PandaBackground.Motif.MoonAndStars -> drawMoonAndStars(accent, r)
        PandaBackground.Motif.SunRays -> drawSunRays(accent)
        PandaBackground.Motif.RainDrops -> drawRainDrops(accent, r)
        PandaBackground.Motif.Snowflakes -> drawSnowflakes(accent, r)
        PandaBackground.Motif.Cherries -> drawCherries(accent, r)
        PandaBackground.Motif.Clouds -> drawClouds(accent, r)
        PandaBackground.Motif.Boba -> drawBoba(accent, r)
        PandaBackground.Motif.PandaSilhouettesRow -> drawPandaSilhouettesRow(accent)
    }
}

// --- Atomic helpers ---------------------------------------------------------------------

private fun DrawScope.pandaHead(
    center: Offset,
    radius: Float,
    ink: Color,
    eyesOpen: Boolean = true
) {
    val white = Color(0xFFFAFAFA)
    drawCircle(white, radius = radius, center = center)
    drawCircle(ink.copy(alpha = 0.18f), radius = radius, center = center, style = Stroke(width = 1.5f))
    drawCircle(ink, radius = radius * 0.35f, center = center + Offset(-radius * 0.85f, -radius * 0.7f))
    drawCircle(ink, radius = radius * 0.35f, center = center + Offset(radius * 0.85f, -radius * 0.7f))
    if (eyesOpen) {
        drawCircle(ink, radius = radius * 0.4f, center = center + Offset(-radius * 0.35f, -radius * 0.05f))
        drawCircle(ink, radius = radius * 0.4f, center = center + Offset(radius * 0.35f, -radius * 0.05f))
        drawCircle(white, radius = radius * 0.13f, center = center + Offset(-radius * 0.3f, -radius * 0.1f))
        drawCircle(white, radius = radius * 0.13f, center = center + Offset(radius * 0.4f, -radius * 0.1f))
    } else {
        // Closed-eye arcs — two short horizontal strokes that read as "sleeping".
        drawLine(ink, center + Offset(-radius * 0.6f, 0f), center + Offset(-radius * 0.1f, 0f),
            strokeWidth = 2f)
        drawLine(ink, center + Offset(radius * 0.1f, 0f), center + Offset(radius * 0.6f, 0f),
            strokeWidth = 2f)
    }
    drawCircle(ink, radius = radius * 0.13f, center = center + Offset(0f, radius * 0.25f))
}

private fun DrawScope.leaf(center: Offset, len: Float, angleDeg: Float, color: Color) {
    rotate(angleDeg, pivot = center) {
        val path = Path().apply {
            moveTo(center.x, center.y - len / 2f)
            quadraticBezierTo(center.x + len / 3f, center.y, center.x, center.y + len / 2f)
            quadraticBezierTo(center.x - len / 3f, center.y, center.x, center.y - len / 2f)
            close()
        }
        drawPath(path, color)
    }
}

private fun DrawScope.heart(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y + size * 0.3f)
        cubicTo(
            center.x - size, center.y - size * 0.2f,
            center.x - size * 0.5f, center.y - size,
            center.x, center.y - size * 0.4f
        )
        cubicTo(
            center.x + size * 0.5f, center.y - size,
            center.x + size, center.y - size * 0.2f,
            center.x, center.y + size * 0.3f
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.star(center: Offset, radius: Float, color: Color, points: Int = 5) {
    val path = Path()
    val outer = radius
    val inner = radius * 0.4f
    val step = (PI / points).toFloat()
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outer else inner
        val angle = -PI.toFloat() / 2f + i * step
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

private fun DrawScope.pawPrint(center: Offset, scale: Float, color: Color) {
    drawOval(
        color = color,
        topLeft = Offset(center.x - scale * 0.75f, center.y - scale * 0.3f),
        size = Size(scale * 1.5f, scale * 1.2f)
    )
    for (i in 0..3) {
        val angle = (i - 1.5f) * 0.6f
        val px = center.x + scale * 1.4f * sin(angle)
        val py = center.y - scale * 1.1f - scale * 0.3f * cos(angle)
        drawOval(
            color = color,
            topLeft = Offset(px - scale * 0.3f, py - scale * 0.45f),
            size = Size(scale * 0.6f, scale * 0.9f)
        )
    }
}

// --- Patterns ---------------------------------------------------------------------------

private fun DrawScope.drawSleepingPanda(ink: Color) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = minOf(size.width, size.height) * 0.18f
    pandaHead(Offset(centerX, centerY), radius, ink.copy(alpha = 0.18f), eyesOpen = false)
    val zPositions = listOf(0.6f to 0.9f, 0.8f to 0.7f, 1.0f to 0.5f)
    zPositions.forEach { (sx, sy) ->
        val cx = centerX + radius * 2.0f * sx
        val cy = centerY - radius * 2.0f * sy
        val s = radius * 0.4f * (1.2f - sy + 0.4f)
        drawLine(ink.copy(alpha = 0.18f), Offset(cx, cy), Offset(cx + s, cy), strokeWidth = 2.5f)
        drawLine(ink.copy(alpha = 0.18f), Offset(cx + s, cy), Offset(cx, cy + s), strokeWidth = 2.5f)
        drawLine(ink.copy(alpha = 0.18f), Offset(cx, cy + s), Offset(cx + s, cy + s), strokeWidth = 2.5f)
    }
}

private fun DrawScope.drawPawPrints(ink: Color, r: Random) {
    repeat(28) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val s = 14f + r.nextFloat() * 10f
        rotate(r.nextFloat() * 360f, pivot = Offset(cx, cy)) {
            pawPrint(Offset(cx, cy), s, ink.copy(alpha = 0.18f))
        }
    }
}

private fun DrawScope.drawBambooStalks(ink: Color) {
    val cols = 6
    val gap = size.width / (cols + 1)
    for (i in 1..cols) {
        val x = i * gap
        drawLine(ink.copy(alpha = 0.22f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 6f)
        var y = 30f
        while (y < size.height) {
            drawLine(ink.copy(alpha = 0.4f),
                Offset(x - 6f, y), Offset(x + 6f, y), strokeWidth = 2f)
            y += 60f
        }
    }
}

private fun DrawScope.drawLeafScatter(ink: Color, r: Random) {
    repeat(36) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val len = 18f + r.nextFloat() * 22f
        leaf(Offset(cx, cy), len, r.nextFloat() * 180f, ink.copy(alpha = 0.2f))
    }
}

private fun DrawScope.drawHeartScatter(ink: Color, r: Random) {
    repeat(24) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val s = 8f + r.nextFloat() * 12f
        heart(Offset(cx, cy), s, ink.copy(alpha = 0.2f))
    }
}

private fun DrawScope.drawDottedGrid(ink: Color) {
    val step = 28f
    var x = step / 2f
    while (x < size.width) {
        var y = step / 2f
        while (y < size.height) {
            drawCircle(ink.copy(alpha = 0.18f), radius = 2f, center = Offset(x, y))
            y += step
        }
        x += step
    }
}

private fun DrawScope.drawDiagonalLines(ink: Color) {
    val step = 24f
    val d = size.width + size.height
    var i = -size.height
    while (i < d) {
        drawLine(ink.copy(alpha = 0.18f),
            Offset(i, 0f), Offset(i + size.height, size.height), strokeWidth = 1.5f)
        i += step
    }
}

private fun DrawScope.drawCirclesScatter(ink: Color, r: Random) {
    repeat(22) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val rad = 14f + r.nextFloat() * 28f
        drawCircle(ink.copy(alpha = 0.13f), radius = rad, center = Offset(cx, cy))
    }
}

private fun DrawScope.drawTrianglesScatter(ink: Color, r: Random) {
    repeat(18) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val s = 18f + r.nextFloat() * 18f
        val p = Path().apply {
            moveTo(cx, cy - s)
            lineTo(cx - s, cy + s)
            lineTo(cx + s, cy + s)
            close()
        }
        rotate(r.nextFloat() * 360f, pivot = Offset(cx, cy)) {
            drawPath(p, ink.copy(alpha = 0.18f))
        }
    }
}

private fun DrawScope.drawStarsScatter(ink: Color, r: Random) {
    repeat(80) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val rad = 1.5f + r.nextFloat() * 2.5f
        drawCircle(ink.copy(alpha = 0.55f), radius = rad, center = Offset(cx, cy))
    }
    repeat(8) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        star(Offset(cx, cy), 6f + r.nextFloat() * 6f, ink.copy(alpha = 0.85f))
    }
}

private fun DrawScope.drawWaveLines(ink: Color) {
    val rows = 9
    val rowGap = size.height / rows
    for (i in 0..rows) {
        val y = i * rowGap
        val p = Path().apply {
            moveTo(0f, y)
            var x = 0f
            while (x < size.width) {
                quadraticBezierTo(x + 14f, y - 10f, x + 28f, y)
                quadraticBezierTo(x + 42f, y + 10f, x + 56f, y)
                x += 56f
            }
        }
        drawPath(p, ink.copy(alpha = 0.22f), style = Stroke(width = 2f))
    }
}

private fun DrawScope.drawConcentricRings(ink: Color) {
    val cx = size.width / 2f
    val cy = size.height * 0.45f
    var radius = size.width * 0.08f
    while (radius < size.height) {
        drawCircle(ink.copy(alpha = 0.12f),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 2f))
        radius += 28f
    }
}

private fun DrawScope.drawCornerPanda(ink: Color) {
    val radius = minOf(size.width, size.height) * 0.32f
    pandaHead(Offset(size.width - radius * 0.6f, size.height - radius * 0.6f), radius, ink.copy(alpha = 0.10f))
}

private fun DrawScope.drawPandaFaceTile(ink: Color) {
    val cols = 4
    val tile = size.width / cols
    val rows = (size.height / tile).toInt() + 1
    for (c in 0 until cols) {
        for (r in 0 until rows) {
            val cx = c * tile + tile / 2f
            val cy = r * tile + tile / 2f
            pandaHead(Offset(cx, cy), tile * 0.32f, ink.copy(alpha = 0.10f))
        }
    }
}

private fun DrawScope.drawMicroPandas(ink: Color, r: Random) {
    repeat(20) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        pandaHead(Offset(cx, cy), 9f + r.nextFloat() * 5f, ink.copy(alpha = 0.18f))
    }
}

private fun DrawScope.drawSoftGradient(accent: Color) {
    drawRect(
        brush = Brush.verticalGradient(listOf(Color.Transparent, accent.copy(alpha = 0.5f))),
        size = size
    )
}

private fun DrawScope.drawGradientHorizon(base: Color, accent: Color) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to base,
            0.55f to base.copy(alpha = 0.8f),
            1f to accent.copy(alpha = 0.65f)
        ),
        size = size
    )
    drawCircle(
        accent.copy(alpha = 0.55f),
        radius = size.width * 0.16f,
        center = Offset(size.width * 0.7f, size.height * 0.62f)
    )
}

private fun DrawScope.drawBambooGrove(ink: Color, r: Random) {
    val stalks = 9
    val gap = size.width / (stalks + 1)
    for (i in 1..stalks) {
        val x = i * gap + (r.nextFloat() - 0.5f) * 8f
        val w = 4f + r.nextFloat() * 5f
        drawLine(ink.copy(alpha = 0.18f),
            Offset(x, 0f), Offset(x, size.height), strokeWidth = w)
        var y = 20f + r.nextFloat() * 40f
        while (y < size.height) {
            drawLine(ink.copy(alpha = 0.32f),
                Offset(x - w, y), Offset(x + w, y), strokeWidth = 1.5f)
            y += 50f + r.nextFloat() * 20f
        }
    }
}

private fun DrawScope.drawPawTrail(ink: Color) {
    var x = 30f
    var y = 30f
    var step = 0
    while (y < size.height - 30f) {
        val offsetX = if (step % 2 == 0) 0f else 30f
        rotate(if (step % 2 == 0) -15f else 15f, pivot = Offset(x + offsetX, y)) {
            pawPrint(Offset(x + offsetX, y), 12f, ink.copy(alpha = 0.25f))
        }
        x += 50f
        if (x > size.width - 30f) { x = 30f; y += 80f }
        step++
    }
}

private fun DrawScope.drawZenCircles(ink: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    drawCircle(ink.copy(alpha = 0.06f), radius = size.width * 0.4f, center = Offset(cx, cy))
    drawCircle(ink.copy(alpha = 0.10f), radius = size.width * 0.28f, center = Offset(cx, cy))
    drawCircle(ink.copy(alpha = 0.14f), radius = size.width * 0.16f, center = Offset(cx, cy),
        style = Stroke(width = 2f))
    pandaHead(Offset(cx, cy), size.width * 0.08f, ink.copy(alpha = 0.30f))
}

private fun DrawScope.drawConfetti(ink: Color, r: Random) {
    repeat(80) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val w = 4f + r.nextFloat() * 8f
        val h = 2f + r.nextFloat() * 4f
        rotate(r.nextFloat() * 360f, pivot = Offset(cx, cy)) {
            drawRect(
                color = ink.copy(alpha = 0.35f),
                topLeft = Offset(cx - w / 2f, cy - h / 2f),
                size = Size(w, h)
            )
        }
    }
}

private fun DrawScope.drawFallingLeaves(ink: Color, r: Random) {
    repeat(20) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        leaf(Offset(cx, cy), 22f + r.nextFloat() * 18f, r.nextFloat() * 90f - 45f,
            ink.copy(alpha = 0.18f + r.nextFloat() * 0.10f))
    }
}

private fun DrawScope.drawMoonAndStars(ink: Color, r: Random) {
    repeat(60) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        drawCircle(ink.copy(alpha = 0.6f), radius = 1f + r.nextFloat() * 2f, center = Offset(cx, cy))
    }
    val moonCx = size.width * 0.75f
    val moonCy = size.height * 0.20f
    val moonR = size.width * 0.10f
    drawCircle(ink.copy(alpha = 0.85f), radius = moonR, center = Offset(moonCx, moonCy))
    drawCircle(Color(0xFF1A237E), radius = moonR * 0.85f,
        center = Offset(moonCx + moonR * 0.35f, moonCy - moonR * 0.05f))
}

private fun DrawScope.drawSunRays(ink: Color) {
    val cx = size.width / 2f
    val cy = size.height * 0.25f
    drawCircle(ink.copy(alpha = 0.5f), radius = size.width * 0.10f, center = Offset(cx, cy))
    val count = 12
    for (i in 0 until count) {
        val angle = (i * 2 * PI / count).toFloat()
        val sx = cx + size.width * 0.13f * cos(angle)
        val sy = cy + size.width * 0.13f * sin(angle)
        val ex = cx + size.width * 0.42f * cos(angle)
        val ey = cy + size.width * 0.42f * sin(angle)
        drawLine(ink.copy(alpha = 0.40f),
            Offset(sx, sy), Offset(ex, ey), strokeWidth = 3f)
    }
}

private fun DrawScope.drawRainDrops(ink: Color, r: Random) {
    repeat(60) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        drawLine(ink.copy(alpha = 0.5f),
            Offset(cx, cy), Offset(cx + 4f, cy + 14f), strokeWidth = 2f)
    }
}

private fun DrawScope.drawSnowflakes(ink: Color, r: Random) {
    repeat(50) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val s = 3f + r.nextFloat() * 5f
        drawCircle(ink.copy(alpha = 0.6f), radius = s, center = Offset(cx, cy))
    }
}

private fun DrawScope.drawCherries(ink: Color, r: Random) {
    repeat(20) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        drawCircle(ink.copy(alpha = 0.55f), radius = 7f, center = Offset(cx, cy))
        drawCircle(ink.copy(alpha = 0.55f), radius = 7f, center = Offset(cx + 14f, cy + 3f))
        drawLine(Color(0xFF558B2F).copy(alpha = 0.6f),
            Offset(cx, cy), Offset(cx + 7f, cy - 16f), strokeWidth = 2f)
        drawLine(Color(0xFF558B2F).copy(alpha = 0.6f),
            Offset(cx + 14f, cy + 3f), Offset(cx + 7f, cy - 16f), strokeWidth = 2f)
    }
}

private fun DrawScope.drawClouds(ink: Color, r: Random) {
    repeat(8) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val s = 18f + r.nextFloat() * 16f
        drawCircle(ink.copy(alpha = 0.7f), radius = s, center = Offset(cx, cy))
        drawCircle(ink.copy(alpha = 0.7f), radius = s * 0.8f, center = Offset(cx + s, cy + 4f))
        drawCircle(ink.copy(alpha = 0.7f), radius = s * 0.9f, center = Offset(cx - s * 0.8f, cy + 6f))
    }
}

private fun DrawScope.drawBoba(ink: Color, r: Random) {
    repeat(14) {
        val cx = r.nextFloat() * size.width
        val cy = r.nextFloat() * size.height
        val cupW = 38f
        val cupH = 56f
        drawRect(ink.copy(alpha = 0.18f),
            topLeft = Offset(cx, cy), size = Size(cupW, cupH))
        // beads at bottom
        for (i in 0..2) {
            drawCircle(ink.copy(alpha = 0.7f),
                radius = 3f,
                center = Offset(cx + 10f + i * 8f, cy + cupH - 6f))
        }
        // straw
        rotate(15f, pivot = Offset(cx + cupW / 2f, cy)) {
            drawRect(ink.copy(alpha = 0.55f),
                topLeft = Offset(cx + cupW / 2f - 2f, cy - 22f), size = Size(4f, 22f))
        }
    }
}

private fun DrawScope.drawPandaSilhouettesRow(ink: Color) {
    val pandas = 4
    val gap = size.width / pandas
    val cy = size.height * 0.78f
    val rad = gap * 0.28f
    for (i in 0 until pandas) {
        val cx = i * gap + gap / 2f
        pandaHead(Offset(cx, cy), rad, ink.copy(alpha = 0.20f))
    }
    val secondCy = size.height * 0.30f
    for (i in 0 until pandas - 1) {
        val cx = (i + 0.5f) * gap + gap / 2f
        pandaHead(Offset(cx, secondCy), rad * 0.85f, ink.copy(alpha = 0.20f))
    }
}

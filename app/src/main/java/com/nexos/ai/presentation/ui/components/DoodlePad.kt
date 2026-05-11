package com.nexos.ai.presentation.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

/**
 * Lightweight freehand drawing screen. Captures strokes as lists of [Offset] in a
 * compose-mutable buffer, replays them onto a Canvas every recomposition. The final
 * doodle is rasterised to a PNG inside the app's filesDir/doodles when the user accepts,
 * and the file's absolute path is returned to the caller (the EditNote flow wraps it as
 * a [com.nexos.ai.domain.model.NoteAttachment.Image]).
 *
 * Tradeoffs:
 *   - Stroke storage is in-memory only; we don't persist intermediate doodles, just the
 *     final PNG attachment. Means the user can't re-open a saved doodle to keep editing,
 *     which is a deliberate v1 limitation (full vector editing would warrant Room rows).
 *   - Each stroke is one continuous path. Lifting the finger commits the path; from that
 *     point the Undo button removes whole strokes (not points).
 *   - 5 colour swatches and a stroke-width slider cover ~95% of casual doodling needs;
 *     we intentionally don't include a fill bucket / shape primitives.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoodlePadScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val strokes = remember { mutableStateListOf<DoodleStroke>() }
    var current by remember { mutableStateOf<DoodleStroke?>(null) }
    var currentColor by remember { mutableStateOf(Color(0xFF263238)) }
    var widthDp by remember { mutableStateOf(4f) }
    val widthPx = with(density) { widthDp.dp.toPx() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val palette = listOf(
        Color(0xFF263238), // ink
        Color(0xFFD32F2F), // red
        Color(0xFF1565C0), // blue
        Color(0xFF2E7D32), // green
        Color(0xFFFFB300)  // amber
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doodle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = { strokes.clear() }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Clear")
                    }
                    IconButton(onClick = {
                        val w = canvasSize.width.takeIf { it > 0 } ?: 1080
                        val h = canvasSize.height.takeIf { it > 0 } ?: 1920
                        val path = rasteriseToPng(context, strokes.toList(), w, h)
                        if (path != null) onSaved(path) else onBack()
                    }) {
                        Icon(Icons.Rounded.Check, contentDescription = "Save")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palette.forEach { swatch ->
                        val selected = swatch == currentColor
                        Box(
                            modifier = Modifier
                                .size(if (selected) 32.dp else 26.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .border(
                                    if (selected) 2.dp else 1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                                .pointerInput(swatch) {
                                    detectDragGestures(
                                        onDragStart = { currentColor = swatch },
                                        onDrag = { _, _ -> },
                                        onDragEnd = {},
                                        onDragCancel = {}
                                    )
                                }
                        )
                        Spacer(Modifier.width(2.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Stroke",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Slider(
                        value = widthDp,
                        onValueChange = { widthDp = it },
                        valueRange = 1.5f..14f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(Color(0xFFFAFAFA))
                .pointerInput(currentColor, widthPx) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            current = DoodleStroke(listOf(offset), currentColor, widthPx)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            current = current?.let { s ->
                                s.copy(points = s.points + change.position)
                            }
                        },
                        onDragEnd = {
                            current?.let { strokes.add(it) }
                            current = null
                        },
                        onDragCancel = { current = null }
                    )
                }
        ) {
            Canvas(modifier = Modifier
                .fillMaxSize()
            ) {
                if (size.width.toInt() != canvasSize.width ||
                    size.height.toInt() != canvasSize.height) {
                    canvasSize = IntSize(size.width.toInt(), size.height.toInt())
                }
                fun draw(s: DoodleStroke) {
                    if (s.points.size < 2) {
                        if (s.points.isNotEmpty()) {
                            drawCircle(s.color,
                                radius = s.widthPx / 2f,
                                center = s.points.first())
                        }
                        return
                    }
                    val path = Path().apply {
                        moveTo(s.points.first().x, s.points.first().y)
                        for (i in 1 until s.points.size) {
                            lineTo(s.points[i].x, s.points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = s.color,
                        style = Stroke(
                            width = s.widthPx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
                strokes.forEach { draw(it) }
                current?.let { draw(it) }
            }
        }
    }
}

/** File-scope so the rasteriser and the stroke buffer share the same type. */
data class DoodleStroke(val points: List<Offset>, val color: Color, val widthPx: Float)

private fun rasteriseToPng(
    context: android.content.Context,
    strokes: List<DoodleStroke>,
    width: Int,
    height: Int
): String? {
    val safeW = width.coerceAtLeast(8)
    val safeH = height.coerceAtLeast(8)
    val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    strokes.forEach { s ->
        paint.color = android.graphics.Color.argb(
            (s.color.alpha * 255).toInt(),
            (s.color.red * 255).toInt(),
            (s.color.green * 255).toInt(),
            (s.color.blue * 255).toInt()
        )
        paint.strokeWidth = s.widthPx
        if (s.points.size < 2) {
            if (s.points.isNotEmpty()) {
                val p = s.points.first()
                paint.style = Paint.Style.FILL
                canvas.drawCircle(p.x, p.y, s.widthPx / 2f, paint)
                paint.style = Paint.Style.STROKE
            }
            return@forEach
        }
        val path = android.graphics.Path().apply {
            moveTo(s.points.first().x, s.points.first().y)
            for (i in 1 until s.points.size) lineTo(s.points[i].x, s.points[i].y)
        }
        canvas.drawPath(path, paint)
    }
    val outDir = File(context.filesDir, "doodles").also { it.mkdirs() }
    val outFile = File(outDir, "doodle_${System.currentTimeMillis()}.png")
    return runCatching {
        FileOutputStream(outFile).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
        outFile.absolutePath
    }.getOrNull()
}

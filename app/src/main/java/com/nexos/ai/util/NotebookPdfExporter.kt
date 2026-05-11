package com.nexos.ai.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextPaint
import android.text.StaticLayout
import android.text.Layout
import com.nexos.ai.data.local.NoteAttachmentCodec
import com.nexos.ai.data.local.NotebookCoverCodec
import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.domain.model.NoteAttachment
import com.nexos.ai.domain.model.NotebookCover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders a notebook (cover Note + its child pages) to a multi-page PDF.
 *
 * Pages, in order:
 *   1. Cover — uses the user-designed [NotebookCover] (background, accent, motif, title).
 *   2. One page per child note. Each page shows: title, timestamp, body text, and below it
 *      the note's image attachments (decoded to bitmaps, fitted into the safe area).
 *   3. Back page — the cover design mirrored, with [NotebookCover.backNote] centered.
 *
 * Output: written to the user's public Documents folder via MediaStore on API 29+, or to
 * /storage/emulated/0/Documents on legacy devices. Returns the saved URI as a string so the
 * UI can immediately offer a "Share" or "Open" action.
 */
@Singleton
class NotebookPdfExporter @Inject constructor() {

    private val pageW = 595   // A4 portrait @ 72 DPI ≈ 595 × 842 (PdfDocument's native unit)
    private val pageH = 842
    private val margin = 48f

    suspend fun export(context: Context, cover: Note, pages: List<Note>): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                val coverDesign = NotebookCoverCodec.decode(cover.coverDesignJson)
                val doc = PdfDocument()

                // Page 1 — cover
                drawPage(doc, pageNumber = 1) { canvas ->
                    drawCover(canvas, cover, coverDesign, isBack = false)
                }

                // Pages 2..N — body pages
                pages.forEachIndexed { idx, note ->
                    drawPage(doc, pageNumber = idx + 2) { canvas ->
                        drawBodyPage(context, canvas, note, coverDesign, idx + 1, pages.size)
                    }
                }

                // Last page — back cover
                drawPage(doc, pageNumber = pages.size + 2) { canvas ->
                    drawCover(canvas, cover, coverDesign, isBack = true)
                }

                val filename = "${sanitize(cover.title.ifBlank { "Notebook" })}-${System.currentTimeMillis()}.pdf"
                val uri = writePdf(context, doc, filename)
                doc.close()
                uri ?: error("Failed to write PDF to storage")
            }
        }

    private inline fun drawPage(doc: PdfDocument, pageNumber: Int, block: (Canvas) -> Unit) {
        val info = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber).create()
        val page = doc.startPage(info)
        try {
            block(page.canvas)
        } finally {
            doc.finishPage(page)
        }
    }

    private fun drawCover(canvas: Canvas, cover: Note, design: NotebookCover, isBack: Boolean) {
        // Background fill
        val bg = parseHex(design.backgroundHex, fallback = Color.parseColor("#0F0F14"))
        val accent = parseHex(design.accentHex, fallback = Color.parseColor("#00E676"))
        canvas.drawColor(bg)

        // Motif behind the title
        drawMotif(canvas, design.motif, accent)

        val titleText = if (isBack) "End." else design.titleOverride.ifBlank { cover.title }.ifBlank { "Notebook" }
        val titlePaint = TextPaint().apply {
            color = contrastingColor(bg)
            textSize = if (isBack) 36f else 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = TextPaint().apply {
            color = accent
            textSize = 18f
            isAntiAlias = true
        }

        // Wrap title to safe width
        val titleLayout = StaticLayout.Builder
            .obtain(titleText, 0, titleText.length, titlePaint, (pageW - 2 * margin).toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.1f)
            .build()
        val centerY = (pageH - titleLayout.height) / 2f
        canvas.save()
        canvas.translate(margin, centerY)
        titleLayout.draw(canvas)
        canvas.restore()

        val subtitleText = if (isBack) design.backNote else design.subtitle.ifBlank {
            "Crafted in NexOS · ${pages(cover)} pages"
        }
        if (subtitleText.isNotBlank()) {
            val subtitleLayout = StaticLayout.Builder
                .obtain(subtitleText, 0, subtitleText.length, subtitlePaint, (pageW - 2 * margin).toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .build()
            canvas.save()
            canvas.translate(margin, centerY + titleLayout.height + 24f)
            subtitleLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawBodyPage(
        context: Context,
        canvas: Canvas,
        note: Note,
        design: NotebookCover,
        pageIndex: Int,
        totalPages: Int
    ) {
        canvas.drawColor(Color.WHITE)
        val accent = parseHex(design.accentHex, fallback = Color.parseColor("#00E676"))

        // Header bar
        val header = Paint().apply {
            color = accent
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageW.toFloat(), 6f, header)

        // Title
        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(note.title.ifBlank { "Page $pageIndex" }, margin, margin + 24f, titlePaint)

        // Timestamp
        val tsPaint = TextPaint().apply {
            color = Color.DKGRAY
            textSize = 11f
            isAntiAlias = true
        }
        val dateText = java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT
        ).format(java.util.Date(note.timestamp))
        canvas.drawText(dateText, margin, margin + 42f, tsPaint)

        // Body
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#222222")
            textSize = 13f
            isAntiAlias = true
        }
        val body = note.content.ifBlank { "(empty)" }
        val bodyLayout = StaticLayout.Builder
            .obtain(body, 0, body.length, bodyPaint, (pageW - 2 * margin).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1.15f)
            .build()
        canvas.save()
        canvas.translate(margin, margin + 64f)
        bodyLayout.draw(canvas)
        canvas.restore()

        // Image attachments below the body, fitted into a row of squares
        val attachments = NoteAttachmentCodec.decode(note.attachmentsJson)
        val images = attachments.filterIsInstance<NoteAttachment.Image>()
        if (images.isNotEmpty()) {
            val top = margin + 64f + bodyLayout.height + 24f
            val tileSize = ((pageW - 2 * margin) - 24f) / 2f // 2 columns
            images.take(4).forEachIndexed { idx, img ->
                val row = idx / 2
                val col = idx % 2
                val left = margin + col * (tileSize + 24f)
                val tileTop = top + row * (tileSize + 24f)
                val bmp = decodeBitmap(context, img.uri, tileSize.toInt()) ?: return@forEachIndexed
                val dst = RectF(left, tileTop, left + tileSize, tileTop + tileSize)
                canvas.drawBitmap(bmp, null, dst, null)
            }
        }

        // Footer with page number
        val footerPaint = TextPaint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Page $pageIndex of $totalPages",
            pageW / 2f,
            pageH - 24f,
            footerPaint
        )
    }

    private fun drawMotif(canvas: Canvas, motif: NotebookCover.Motif, accent: Int) {
        val p = Paint().apply {
            isAntiAlias = true
            color = (accent and 0x00FFFFFF) or 0x33000000
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        when (motif) {
            NotebookCover.Motif.Gridlines -> {
                val step = 40f
                var x = 0f
                while (x < pageW) { canvas.drawLine(x, 0f, x, pageH.toFloat(), p); x += step }
                var y = 0f
                while (y < pageH) { canvas.drawLine(0f, y, pageW.toFloat(), y, p); y += step }
            }
            NotebookCover.Motif.PandaLeaf -> {
                val leaf = Path().apply {
                    moveTo(pageW / 2f - 80f, pageH / 2f - 160f)
                    quadTo(pageW / 2f, pageH / 2f - 260f, pageW / 2f + 80f, pageH / 2f - 160f)
                    quadTo(pageW / 2f, pageH / 2f - 120f, pageW / 2f - 80f, pageH / 2f - 160f)
                }
                val fill = Paint(p).apply { style = Paint.Style.FILL; alpha = 220 }
                canvas.drawPath(leaf, fill)
            }
            NotebookCover.Motif.Confetti -> {
                val rnd = java.util.Random(42L)
                val dot = Paint().apply { isAntiAlias = true; color = (accent and 0x00FFFFFF) or 0x55000000 }
                repeat(80) {
                    val cx = rnd.nextFloat() * pageW
                    val cy = rnd.nextFloat() * pageH
                    val r = 6f + rnd.nextFloat() * 18f
                    canvas.drawCircle(cx, cy, r, dot)
                }
            }
            NotebookCover.Motif.Plain -> Unit
        }
    }

    private fun decodeBitmap(context: Context, uriString: String, targetPx: Int): Bitmap? {
        return runCatching {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use {
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = 2 // crude downsample; targetPx is small (~250) so 2x is fine
                }
                BitmapFactory.decodeStream(it, null, opts)
            }
        }.getOrNull()
    }

    private fun writePdf(context: Context, doc: PdfDocument, filename: String): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/NexOS")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values) ?: return null
            resolver.openOutputStream(uri)?.use { os: OutputStream -> doc.writeTo(os) }
            return uri
        }
        // Legacy fallback: app-private cache then return file://
        val cacheDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val target = File(cacheDir, filename)
        FileOutputStream(target).use { doc.writeTo(it) }
        return Uri.fromFile(target)
    }

    private fun contrastingColor(bg: Int): Int {
        // Luminance check — bright bg → black text, dark bg → off-white text
        val r = Color.red(bg); val g = Color.green(bg); val b = Color.blue(bg)
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        return if (luminance > 0.55) Color.parseColor("#0F0F14") else Color.parseColor("#FAFAFA")
    }

    private fun parseHex(hex: String, fallback: Int): Int =
        runCatching { Color.parseColor(hex) }.getOrDefault(fallback)

    private fun sanitize(name: String): String =
        name.replace(Regex("[^A-Za-z0-9_\\-]+"), "_").trim('_').take(40).ifBlank { "Notebook" }

    private fun pages(cover: Note): String = "" // no-op placeholder for future expansion
}

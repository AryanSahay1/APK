package com.nexos.ai.ocr

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nexos.ai.domain.model.OcrResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * On-device OCR via ML Kit Text Recognition (Latin script).
 *
 * - Pure conversion: Bitmap → OcrResult. No AI calls, no DB writes.
 * - Recognizer is closed on every call to avoid native model leaks.
 * - Confidence is approximated from block count and total character density.
 */
@Singleton
class OcrEngine @Inject constructor() {

    private val tag = "NexOS/OcrEngine"

    suspend fun extractText(bitmap: Bitmap): OcrResult = withContext(Dispatchers.Default) {
        if (bitmap.isRecycled) return@withContext OcrResult.empty("Bitmap was recycled before OCR.")

        val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = suspendCancellableCoroutine<com.google.mlkit.vision.text.Text> { cont ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { e -> if (cont.isActive) cont.resumeWith(Result.failure(e)) }
            }

            val rawText = visionText.text
            val blocks = visionText.textBlocks.map { it.text }
            val cleanText = TextCleaner.clean(rawText)
            val confidence = approximateConfidence(blocks)

            OcrResult(
                rawText = rawText,
                cleanText = cleanText,
                blocks = blocks,
                confidence = confidence,
                isSuccess = true
            )
        } catch (t: Throwable) {
            Log.e(tag, "OCR failed: ${t.message}", t)
            OcrResult.empty(t.message ?: "OCR failed")
        } finally {
            runCatching { recognizer.close() }
        }
    }

    private fun approximateConfidence(blocks: List<String>): Float {
        if (blocks.isEmpty()) return 0f
        val totalChars = blocks.sumOf { it.length }
        if (totalChars == 0) return 0f
        val density = (totalChars.toFloat() / (blocks.size * 80f)).coerceIn(0f, 1f)
        return (0.5f + 0.5f * density).coerceIn(0f, 1f)
    }
}

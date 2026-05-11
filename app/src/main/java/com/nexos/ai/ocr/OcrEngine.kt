package com.nexos.ai.ocr

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nexos.ai.domain.model.OcrResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit-backed OCR. The TextRecognizer holds a native model so it MUST be
 * closed after use — we close it on every call rather than caching it; the
 * cost is tiny and a leaked model OOMs low-RAM devices.
 *
 * All work runs on [Dispatchers.IO]; callers can be on any thread.
 */
@Singleton
class OcrEngine @Inject constructor() {

    suspend fun extractText(bitmap: Bitmap): OcrResult = withContext(Dispatchers.IO) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val visionText = suspendCancellableCoroutine { cont ->
                recognizer.process(image)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
                cont.invokeOnCancellation { /* recognizer is closed in finally */ }
            }

            val blocks = visionText.textBlocks
            if (blocks.isEmpty() || visionText.text.isBlank()) {
                return@withContext OcrResult(
                    rawText = "",
                    cleanText = "",
                    blocks = emptyList(),
                    confidence = 0f,
                    isSuccess = false,
                    error = "No text detected"
                )
            }

            val raw = visionText.text
            val cleaned = TextCleaner.clean(raw)

            OcrResult(
                rawText = raw,
                cleanText = cleaned,
                blocks = blocks.map { it.text },
                confidence = estimateConfidence(blocks),
                isSuccess = cleaned.isNotBlank(),
                error = if (cleaned.isBlank()) "Cleaned text empty after post-processing" else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed", e)
            OcrResult.failure(e.message ?: "Unknown OCR error")
        } finally {
            try {
                recognizer.close()
            } catch (_: Exception) { /* best-effort */ }
        }
    }

    /**
     * ML Kit doesn't expose per-block confidence numbers in the public
     * Latin recogniser, so we approximate quality from text density —
     * long lines with many words signal a successful capture.
     */
    private fun estimateConfidence(
        blocks: List<com.google.mlkit.vision.text.Text.TextBlock>
    ): Float {
        if (blocks.isEmpty()) return 0f
        val totalChars = blocks.sumOf { it.text.length }
        val wordy = blocks.sumOf { it.text.split(Regex("\\s+")).size }
        if (totalChars == 0) return 0f
        val avgWordLen = totalChars.toFloat() / wordy.coerceAtLeast(1)
        return (avgWordLen / 6f).coerceIn(0.4f, 1f)
    }

    private companion object {
        const val TAG = "NexOS/OcrEngine"
    }
}

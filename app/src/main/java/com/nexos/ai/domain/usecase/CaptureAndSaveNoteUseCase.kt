package com.nexos.ai.domain.usecase

import android.graphics.Bitmap
import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.util.NexosOrchestrator
import javax.inject.Inject

/**
 * Single-responsibility use case: turn a captured screenshot into a saved Note.
 *
 * Wraps [NexosOrchestrator.handleScreenshotCapture] so ViewModels never call the orchestrator
 * directly (Clean Architecture per ANDROID_DEV_SKILL.md). The orchestrator remains the only
 * component that knows about OCR, AI, and storage simultaneously.
 */
class CaptureAndSaveNoteUseCase @Inject constructor(
    private val orchestrator: NexosOrchestrator
) {
    suspend operator fun invoke(bitmap: Bitmap?, imagePath: String = ""): Note? =
        orchestrator.handleScreenshotCapture(bitmap, imagePath)
}

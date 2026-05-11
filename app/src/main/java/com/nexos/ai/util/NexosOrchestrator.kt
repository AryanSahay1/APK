package com.nexos.ai.util

import android.graphics.Bitmap
import android.util.Log
import com.nexos.ai.ai.AIRouter
import com.nexos.ai.ai.NoteAIHelper
import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.domain.model.OcrResult
import com.nexos.ai.domain.model.ParsedNote
import com.nexos.ai.domain.model.WorkflowState
import com.nexos.ai.ocr.OcrEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE orchestration layer — the only component that knows about all others.
 *
 * Coordinates Screenshot/Voice/Manual → OCR → AI → Save → Notify, emitting [WorkflowState]
 * updates that the FloatingButtonService and UI observe. Never throws to the caller.
 *
 * Architecture (Layer 4 + Orchestration section): the rest of the system is decoupled. Only
 * this class composes them. Single SharedFlow keeps services and UI loosely coupled.
 */
@Singleton
class NexosOrchestrator @Inject constructor(
    private val noteRepository: NoteRepository,
    private val ocrEngine: OcrEngine,
    private val aiRouter: AIRouter,
    private val noteAiHelper: NoteAIHelper,
    private val notificationHelper: NotificationHelper
) {

    private val tag = "NexOS/Orchestrator"

    private val _state = MutableSharedFlow<WorkflowState>(
        replay = 1,
        extraBufferCapacity = 16
    )
    val state: SharedFlow<WorkflowState> = _state.asSharedFlow()

    init {
        _state.tryEmit(WorkflowState.Idle)
    }

    /**
     * Process a screenshot bitmap end-to-end.
     * Idempotent over failure: any step's exception is converted to WorkflowState.Failed.
     */
    suspend fun handleScreenshotCapture(bitmap: Bitmap?, imagePath: String = ""): Note? {
        return try {
            _state.emit(WorkflowState.Capturing)
            if (bitmap == null) {
                fail("Screen capture returned no image", "capture")
                return null
            }
            _state.emit(WorkflowState.ExtractingText)
            val ocrResult = ocrEngine.extractText(bitmap)
            val parsed = maybeSummarize(ocrResult.cleanText, isVoice = false)
            val note = buildNote(
                parsed = parsed,
                fallbackContent = ocrResult.cleanText,
                fallbackTitle = "Screenshot · ${System.currentTimeMillis().toFormattedDateTime()}",
                sourceType = Constants.SOURCE_SCREENSHOT,
                rawImagePath = imagePath,
                summary = parsed?.summary.orEmpty()
            )
            val saved = save(note) ?: return null
            _state.emit(WorkflowState.Done(saved))
            notificationHelper.showNoteSaved(saved.id, saved.title)
            saved
        } catch (t: Throwable) {
            Log.e(tag, "Screenshot flow failed", t)
            fail(t.message ?: "Unexpected failure", "screenshot")
            null
        }
    }

    /**
     * Process a transcribed voice input end-to-end.
     */
    suspend fun handleVoiceTranscript(transcript: String): Note? {
        return try {
            if (transcript.isBlank()) {
                fail("Empty transcript", "voice")
                return null
            }
            _state.emit(WorkflowState.Saving)
            val cleaned = transcript.trim()
            val parsed = maybeSummarize(cleaned, isVoice = true)
            val note = buildNote(
                parsed = parsed,
                fallbackContent = cleaned,
                fallbackTitle = cleaned.titleCaseSafe(),
                sourceType = Constants.SOURCE_VOICE,
                rawImagePath = "",
                summary = parsed?.summary.orEmpty()
            )
            val saved = save(note) ?: return null
            _state.emit(WorkflowState.Done(saved))
            notificationHelper.showNoteSaved(saved.id, saved.title)
            saved
        } catch (t: Throwable) {
            Log.e(tag, "Voice flow failed", t)
            fail(t.message ?: "Unexpected failure", "voice")
            null
        }
    }

    suspend fun handleManualSave(title: String, content: String, tags: String = ""): Note? {
        return try {
            val safeTitle = title.ifBlank { content.titleCaseSafe() }
            val note = Note(
                title = safeTitle,
                content = content,
                summary = "",
                sourceType = Constants.SOURCE_MANUAL,
                tags = tags
            )
            save(note)
        } catch (t: Throwable) {
            Log.e(tag, "Manual save failed", t)
            fail(t.message ?: "Unexpected failure", "manual")
            null
        }
    }

    suspend fun handleSharedText(text: String): Note? {
        return try {
            if (text.isBlank()) return null
            val parsed = maybeSummarize(text.trim(), isVoice = false)
            val note = buildNote(
                parsed = parsed,
                fallbackContent = text.trim(),
                fallbackTitle = text.titleCaseSafe(),
                sourceType = Constants.SOURCE_SHARED_TEXT,
                rawImagePath = "",
                summary = parsed?.summary.orEmpty()
            )
            val saved = save(note) ?: return null
            _state.emit(WorkflowState.Done(saved))
            notificationHelper.showNoteSaved(saved.id, saved.title)
            saved
        } catch (t: Throwable) {
            Log.e(tag, "Shared text flow failed", t)
            fail(t.message ?: "Unexpected failure", "shared")
            null
        }
    }

    private suspend fun maybeSummarize(text: String, isVoice: Boolean): ParsedNote? {
        if (text.isBlank()) return null
        val provider = aiRouter.getActive()
        if (!provider.isConfigured) return null
        _state.emit(WorkflowState.AiProcessing)
        val prompt = if (isVoice) noteAiHelper.buildVoicePrompt(text) else noteAiHelper.buildScreenshotPrompt(text)
        val response = provider.complete(prompt)
        if (!response.isSuccess) {
            Log.w(tag, "AI summarise failed: ${response.error}")
            return null
        }
        return noteAiHelper.parse(response.text)
    }

    private fun buildNote(
        parsed: ParsedNote?,
        fallbackContent: String,
        fallbackTitle: String,
        sourceType: String,
        rawImagePath: String,
        summary: String
    ): Note {
        val title = parsed?.title?.ifBlank { fallbackTitle } ?: fallbackTitle
        val content = if (parsed != null && (parsed.bullets.isNotEmpty() || parsed.summary.isNotBlank())) {
            parsed.toMarkdownContent().ifBlank { fallbackContent }
        } else {
            fallbackContent
        }
        return Note(
            title = title.take(160),
            content = content,
            summary = summary,
            sourceType = sourceType,
            rawImagePath = rawImagePath
        )
    }

    private suspend fun save(note: Note): Note? = try {
        _state.emit(WorkflowState.Saving)
        val id = noteRepository.insert(note)
        note.copy(id = id)
    } catch (t: Throwable) {
        Log.e(tag, "DB insert failed", t)
        fail(t.message ?: "Database error", "save")
        null
    }

    private suspend fun fail(message: String, step: String) {
        _state.emit(WorkflowState.Failed(message, step))
        notificationHelper.showFailure("$step: $message")
    }
}

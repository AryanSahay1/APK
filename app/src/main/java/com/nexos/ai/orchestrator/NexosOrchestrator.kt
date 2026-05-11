package com.nexos.ai.orchestrator

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nexos.ai.MainActivity
import com.nexos.ai.R
import com.nexos.ai.ai.NoteAIHelper
import com.nexos.ai.data.repository.NoteRepository
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.domain.model.Note
import com.nexos.ai.domain.model.SourceType
import com.nexos.ai.domain.model.WorkflowState
import com.nexos.ai.ocr.OcrEngine
import com.nexos.ai.service.ScreenshotService
import com.nexos.ai.util.NexosActions
import com.nexos.ai.util.NexosChannels
import com.nexos.ai.util.toAutoTitle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The nervous system. The only component that knows about all others.
 *
 * Coordinates: capture → OCR → AI → save → notify.
 *
 * Emits [WorkflowState] on every transition so the floating button and the
 * in-app HUD can both animate in lock-step. Falls back to saving raw text
 * whenever AI is unavailable or fails (Laws 1 + 2).
 */
@Singleton
class NexosOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrEngine: OcrEngine,
    private val noteAIHelper: NoteAIHelper,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository
) {

    private val _state = MutableStateFlow<WorkflowState>(WorkflowState.Idle)
    val state: StateFlow<WorkflowState> = _state.asStateFlow()

    private val _toasts = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toasts: SharedFlow<String> = _toasts.asSharedFlow()

    /**
     * Whole screenshot-to-note flow. Safe to call repeatedly; concurrent
     * calls will overlap and produce two notes — UI throttles via the
     * processing state.
     */
    suspend fun handleScreenshotCapture() {
        _state.emit(WorkflowState.Capturing)
        val service = ScreenshotService.instance
        if (service == null || !ScreenshotService.isReady()) {
            fail("Screen capture isn't ready — grant permission first.", "capture")
            return
        }

        val bitmap = service.captureScreen()
        if (bitmap == null) {
            fail(context.getString(R.string.capture_error_screen), "capture")
            return
        }

        _state.emit(WorkflowState.ExtractingText)
        val ocr = ocrEngine.extractText(bitmap)
        try { bitmap.recycle() } catch (_: Exception) {}

        if (!ocr.isSuccess || ocr.cleanText.isBlank()) {
            saveFallbackNote(
                content = ocr.cleanText.ifBlank { "(no text detected)" },
                source = SourceType.Screenshot,
                titlePrefix = "Screenshot"
            )
            return
        }

        runAiThenSave(
            sourceText = ocr.cleanText,
            source = SourceType.Screenshot,
            buildPrompt = { noteAIHelper.summarizeScreenshot(it) },
            fallbackTitlePrefix = "Screenshot"
        )
    }

    suspend fun handleVoiceCapture(transcript: String) {
        if (transcript.isBlank()) {
            fail("Empty transcript — nothing to save.", "voice")
            return
        }
        runAiThenSave(
            sourceText = transcript,
            source = SourceType.Voice,
            buildPrompt = { noteAIHelper.summarizeVoice(it) },
            fallbackTitlePrefix = "Voice note"
        )
    }

    suspend fun saveManualNote(title: String, content: String, tags: List<String>): Long {
        _state.emit(WorkflowState.Saving)
        val resolvedTitle = title.ifBlank { content.toAutoTitle() }
        val id = noteRepository.insertNote(
            Note(
                title = resolvedTitle,
                content = content,
                summary = "",
                sourceType = SourceType.Manual,
                tags = tags,
                timestamp = System.currentTimeMillis()
            )
        )
        val saved = noteRepository.getNote(id) ?: Note(
            id = id, title = resolvedTitle, content = content, sourceType = SourceType.Manual
        )
        _state.emit(WorkflowState.Done(saved))
        notifyNoteSaved(saved)
        return id
    }

    fun resetState() {
        _state.value = WorkflowState.Idle
    }

    private suspend fun runAiThenSave(
        sourceText: String,
        source: SourceType,
        buildPrompt: suspend (String) -> com.nexos.ai.domain.model.ParsedNote?,
        fallbackTitlePrefix: String
    ) {
        val autoSummarize = settingsRepository.autoSummarize.first()
        val parsed = if (autoSummarize) {
            _state.emit(WorkflowState.AiProcessing)
            try {
                buildPrompt(sourceText)
            } catch (e: Exception) {
                Log.w(TAG, "AI summarisation threw — falling back", e)
                null
            }
        } else null

        _state.emit(WorkflowState.Saving)
        val now = System.currentTimeMillis()
        val note = if (parsed != null && parsed.bullets.isNotEmpty()) {
            Note(
                title = parsed.title.ifBlank { sourceText.toAutoTitle() },
                content = parsed.toContent().ifBlank { sourceText },
                summary = parsed.summary,
                sourceType = source,
                timestamp = now
            )
        } else {
            Note(
                title = sourceText.toAutoTitle().ifBlank { "$fallbackTitlePrefix ${timestampLabel()}" },
                content = sourceText,
                summary = "",
                sourceType = source,
                timestamp = now
            )
        }

        val id = noteRepository.insertNote(note)
        val saved = note.copy(id = id)
        _state.emit(WorkflowState.Done(saved))
        _toasts.tryEmit(context.getString(R.string.capture_success))
        notifyNoteSaved(saved)
    }

    private suspend fun saveFallbackNote(content: String, source: SourceType, titlePrefix: String) {
        _state.emit(WorkflowState.Saving)
        val note = Note(
            title = "$titlePrefix ${timestampLabel()}",
            content = content,
            sourceType = source,
            timestamp = System.currentTimeMillis()
        )
        val id = noteRepository.insertNote(note)
        val saved = note.copy(id = id)
        _state.emit(WorkflowState.Done(saved))
        _toasts.tryEmit(context.getString(R.string.capture_error_ai))
        notifyNoteSaved(saved)
    }

    private suspend fun fail(message: String, step: String) {
        Log.w(TAG, "fail($step): $message")
        _state.emit(WorkflowState.Failed(message, step))
        _toasts.tryEmit(message)
    }

    private fun timestampLabel(): String =
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date())

    private fun notifyNoteSaved(note: Note) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(NexosActions.EXTRA_NOTE_ID, note.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, note.id.toInt().coerceAtLeast(1),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NexosChannels.NOTES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nexos)
            .setContentTitle(context.getString(R.string.notification_service_saved))
            .setContentText(note.title)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }
        NotificationManagerCompat.from(context)
            .notify(NexosChannels.NOTE_SAVED_NOTIFICATION_ID + note.id.toInt(), notification)
    }

    private companion object {
        const val TAG = "NexOS/Orchestrator"
    }
}

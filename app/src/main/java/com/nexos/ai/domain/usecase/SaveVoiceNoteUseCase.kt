package com.nexos.ai.domain.usecase

import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.util.NexosOrchestrator
import javax.inject.Inject

/**
 * Single-responsibility use case: turn a voice transcript into a saved Note.
 */
class SaveVoiceNoteUseCase @Inject constructor(
    private val orchestrator: NexosOrchestrator
) {
    suspend operator fun invoke(transcript: String): Note? =
        orchestrator.handleVoiceTranscript(transcript)
}

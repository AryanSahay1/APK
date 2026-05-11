package com.nexos.ai.domain.usecase

import com.nexos.ai.data.local.entity.Note
import com.nexos.ai.util.NexosOrchestrator
import javax.inject.Inject

/**
 * Single-responsibility use case: persist a manually-entered note.
 */
class SaveManualNoteUseCase @Inject constructor(
    private val orchestrator: NexosOrchestrator
) {
    suspend operator fun invoke(title: String, content: String, tags: String = ""): Note? =
        orchestrator.handleManualSave(title, content, tags)
}

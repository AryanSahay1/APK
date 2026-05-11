package com.nexos.ai.domain.usecase

import com.nexos.ai.ai.AIRouter
import com.nexos.ai.ai.NoteAIHelper
import com.nexos.ai.domain.model.ParsedNote
import javax.inject.Inject

/**
 * Optional summarisation step. Returns null when no provider is configured (Law 1) or when the
 * provider's response cannot be parsed as JSON.
 *
 * Currently only used directly by [com.nexos.ai.util.NexosOrchestrator] but exposed as a use case
 * for ANDROID_DEV_SKILL Clean-Architecture compliance and so future surfaces (e.g. a "re-run AI"
 * button in NoteDetailScreen) can call it without going through the orchestrator.
 */
class SummarizeWithAIUseCase @Inject constructor(
    private val aiRouter: AIRouter,
    private val noteAiHelper: NoteAIHelper
) {
    suspend operator fun invoke(text: String, isVoice: Boolean = false): ParsedNote? {
        if (text.isBlank()) return null
        val provider = aiRouter.getActive()
        if (!provider.isConfigured) return null
        val prompt = if (isVoice) noteAiHelper.buildVoicePrompt(text)
        else noteAiHelper.buildScreenshotPrompt(text)
        val response = provider.complete(prompt)
        if (!response.isSuccess) return null
        return noteAiHelper.parse(response.text)
    }
}

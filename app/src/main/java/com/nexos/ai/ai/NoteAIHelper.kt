package com.nexos.ai.ai

import android.util.Log
import com.nexos.ai.domain.model.ParsedNote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Glue between the orchestrator and [AIRouter]. Builds the right prompt,
 * runs it through the active provider, and parses the JSON response into
 * a [ParsedNote] — or returns `null` so the caller can fall back to raw
 * text storage.
 */
@Singleton
class NoteAIHelper @Inject constructor(
    private val aiRouter: AIRouter
) {

    suspend fun summarizeScreenshot(rawText: String): ParsedNote? =
        runPrompt(AIPrompts.screenshotSummary(rawText))

    suspend fun summarizeVoice(transcript: String): ParsedNote? =
        runPrompt(AIPrompts.voiceSummary(transcript))

    private suspend fun runPrompt(prompt: String): ParsedNote? {
        val provider = aiRouter.getActiveProvider()
        val response = provider.complete(prompt)
        if (!response.isSuccess) {
            Log.i(TAG, "AI fallback: ${response.error}")
            return null
        }
        val parsed = ParsedNoteParser.parse(response.text)
        if (parsed == null) Log.w(TAG, "AI response did not parse as JSON")
        return parsed
    }

    private companion object {
        const val TAG = "NexOS/NoteAIHelper"
    }
}

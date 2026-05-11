package com.nexos.ai.ai

import com.google.gson.JsonParser
import com.nexos.ai.domain.model.ParsedNote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Prompt builders and response parser for note-style AI summarisation.
 *
 * Layer 2 (Intent Engine): produces deterministic JSON-only prompts and a defensive parser
 * that recovers from common AI mis-formatting (markdown fences, preambles, trailing prose).
 */
@Singleton
class NoteAIHelper @Inject constructor() {

    fun buildScreenshotPrompt(rawText: String): String = buildString {
        appendLine("You are a note-taking assistant. The following text was extracted from a screenshot via OCR.")
        appendLine("Create a structured note from this content.")
        appendLine()
        appendLine("Respond ONLY with valid JSON in this exact format:")
        appendLine("{")
        appendLine("  \"title\": \"concise title under 8 words\",")
        appendLine("  \"bullets\": [\"key point 1\", \"key point 2\", \"key point 3\"],")
        appendLine("  \"summary\": \"one sentence summary\"")
        appendLine("}")
        appendLine()
        appendLine("Rules:")
        appendLine("- Title must be under 8 words")
        appendLine("- 3 to 6 bullet points maximum")
        appendLine("- Summary must be one sentence")
        appendLine("- No markdown, no preamble, no code fences — ONLY the JSON object")
        appendLine()
        appendLine("Raw OCR text:")
        append(rawText.take(MAX_INPUT_CHARS))
    }

    fun buildVoicePrompt(transcript: String): String = buildString {
        appendLine("You are a note-taking assistant. Convert this voice transcript into a structured note.")
        appendLine()
        appendLine("Respond ONLY with valid JSON in this exact format:")
        appendLine("{")
        appendLine("  \"title\": \"concise title under 8 words\",")
        appendLine("  \"bullets\": [\"key point 1\", \"key point 2\"],")
        appendLine("  \"summary\": \"one sentence summary\"")
        appendLine("}")
        appendLine()
        appendLine("Rules:")
        appendLine("- Title must be under 8 words")
        appendLine("- 2 to 5 bullet points")
        appendLine("- Summary must be one sentence")
        appendLine("- No markdown, no preamble, no code fences — ONLY the JSON object")
        appendLine()
        appendLine("Transcript:")
        append(transcript.take(MAX_INPUT_CHARS))
    }

    /**
     * Parse AI output into a [ParsedNote]. Returns null when the payload cannot be salvaged.
     * Strips markdown code fences, extracts the first JSON object via brace matching, then
     * delegates to Gson. Never throws.
     */
    fun parse(raw: String): ParsedNote? {
        if (raw.isBlank()) return null
        val candidate = extractJsonObject(raw) ?: return null
        return try {
            val obj = JsonParser.parseString(candidate).asJsonObject
            val title = obj.get("title")?.asString?.trim().orEmpty().ifBlank { return null }
            val summary = obj.get("summary")?.asString?.trim().orEmpty()
            val bulletsJson = obj.getAsJsonArray("bullets")
            val bullets = if (bulletsJson == null) emptyList()
            else bulletsJson.mapNotNull { runCatching { it.asString.trim() }.getOrNull() }.filter { it.isNotBlank() }
            ParsedNote(title = title, bullets = bullets, summary = summary)
        } catch (t: Throwable) {
            null
        }
    }

    private fun extractJsonObject(raw: String): String? {
        val stripped = raw.trim()
            .removePrefix("```json").removePrefix("```JSON").removePrefix("```")
            .removeSuffix("```")
            .trim()

        val firstBrace = stripped.indexOf('{')
        val lastBrace = stripped.lastIndexOf('}')
        if (firstBrace == -1 || lastBrace == -1 || lastBrace < firstBrace) return null
        return stripped.substring(firstBrace, lastBrace + 1)
    }

    companion object {
        const val MAX_INPUT_CHARS = 8_000
    }
}

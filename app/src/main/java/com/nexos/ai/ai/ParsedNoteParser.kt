package com.nexos.ai.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.nexos.ai.domain.model.ParsedNote

/**
 * Robustly parses the JSON output produced by [AIPrompts]. Tolerates the
 * three most common ways an LLM violates "JSON only":
 *  1. Wrapping the JSON in ```json ... ``` fences.
 *  2. Adding leading/trailing prose.
 *  3. Using single quotes around field values.
 *
 * Returns `null` rather than throwing — the orchestrator interprets that
 * as "AI failed" and falls back to raw text storage.
 */
object ParsedNoteParser {

    private val gson = Gson()
    private val fenceRegex = Regex("```(?:json)?\\s*", RegexOption.IGNORE_CASE)
    private val jsonObjectRegex = Regex("\\{[\\s\\S]*\\}")

    fun parse(rawResponse: String): ParsedNote? {
        if (rawResponse.isBlank()) return null

        val withoutFences = rawResponse
            .replace(fenceRegex, "")
            .replace("```", "")
            .trim()

        val jsonChunk = jsonObjectRegex.find(withoutFences)?.value ?: return null

        return try {
            val dto = gson.fromJson(jsonChunk, ParsedDto::class.java)
            ParsedNote(
                title = dto.title.orEmpty().trim().ifBlank { "Untitled note" },
                bullets = dto.bullets.orEmpty().map { it.trim() }.filter { it.isNotBlank() },
                summary = dto.summary.orEmpty().trim()
            )
        } catch (e: JsonSyntaxException) {
            Log.w(TAG, "JSON parse failed: ${e.message}")
            null
        }
    }

    private data class ParsedDto(
        val title: String? = null,
        val bullets: List<String>? = null,
        val summary: String? = null
    )

    private const val TAG = "NexOS/ParsedNoteParser"
}

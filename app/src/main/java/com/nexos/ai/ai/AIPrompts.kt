package com.nexos.ai.ai

/**
 * Prompt templates engineered to produce *strictly JSON* output. The parser
 * in [com.nexos.ai.ai.ParsedNoteParser] tolerates fenced markdown blocks,
 * but instructing the model not to emit them is still the cheapest win.
 */
object AIPrompts {

    fun screenshotSummary(rawText: String): String = """
        You are a note-taking assistant. The text below was extracted via OCR
        from a screenshot. Convert it into a structured note.

        Respond with VALID JSON ONLY in this exact shape:
        {
          "title": "concise title under 8 words",
          "bullets": ["key point 1", "key point 2", "key point 3"],
          "summary": "one sentence summary"
        }

        Rules:
        - Title under 8 words.
        - Provide 3 to 6 bullet points; each is a short sentence.
        - Summary is exactly one sentence.
        - No markdown. No code fences. No preamble. ONLY the JSON object.

        Raw OCR text:
        ---
        $rawText
        ---
    """.trimIndent()

    fun voiceSummary(transcript: String): String = """
        You are a note-taking assistant. Convert this voice transcript into a
        structured note.

        Respond with VALID JSON ONLY in this exact shape:
        {
          "title": "concise title under 8 words",
          "bullets": ["key point 1", "key point 2"],
          "summary": "one sentence summary"
        }

        Rules:
        - Title under 8 words.
        - 2 to 5 bullet points capturing the key takeaways.
        - Summary is exactly one sentence.
        - No markdown. No code fences. No preamble. ONLY the JSON object.

        Transcript:
        ---
        $transcript
        ---
    """.trimIndent()
}

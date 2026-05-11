package com.nexos.ai.ai

import org.junit.Assert.assertTrue
import org.junit.Test

class AIPromptsTest {

    @Test
    fun `screenshot prompt contains the raw text`() {
        val raw = "OCR text from the screen"
        val prompt = AIPrompts.screenshotSummary(raw)
        assertTrue(prompt.contains(raw))
    }

    @Test
    fun `screenshot prompt instructs strictly-JSON output`() {
        val prompt = AIPrompts.screenshotSummary("x")
        assertTrue("must require JSON-only output", prompt.contains("VALID JSON ONLY"))
        assertTrue("must forbid markdown", prompt.lowercase().contains("no markdown"))
    }

    @Test
    fun `voice prompt enforces title length`() {
        val prompt = AIPrompts.voiceSummary("hello there")
        assertTrue(prompt.contains("under 8 words"))
        assertTrue(prompt.contains("hello there"))
    }
}

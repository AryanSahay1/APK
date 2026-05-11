package com.nexos.ai.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextCleanerTest {

    @Test
    fun `blank input returns empty string`() {
        assertEquals("", TextCleaner.clean(""))
        assertEquals("", TextCleaner.clean("    \n\n\n   "))
    }

    @Test
    fun `collapses runs of blank lines`() {
        val raw = "first\n\n\n\nsecond"
        val cleaned = TextCleaner.clean(raw)
        assertEquals("first\n\nsecond", cleaned)
    }

    @Test
    fun `strips zero-width characters`() {
        val raw = "he\u200Bllo"
        val cleaned = TextCleaner.clean(raw)
        assertEquals("hello", cleaned)
    }

    @Test
    fun `collapses multiple spaces`() {
        val raw = "hello       world"
        val cleaned = TextCleaner.clean(raw)
        assertEquals("hello world", cleaned)
    }

    @Test
    fun `drops single noise characters`() {
        val raw = ".\nhello\n@\nworld"
        val cleaned = TextCleaner.clean(raw)
        assertTrue(cleaned.contains("hello"))
        assertTrue(cleaned.contains("world"))
        assertTrue(!cleaned.contains("@"))
    }
}

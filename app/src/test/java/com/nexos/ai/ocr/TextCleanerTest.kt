package com.nexos.ai.ocr

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TextCleanerTest {

    @Test
    fun `empty input returns empty string`() {
        assertThat(TextCleaner.clean("")).isEmpty()
        assertThat(TextCleaner.clean("   ")).isEmpty()
    }

    @Test
    fun `collapses repeated whitespace and trims lines`() {
        val raw = "Hello   world   \n\n\n   foo\tbar  \n   \n   "
        val cleaned = TextCleaner.clean(raw)
        assertThat(cleaned).isEqualTo("Hello world\n\nfoo bar")
    }

    @Test
    fun `removes control characters and zero width invisibles`() {
        val raw = "Hi\u200B\u0001 there\u0007"
        assertThat(TextCleaner.clean(raw)).isEqualTo("Hi there")
    }

    @Test
    fun `drops punctuation-only noise lines`() {
        val raw = """
            Real line
            ---
            another
            ===
            ·
        """.trimIndent()
        val cleaned = TextCleaner.clean(raw)
        assertThat(cleaned).isEqualTo("Real line\nanother")
    }

    @Test
    fun `derive title uses first meaningful line capped at maxWords`() {
        val cleaned = "this is a fairly long title that runs past the cap\nbody"
        val title = TextCleaner.deriveTitle(cleaned, maxWords = 5)
        assertThat(title.split(" ")).hasSize(5)
    }

    @Test
    fun `derive title falls back when input is blank`() {
        assertThat(TextCleaner.deriveTitle("")).isEqualTo("Untitled note")
        assertThat(TextCleaner.deriveTitle("   ")).isEqualTo("Untitled note")
    }
}

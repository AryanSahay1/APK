package com.nexos.ai.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    @Test
    fun `toAutoTitle returns Untitled for blank input`() {
        assertEquals("Untitled note", "".toAutoTitle())
        assertEquals("Untitled note", "   \n\n  ".toAutoTitle())
    }

    @Test
    fun `toAutoTitle truncates to first line and word limit`() {
        val raw = "  Meeting notes from Tuesday's product review session\nNext line"
        val title = raw.toAutoTitle(maxWords = 4)
        assertEquals("Meeting notes from Tuesday's", title)
    }

    @Test
    fun `toRelativeTimeString uses 'just now' for very recent`() {
        val now = 1_000_000_000_000L
        val almost = now - 5_000L
        assertEquals("just now", almost.toRelativeTimeString(now = now))
    }

    @Test
    fun `toRelativeTimeString formats minutes`() {
        val now = 1_000_000_000_000L
        val ago = now - 7 * 60_000L
        assertEquals("7m ago", ago.toRelativeTimeString(now = now))
    }

    @Test
    fun `toRelativeTimeString formats days`() {
        val now = 1_000_000_000_000L
        val ago = now - 3 * 24 * 60 * 60_000L
        assertEquals("3d ago", ago.toRelativeTimeString(now = now))
    }

    @Test
    fun `toRelativeTimeString falls back to absolute beyond a week`() {
        val now = 1_700_000_000_000L
        val ago = now - 30 * 24 * 60 * 60_000L
        val result = ago.toRelativeTimeString(now = now)
        // Should contain a year/month label, not 'd ago'.
        assertTrue("expected absolute date, got '$result'", !result.contains("ago"))
    }
}

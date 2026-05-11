package com.nexos.ai.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ParsedNoteParserTest {

    @Test
    fun `clean json parses to ParsedNote`() {
        val raw = """{"title":"Meeting","bullets":["a","b"],"summary":"s"}"""
        val parsed = ParsedNoteParser.parse(raw)
        assertNotNull(parsed)
        assertEquals("Meeting", parsed!!.title)
        assertEquals(listOf("a", "b"), parsed.bullets)
        assertEquals("s", parsed.summary)
    }

    @Test
    fun `markdown fenced json is unwrapped`() {
        val raw = """```json
{"title":"Hello","bullets":["x"],"summary":"y"}
```"""
        val parsed = ParsedNoteParser.parse(raw)
        assertNotNull(parsed)
        assertEquals("Hello", parsed!!.title)
    }

    @Test
    fun `prose before json is tolerated`() {
        val raw = "Here you go: {\"title\":\"T\",\"bullets\":[],\"summary\":\"S\"}  thanks!"
        val parsed = ParsedNoteParser.parse(raw)
        assertNotNull(parsed)
        assertEquals("T", parsed!!.title)
    }

    @Test
    fun `malformed json returns null instead of throwing`() {
        assertNull(ParsedNoteParser.parse("not json at all"))
        assertNull(ParsedNoteParser.parse("{ broken"))
    }

    @Test
    fun `blank input returns null`() {
        assertNull(ParsedNoteParser.parse(""))
        assertNull(ParsedNoteParser.parse("   "))
    }
}

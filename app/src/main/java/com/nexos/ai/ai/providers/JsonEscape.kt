package com.nexos.ai.ai.providers

/**
 * Minimal JSON string escaper for embedding prompts inside hand-built JSON
 * bodies. We don't use a full serializer because the bodies are tiny and a
 * Gson dependency for a single string field would add little.
 */
internal object JsonEscape {
    fun escape(s: String): String = buildString(s.length + 16) {
        for (c in s) when (c) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
        }
    }
}

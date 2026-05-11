package com.nexos.ai.domain.model

/**
 * Domain representation of a note. Has zero Android dependencies so it can be
 * shared across layers without coupling presentation to Room.
 */
data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val summary: String = "",
    val sourceType: SourceType,
    val timestamp: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val rawImagePath: String = ""
)

enum class SourceType(val key: String) {
    Screenshot("screenshot"),
    Voice("voice"),
    Manual("manual");

    companion object {
        fun fromKey(key: String?): SourceType = entries.firstOrNull { it.key == key } ?: Manual
    }
}

package com.nexos.ai.data.mapper

import com.nexos.ai.data.local.entity.NoteEntity
import com.nexos.ai.domain.model.Note
import com.nexos.ai.domain.model.SourceType

/**
 * Mapping helpers between the Room entity and the pure-Kotlin domain model.
 * Tag lists are serialised as a comma-separated string in storage.
 */

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    summary = summary,
    sourceType = SourceType.fromKey(sourceType),
    timestamp = timestamp,
    tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    rawImagePath = rawImagePath
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    summary = summary,
    sourceType = sourceType.key,
    timestamp = timestamp,
    tags = tags.joinToString(","),
    isSynced = false,
    rawImagePath = rawImagePath
)

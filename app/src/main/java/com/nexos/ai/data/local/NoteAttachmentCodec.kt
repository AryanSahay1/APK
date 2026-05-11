package com.nexos.ai.data.local

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nexos.ai.domain.model.NoteAttachment

/**
 * Encodes [NoteAttachment] lists to a single JSON string and back. Stored as a TEXT column on
 * the notes table (see Note.attachmentsJson). Done by hand instead of Gson's polymorphic
 * deserialiser so the tag layout is stable across refactors and forward-compatible (an
 * unknown `type` value is silently skipped rather than crashing the parse).
 *
 *   [{"type":"image","id":"a1","uri":"content://…","mime":"image/jpeg","ts":1700…},
 *    {"type":"audio","id":"a2","path":"/data/…/voice.m4a","durMs":12340,"ts":1700…},
 *    {"type":"location","id":"a3","lat":12.97,"lng":77.59,"label":"MG Road, Bengaluru","ts":1700…}]
 */
object NoteAttachmentCodec {

    fun encode(attachments: List<NoteAttachment>): String {
        if (attachments.isEmpty()) return ""
        val arr = JsonArray()
        attachments.forEach { att ->
            val obj = JsonObject().apply {
                addProperty("id", att.id)
                when (att) {
                    is NoteAttachment.Image -> {
                        addProperty("type", "image")
                        addProperty("uri", att.uri)
                        addProperty("mime", att.mimeType)
                        addProperty("ts", att.capturedAt)
                    }
                    is NoteAttachment.Audio -> {
                        addProperty("type", "audio")
                        addProperty("path", att.filePath)
                        addProperty("durMs", att.durationMs)
                        addProperty("ts", att.capturedAt)
                    }
                    is NoteAttachment.Location -> {
                        addProperty("type", "location")
                        addProperty("lat", att.latitude)
                        addProperty("lng", att.longitude)
                        addProperty("label", att.label)
                        addProperty("ts", att.capturedAt)
                    }
                }
            }
            arr.add(obj)
        }
        return arr.toString()
    }

    fun decode(json: String): List<NoteAttachment> {
        if (json.isBlank()) return emptyList()
        val parsed = runCatching { JsonParser.parseString(json).asJsonArray }.getOrNull() ?: return emptyList()
        return parsed.mapNotNull { element ->
            val obj = element.asJsonObject
            val id = obj.get("id")?.asString.orEmpty()
            val ts = obj.get("ts")?.asLong ?: System.currentTimeMillis()
            when (obj.get("type")?.asString) {
                "image" -> NoteAttachment.Image(
                    id = id,
                    uri = obj.get("uri")?.asString.orEmpty(),
                    mimeType = obj.get("mime")?.asString ?: "image/*",
                    capturedAt = ts
                )
                "audio" -> NoteAttachment.Audio(
                    id = id,
                    filePath = obj.get("path")?.asString.orEmpty(),
                    durationMs = obj.get("durMs")?.asLong ?: 0L,
                    capturedAt = ts
                )
                "location" -> NoteAttachment.Location(
                    id = id,
                    latitude = obj.get("lat")?.asDouble ?: 0.0,
                    longitude = obj.get("lng")?.asDouble ?: 0.0,
                    label = obj.get("label")?.asString.orEmpty(),
                    capturedAt = ts
                )
                else -> null
            }
        }
    }
}

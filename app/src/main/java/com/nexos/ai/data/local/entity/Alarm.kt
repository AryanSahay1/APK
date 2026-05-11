package com.nexos.ai.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for an alarm / reminder scheduled via the system AlarmManager.
 *
 * Design notes:
 * - [triggerAt] is an absolute epoch millis. The repository computes it once at insert time; we
 *   never store relative offsets so OEM clock changes do not silently re-fire the alarm.
 * - [rawRequest] preserves the natural-language input ("remind me at 8am tomorrow") so the UI
 *   can show what the user actually said.
 * - [pendingIntentRequestCode] equals [id] (cast to Int); used to cancel/replace the
 *   PendingIntent in AlarmManager.
 */
@Immutable
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val rawRequest: String,
    val triggerAt: Long,
    val isEnabled: Boolean = true,
    val isFired: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val pendingIntentRequestCode: Int get() = (id and 0x7fffffff).toInt()
}

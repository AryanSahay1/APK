package com.nexos.ai.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private const val SECOND_MS = 1_000L
private const val MINUTE_MS = 60 * SECOND_MS
private const val HOUR_MS = 60 * MINUTE_MS
private const val DAY_MS = 24 * HOUR_MS
private const val WEEK_MS = 7 * DAY_MS

/**
 * Formats an epoch-millis timestamp as a human-friendly relative time,
 * collapsing to absolute dates for anything older than a week.
 */
fun Long.toRelativeTimeString(now: Long = System.currentTimeMillis()): String {
    val delta = abs(now - this)
    return when {
        delta < MINUTE_MS -> "just now"
        delta < HOUR_MS -> "${delta / MINUTE_MS}m ago"
        delta < DAY_MS -> "${delta / HOUR_MS}h ago"
        delta < WEEK_MS -> "${delta / DAY_MS}d ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(this))
    }
}

/** Trims a long string for use as an auto-generated note title. */
fun String.toAutoTitle(maxWords: Int = 8, maxChars: Int = 60): String {
    val firstLine = trim().lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    if (firstLine.isBlank()) return "Untitled note"
    val words = firstLine.split(Regex("\\s+")).take(maxWords).joinToString(" ")
    return if (words.length <= maxChars) words else words.take(maxChars).trimEnd() + "…"
}

/** Returns true if [permission] is granted at runtime. */
fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/** True on devices that can draw overlays (or older than API 23). */
fun Context.canDrawOverlays(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

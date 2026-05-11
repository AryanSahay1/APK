package com.nexos.ai.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = (now - this).coerceAtLeast(0)
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(this))
    }
}

fun Long.toFormattedDateTime(): String =
    SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(this))

fun String.titleCaseSafe(maxWords: Int = 8): String {
    val trimmed = trim().ifBlank { return "Untitled note" }
    val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return "Untitled note"
    return words.take(maxWords).joinToString(" ")
}

fun Context.canDrawOverlays(): Boolean = Settings.canDrawOverlays(this)

fun Context.openOverlaySettings() {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:$packageName")
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

inline fun Modifier.thenIf(condition: Boolean, builder: Modifier.() -> Modifier): Modifier =
    if (condition) then(builder(Modifier)) else this

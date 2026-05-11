package com.nexos.ai.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
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

/**
 * Whether the system is currently ignoring battery optimisations for this package. Required for
 * the floating button to survive aggressive OEM background restrictions (SKILL-1.md §8).
 */
fun Context.isBatteryOptimizationIgnored(): Boolean {
    val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
    return pm.isIgnoringBatteryOptimizations(packageName)
}

/**
 * Open the system dialog asking the user to whitelist NexOS. Requires
 * REQUEST_IGNORE_BATTERY_OPTIMIZATIONS in the manifest (already declared).
 */
fun Activity.requestBatteryOptimizationExempt() {
    if (isBatteryOptimizationIgnored()) return
    val intent = Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:$packageName")
    )
    runCatching { startActivity(intent) }
}

inline fun Modifier.thenIf(condition: Boolean, builder: Modifier.() -> Modifier): Modifier =
    if (condition) then(builder(Modifier)) else this

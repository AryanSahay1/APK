package com.nexos.ai.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.nexos.ai.data.local.entity.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over AlarmManager. Schedules exact alarms when the user has granted the
 * exact-alarm permission, otherwise falls back to setAndAllowWhileIdle, which is reliable
 * within the Doze whitelist window (typically within ±9 minutes for unfocused apps, exact for
 * apps the user has opened recently).
 *
 * Architecture compliance:
 * - All PendingIntents use FLAG_IMMUTABLE (API 31+ requirement, mandatory)
 * - Hilt-scoped @Singleton
 * - No GlobalScope; synchronous calls only
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val tag = "NexOS/AlarmScheduler"
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled || alarm.isFired) return
        val pendingIntent = buildPendingIntent(alarm) ?: run {
            Log.w(tag, "Failed to build PendingIntent for alarm ${alarm.id}")
            return
        }
        val triggerAt = alarm.triggerAt
        val canScheduleExact = canScheduleExactAlarms()
        try {
            if (canScheduleExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            }
            Log.i(tag, "Scheduled alarm ${alarm.id} at $triggerAt (exact=$canScheduleExact)")
        } catch (se: SecurityException) {
            // The user revoked the exact-alarm permission after we resolved canScheduleExact.
            // Fall back to the inexact path so the alarm still fires (within Doze tolerance).
            Log.w(tag, "Exact alarm denied — falling back to setAndAllowWhileIdle: ${se.message}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }

    fun cancel(alarm: Alarm) {
        val pendingIntent = buildPendingIntent(alarm, flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.i(tag, "Cancelled alarm ${alarm.id}")
        }
    }

    fun canScheduleExactAlarms(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    private fun buildPendingIntent(
        alarm: Alarm,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    ): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, alarm.title)
        }
        return PendingIntent.getBroadcast(context, alarm.pendingIntentRequestCode, intent, flags)
    }
}

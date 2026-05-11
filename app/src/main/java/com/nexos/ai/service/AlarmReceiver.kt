package com.nexos.ai.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nexos.ai.MainActivity
import com.nexos.ai.R
import com.nexos.ai.data.repository.AlarmRepository
import com.nexos.ai.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fired by AlarmManager when a scheduled reminder is due. Posts a high-priority notification
 * with the user's title and an open-app action. Also marks the alarm as fired in the DB so the
 * UI shows it greyed out.
 *
 * Architecture compliance:
 * - @AndroidEntryPoint for Hilt field injection
 * - All Room writes happen on Dispatchers.IO via the repository
 * - PendingIntent uses FLAG_IMMUTABLE (API 31+ requirement)
 * - Notification channel is created in NexosApp.onCreate (idempotent)
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmRepository: AlarmRepository

    private val tag = "NexOS/AlarmReceiver"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Reminder" }
        Log.i(tag, "Alarm fired: id=$alarmId title=$title")

        // Briefly acquire a wake lock so the notification posts even if the device just dozed.
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = pm?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$PACKAGE_NAME:AlarmReceiver"
        )?.apply { acquire(10_000L) }

        try {
            postNotification(context, alarmId, title)
            if (alarmId > 0) {
                val pending = goAsync()
                scope.launch {
                    try {
                        alarmRepository.markFired(alarmId)
                    } catch (t: Throwable) {
                        Log.e(tag, "Failed to mark alarm fired", t)
                    } finally {
                        pending.finish()
                    }
                }
            }
        } finally {
            runCatching { wakeLock?.release() }
        }
    }

    private fun postNotification(context: Context, alarmId: Long, title: String) {
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_NAV_ROUTE, "alarms")
        }
        val tap = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ALARMS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("NexOS reminder")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setAutoCancel(true)
            .setContentIntent(tap)
            .build()

        NotificationManagerCompat.from(context).notify(
            (Constants.NOTIF_ALARM_BASE + (alarmId.toInt() % 1000)),
            notification
        )
    }

    companion object {
        const val ACTION_FIRE = "com.nexos.ai.ACTION_ALARM_FIRE"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_TITLE = "alarm_title"
        private const val PACKAGE_NAME = "com.nexos.ai"
    }
}

package com.nexos.ai.util

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nexos.ai.MainActivity
import com.nexos.ai.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for system-level notifications fired by the orchestrator. Distinct from the persistent
 * foreground notifications managed inside each service.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun showNoteSaved(noteId: Long, title: String) {
        if (!hasPostPermission()) return
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_NOTE_ID, noteId)
            putExtra(Constants.EXTRA_NAV_ROUTE, "noteDetail")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_NOTES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Note saved")
            .setContentText(title.take(140))
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(
            (Constants.NOTIF_NOTE_SAVED_BASE + noteId.toInt() % 1000),
            notification
        )
    }

    fun showFailure(message: String) {
        if (!hasPostPermission()) return
        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_NOTES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("NexOS — couldn't save note")
            .setContentText(message.take(140))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(Constants.NOTIF_NOTE_SAVED_BASE - 1, notification)
    }

    private fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

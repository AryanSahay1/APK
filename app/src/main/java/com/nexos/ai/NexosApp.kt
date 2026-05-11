package com.nexos.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.nexos.ai.util.NexosChannels
import dagger.hilt.android.HiltAndroidApp

/**
 * NexOS Application root.
 *
 * Hilt entry-point. Creates notification channels eagerly so foreground services
 * can call [android.app.Service.startForeground] within the 5-second window
 * Android enforces on API 26+.
 */
@HiltAndroidApp
class NexosApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                NexosChannels.SERVICE_CHANNEL_ID,
                getString(R.string.notification_channel_service_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_service_desc)
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                NexosChannels.NOTES_CHANNEL_ID,
                getString(R.string.notification_channel_notes_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_notes_desc)
                setShowBadge(true)
            }
        )
    }
}

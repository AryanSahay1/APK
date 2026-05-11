package com.nexos.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nexos.ai.util.Constants
import dagger.hilt.android.HiltAndroidApp

/**
 * Application root. Creates notification channels on first launch and wires Hilt.
 *
 * Law 2 (NexOS): never crash silently — every long-running operation needs a visible channel.
 * Law 3: channels for the persistent foreground service use LOW importance to avoid sound/vibration.
 */
@HiltAndroidApp
class NexosApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return

        val service = NotificationChannel(
            Constants.CHANNEL_SERVICE,
            getString(R.string.channel_service_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_service_desc)
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }

        val notes = NotificationChannel(
            Constants.CHANNEL_NOTES,
            getString(R.string.channel_notes_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.channel_notes_desc)
            setShowBadge(true)
        }

        nm.createNotificationChannels(listOf(service, notes))
    }
}

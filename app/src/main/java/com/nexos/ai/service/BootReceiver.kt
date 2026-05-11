package com.nexos.ai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nexos.ai.data.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Re-arms every pending alarm after the device reboots. AlarmManager state is process-local and
 * does not survive a reboot, so we replay it from the Room table.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmRepository: AlarmRepository
    private val tag = "NexOS/BootReceiver"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        val pending = goAsync()
        scope.launch {
            try {
                alarmRepository.reschedulePending()
                Log.i(tag, "Rescheduled pending alarms after $action")
            } catch (t: Throwable) {
                Log.e(tag, "Failed to reschedule alarms", t)
            } finally {
                pending.finish()
            }
        }
    }
}

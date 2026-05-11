package com.nexos.ai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nexos.ai.orchestrator.NexosOrchestrator
import com.nexos.ai.util.NexosActions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Bridges floating-button broadcasts to the orchestrator. The receiver
 * itself is short-lived, so we hand off to a singleton scope owned by the
 * application graph.
 */
@AndroidEntryPoint
class NexosReceiver : BroadcastReceiver() {

    @Inject lateinit var orchestrator: NexosOrchestrator

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i(TAG, "received: $action")
        scope.launch {
            when (action) {
                NexosActions.ACTION_CAPTURE_SCREENSHOT -> orchestrator.handleScreenshotCapture()
                NexosActions.ACTION_START_VOICE -> {
                    /* Voice flow is launched from the in-app bottom sheet; the
                       broadcast surfaces the request and the orchestrator's
                       state machine pulls the bottom sheet open via Toasts/HUD. */
                }
            }
        }
    }

    private companion object {
        const val TAG = "NexOS/Receiver"
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

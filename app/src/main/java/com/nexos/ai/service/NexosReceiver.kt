package com.nexos.ai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nexos.ai.util.Constants
import com.nexos.ai.util.NexosOrchestrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Routes broadcasts from the FloatingButtonService into the orchestrator. Keeps the service
 * unaware of the orchestrator so the two stay decoupled.
 *
 * Architecture: services emit broadcasts, NexosReceiver invokes orchestrator methods,
 * orchestrator emits WorkflowState back through its SharedFlow.
 */
@AndroidEntryPoint
class NexosReceiver : BroadcastReceiver() {

    @Inject lateinit var orchestrator: NexosOrchestrator

    private val tag = "NexOS/Receiver"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i(tag, "Received: $action")
        when (action) {
            Constants.ACTION_CAPTURE_SCREENSHOT -> {
                // Forward to FloatingButtonService → which calls ScreenshotService.captureScreen()
                FloatingButtonService.requestScreenshot(context)
            }
            Constants.ACTION_START_VOICE -> {
                FloatingButtonService.requestVoice(context)
            }
            Constants.ACTION_OPEN_APP -> {
                FloatingButtonService.openApp(context)
            }
        }
    }
}

package com.nexos.ai.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-scoped bridge that lets non-Activity components (services, the
 * orchestrator) ask the foreground Activity to launch the MediaProjection
 * permission dialog.
 *
 * Holds only a function reference — no Activity context — so it cannot leak.
 */
@Singleton
class ScreenCaptureBridge @Inject constructor() {

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    @Volatile private var launcher: (() -> Unit)? = null

    fun setLauncher(block: () -> Unit) { launcher = block }
    fun clearLauncher() { launcher = null }

    /** Called from UI when the user wants a screenshot; routes through the Activity. */
    fun requestPermission() {
        launcher?.invoke()
    }

    fun onPermissionGranted() { _events.tryEmit(Event.Granted) }
    fun onPermissionDenied() { _events.tryEmit(Event.Denied) }

    sealed interface Event {
        data object Granted : Event
        data object Denied : Event
    }
}

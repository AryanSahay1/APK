package com.nexos.ai.service

import android.animation.ValueAnimator
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.nexos.ai.MainActivity
import com.nexos.ai.R
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.domain.model.WorkflowState
import com.nexos.ai.util.Constants
import com.nexos.ai.util.NexosOrchestrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

/**
 * Persistent floating overlay button. Implements:
 *  - Drag with WindowManager
 *  - Edge snap with spring-physics animation
 *  - Tap → screenshot capture
 *  - Long press → voice input
 *  - Double-tap → open app
 *
 * Communicates with NexosOrchestrator via injected reference and observes WorkflowState to
 * update its tint while a capture is in progress.
 */
@AndroidEntryPoint
class FloatingButtonService : Service() {

    @Inject lateinit var orchestrator: NexosOrchestrator
    @Inject lateinit var settingsRepository: SettingsRepository

    private val tag = "NexOS/FloatingButton"

    private var windowManager: WindowManager? = null
    private var rootView: View? = null
    private var iconView: ImageView? = null
    private val layoutParams by lazy {
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private var screenshotService: ScreenshotService? = null
    private val screenshotConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            screenshotService = (service as? ScreenshotService.LocalBinder)?.service
            Log.i(tag, "Bound to ScreenshotService")
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            screenshotService = null
        }
    }
    private var screenshotBound = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundCompat(WorkflowState.Idle)
        instance = this
        bindScreenshotService()
        observeWorkflow()
        attachOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_REQUEST_SCREENSHOT -> capture()
            ACTION_REQUEST_VOICE -> startVoice()
            ACTION_OPEN_APP -> openApp(this)
        }
        return START_STICKY
    }

    private fun startForegroundCompat(state: WorkflowState) {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = when (state) {
            WorkflowState.Idle -> getString(R.string.fg_floating)
            WorkflowState.Capturing -> getString(R.string.fg_capture)
            WorkflowState.ExtractingText -> getString(R.string.fg_processing)
            WorkflowState.AiProcessing -> getString(R.string.fg_ai)
            WorkflowState.Saving -> getString(R.string.fg_saving)
            is WorkflowState.Done -> "Saved: ${state.note.title.take(40)}"
            is WorkflowState.Failed -> "Failed: ${state.error.take(40)}"
        }
        val notification: Notification = NotificationCompat.Builder(this, Constants.CHANNEL_SERVICE)
            .setContentTitle(getString(R.string.fg_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(Constants.NOTIF_FLOATING, notification)
    }

    private fun observeWorkflow() {
        stateJob?.cancel()
        stateJob = scope.launch {
            orchestrator.state.collect { state ->
                startForegroundCompat(state)
                updateButtonForState(state)
            }
        }
    }

    private fun updateButtonForState(state: WorkflowState) {
        val view = iconView ?: return
        when (state) {
            WorkflowState.Idle -> view.alpha = 1f
            is WorkflowState.Done -> {
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(120).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
                }.start()
            }
            is WorkflowState.Failed -> {
                view.animate().translationX(-12f).setDuration(60).withEndAction {
                    view.animate().translationX(12f).setDuration(60).withEndAction {
                        view.animate().translationX(0f).setDuration(60).start()
                    }.start()
                }.start()
            }
            else -> view.alpha = 0.7f
        }
    }

    private fun attachOverlay() {
        if (rootView != null) return
        val inflater = LayoutInflater.from(this)
        val container = inflater.inflate(R.layout.view_floating_button, null, false)
        val icon = container.findViewById<ImageView>(R.id.floating_icon)

        val (screenW, screenH) = currentScreenSize()

        scope.launch {
            val side = settingsRepository.floatingButtonSide.first()
            val yFraction = settingsRepository.floatingButtonY.first()
            val size = (56 * Resources.getSystem().displayMetrics.density).toInt()
            layoutParams.x = if (side == "left") 16 else screenW - size - 16
            layoutParams.y = (screenH * yFraction).toInt()
            runCatching { windowManager?.updateViewLayout(container, layoutParams) }
        }

        attachDragBehavior(icon, container, screenW, screenH)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            windowManager?.addView(container, layoutParams)
            rootView = container
            iconView = icon
        } catch (t: Throwable) {
            Log.e(tag, "Failed to add overlay (overlay permission?)", t)
            stopSelf()
        }
    }

    private fun attachDragBehavior(icon: ImageView, container: View, screenW: Int, screenH: Int) {
        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var downTime = 0L
        var moved = false
        var longPressFired = false

        val longPressRunnable = Runnable {
            longPressFired = true
            icon.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            startVoice()
        }

        icon.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    downTime = System.currentTimeMillis()
                    moved = false
                    longPressFired = false
                    v.postDelayed(longPressRunnable, 500L)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - touchX
                    val dy = event.rawY - touchY
                    if (!moved && (abs(dx) > 12f || abs(dy) > 12f)) {
                        moved = true
                        v.removeCallbacks(longPressRunnable)
                    }
                    if (moved) {
                        layoutParams.x = (initialX + dx).toInt().coerceIn(0, screenW - v.width)
                        layoutParams.y = (initialY + dy).toInt().coerceIn(0, screenH - v.height)
                        runCatching { windowManager?.updateViewLayout(container, layoutParams) }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.removeCallbacks(longPressRunnable)
                    if (!moved && !longPressFired && (System.currentTimeMillis() - downTime) < 350L) {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        capture()
                    } else if (moved) {
                        snapToEdge(container, screenW)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToEdge(container: View, screenW: Int) {
        val midpoint = screenW / 2
        val targetX = if (layoutParams.x + container.width / 2 < midpoint) 16
        else screenW - container.width - 16
        val side = if (targetX < midpoint) "left" else "right"
        val anim = ValueAnimator.ofInt(layoutParams.x, targetX).apply {
            duration = 250
            interpolator = OvershootInterpolator(1.2f)
            addUpdateListener {
                layoutParams.x = it.animatedValue as Int
                runCatching { windowManager?.updateViewLayout(container, layoutParams) }
            }
        }
        anim.start()

        val (_, screenH) = currentScreenSize()
        val yFraction = (layoutParams.y.toFloat() / screenH.coerceAtLeast(1)).coerceIn(0f, 1f)
        scope.launch { settingsRepository.setFloatingButtonPosition(side, yFraction) }
    }

    /**
     * Returns the current screen size in pixels, using the Android 11+ API when available and
     * falling back to the deprecated Display#getSize on older devices.
     */
    private fun currentScreenSize(): Pair<Int, Int> {
        val wm = (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else {
            @Suppress("DEPRECATION")
            val display = wm.defaultDisplay
            val point = android.graphics.Point()
            @Suppress("DEPRECATION") display.getSize(point)
            point.x to point.y
        }
    }

    private fun bindScreenshotService() {
        if (screenshotBound) return
        val bound = bindService(
            Intent(this, ScreenshotService::class.java),
            screenshotConnection,
            Context.BIND_AUTO_CREATE
        )
        screenshotBound = bound
    }

    private fun capture() {
        scope.launch {
            val svc = screenshotService
            if (svc == null) {
                // Projection not yet granted — open MainActivity to ask
                val openIntent = Intent(this@FloatingButtonService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Constants.EXTRA_NAV_ROUTE, "requestCapture")
                }
                startActivity(openIntent)
                return@launch
            }
            val bitmap = svc.captureScreen()
            val imagePath = bitmap?.let { saveBitmapToCache(it) }.orEmpty()
            orchestrator.handleScreenshotCapture(bitmap, imagePath)
            // Recycle bitmap shortly after OCR completes (Layer 5: Image Cache Management).
            scope.launch {
                delay(8_000L)
                runCatching { bitmap?.recycle() }
            }
        }
    }

    private fun saveBitmapToCache(bitmap: android.graphics.Bitmap): String {
        return runCatching {
            val file = java.io.File(cacheDir, "nexos_capture_${System.currentTimeMillis()}.webp")
            file.outputStream().use { out ->
                @Suppress("DEPRECATION")
                bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 80, out)
            }
            file.absolutePath
        }.getOrDefault("")
    }

    private fun startVoice() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Constants.EXTRA_NAV_ROUTE, "voice")
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        instance = null
        stateJob?.cancel()
        if (screenshotBound) runCatching { unbindService(screenshotConnection) }
        screenshotBound = false
        rootView?.let { runCatching { windowManager?.removeView(it) } }
        rootView = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_REQUEST_SCREENSHOT = "${Constants.PACKAGE}.FLOAT_REQ_SCREENSHOT"
        const val ACTION_REQUEST_VOICE = "${Constants.PACKAGE}.FLOAT_REQ_VOICE"
        const val ACTION_OPEN_APP = "${Constants.PACKAGE}.FLOAT_OPEN_APP"

        @Volatile private var instance: FloatingButtonService? = null

        fun isRunning(): Boolean = instance != null

        fun requestScreenshot(context: Context) {
            val i = Intent(context, FloatingButtonService::class.java).apply { action = ACTION_REQUEST_SCREENSHOT }
            startCompat(context, i)
        }

        fun requestVoice(context: Context) {
            val i = Intent(context, FloatingButtonService::class.java).apply { action = ACTION_REQUEST_VOICE }
            startCompat(context, i)
        }

        fun openApp(context: Context) {
            val launch = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(launch)
        }

        fun startCompat(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}

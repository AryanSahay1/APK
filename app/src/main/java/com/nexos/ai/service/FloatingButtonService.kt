package com.nexos.ai.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.nexos.ai.MainActivity
import com.nexos.ai.R
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.util.NexosActions
import com.nexos.ai.util.NexosChannels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

/**
 * Foreground service that renders the always-on floating capture button via
 * [WindowManager] using `TYPE_APPLICATION_OVERLAY` (correct type from API 26).
 *
 * Interaction model:
 *  - Single tap   → broadcasts `ACTION_CAPTURE_SCREENSHOT`.
 *  - Long press   → broadcasts `ACTION_START_VOICE`.
 *  - Drag         → moves the button; on release it snaps to the nearest edge
 *                   and the position is persisted to DataStore.
 *
 * The view is added with `FLAG_NOT_FOCUSABLE` so it never steals keyboard
 * focus from the underlying app.
 */
@AndroidEntryPoint
class FloatingButtonService : Service() {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var bubbleView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private var positionJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundCompat()
        scope.launch { addBubble() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()
        if (bubbleView == null) scope.launch { addBubble() }
        return START_STICKY
    }

    override fun onDestroy() {
        removeBubble()
        positionJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun addBubble() {
        val bubble = ImageView(this).apply {
            setImageResource(R.drawable.ic_floating_button)
            contentDescription = getString(R.string.cd_floating_button)
            elevation = 12f
            isClickable = true
            isFocusable = false
        }
        bubbleView = bubble

        val savedSide = settingsRepository.floatingButtonSide.first()
        val savedY = settingsRepository.floatingButtonY.first()
        val metrics = DisplayMetrics().also { @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(it) }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = if (savedSide == "left") 0 else metrics.widthPixels - 56.dp(metrics)
            y = if (savedY >= 0) savedY else metrics.heightPixels / 3
        }
        bubbleParams = params

        try {
            windowManager.addView(bubble, params)
        } catch (e: Exception) {
            Log.e(TAG, "Could not add overlay (missing SYSTEM_ALERT_WINDOW?)", e)
            stopSelf()
            return
        }

        attachTouchListener(bubble, metrics)
    }

    private fun attachTouchListener(bubble: View, metrics: DisplayMetrics) {
        val touchSlop = 12.dp(metrics)
        val longPressThreshold = 480L
        val tapMaxDuration = 350L

        var startX = 0
        var startY = 0
        var rawStartX = 0f
        var rawStartY = 0f
        var downTime = 0L
        var didMove = false
        var didLongPress = false
        var longPressRunnable: Runnable? = null

        bubble.setOnTouchListener { _, event ->
            val params = bubbleParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    rawStartX = event.rawX
                    rawStartY = event.rawY
                    downTime = SystemClock.uptimeMillis()
                    didMove = false
                    didLongPress = false
                    longPressRunnable = Runnable {
                        if (!didMove) {
                            didLongPress = true
                            bubble.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                            sendAction(NexosActions.ACTION_START_VOICE)
                        }
                    }
                    bubble.postDelayed(longPressRunnable, longPressThreshold)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - rawStartX
                    val dy = event.rawY - rawStartY
                    if (!didMove && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        didMove = true
                        longPressRunnable?.let { bubble.removeCallbacks(it) }
                    }
                    if (didMove) {
                        params.x = (startX + dx.toInt()).coerceIn(0, metrics.widthPixels - bubble.width)
                        params.y = (startY + dy.toInt()).coerceIn(0, metrics.heightPixels - bubble.height)
                        try { windowManager.updateViewLayout(bubble, params) } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    longPressRunnable?.let { bubble.removeCallbacks(it) }
                    val duration = SystemClock.uptimeMillis() - downTime
                    when {
                        didMove -> snapToEdge(bubble, params, metrics)
                        didLongPress -> Unit
                        duration < tapMaxDuration -> {
                            bubble.performClick()
                            sendAction(NexosActions.ACTION_CAPTURE_SCREENSHOT)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToEdge(bubble: View, params: WindowManager.LayoutParams, metrics: DisplayMetrics) {
        val midScreen = metrics.widthPixels / 2
        val snapLeft = (params.x + bubble.width / 2) < midScreen
        params.x = if (snapLeft) 0 else metrics.widthPixels - bubble.width
        try { windowManager.updateViewLayout(bubble, params) } catch (_: Exception) {}

        positionJob?.cancel()
        positionJob = scope.launch {
            settingsRepository.setFloatingButtonSide(if (snapLeft) "left" else "right")
            settingsRepository.setFloatingButtonY(params.y)
        }
    }

    private fun sendAction(action: String) {
        val intent = Intent(action).setPackage(packageName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        sendBroadcast(intent)
    }

    private fun removeBubble() {
        val view = bubbleView ?: return
        try { windowManager.removeView(view) } catch (_: Exception) {}
        bubbleView = null
        bubbleParams = null
    }

    private fun startForegroundCompat() {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, NexosChannels.SERVICE_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_service_title))
            .setContentText(getString(R.string.notification_service_idle))
            .setSmallIcon(R.drawable.ic_nexos)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NexosChannels.FLOATING_NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NexosChannels.FLOATING_NOTIFICATION_ID, notification)
        }
    }

    private fun Int.dp(metrics: DisplayMetrics): Int = (this * metrics.density).toInt()

    companion object {
        private const val TAG = "NexOS/FloatingButtonService"

        fun start(context: Context) {
            val intent = Intent(context, FloatingButtonService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingButtonService::class.java))
        }
    }
}

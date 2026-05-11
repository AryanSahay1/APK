package com.nexos.ai.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.nexos.ai.MainActivity
import com.nexos.ai.R
import com.nexos.ai.util.NexosActions
import com.nexos.ai.util.NexosChannels
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Holds the [MediaProjection] for the lifetime of the user's session and
 * exposes a single [captureScreen] coroutine that returns a [Bitmap].
 *
 * Teardown order per Android docs and `SKILL.md §8`:
 *   ImageReader.close() → VirtualDisplay.release() → MediaProjection.stop()
 *
 * Each capture creates and destroys its own ImageReader + VirtualDisplay so
 * GPU memory is freed immediately; the [MediaProjection] itself is reused
 * across captures until the service is stopped.
 */
class ScreenshotService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        backgroundThread = HandlerThread("NexosScreenshot").apply { start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()

        val resultCode = intent?.getIntExtra(NexosActions.EXTRA_MEDIA_PROJECTION_RESULT_CODE, 0) ?: 0
        @Suppress("DEPRECATION") val data: Intent? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra(NexosActions.EXTRA_MEDIA_PROJECTION_DATA, Intent::class.java)
            } else {
                intent?.getParcelableExtra(NexosActions.EXTRA_MEDIA_PROJECTION_DATA)
            }
        if (resultCode != 0 && data != null && mediaProjection == null) {
            val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mpm.getMediaProjection(resultCode, data).apply {
                registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        Log.i(TAG, "MediaProjection stopped by system")
                        mediaProjection = null
                    }
                }, backgroundHandler)
            }
            Log.i(TAG, "MediaProjection ready")
        }

        return START_STICKY
    }

    /**
     * Captures one frame and returns its bitmap. Returns null if the projection
     * is unavailable (user revoked permission) or the system fails to deliver
     * a frame within ~2 seconds.
     */
    suspend fun captureScreen(): Bitmap? {
        val projection = mediaProjection ?: run {
            Log.w(TAG, "captureScreen() called without an active MediaProjection")
            return null
        }
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics().also { @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(it) }
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        var imageReader: ImageReader? = null
        var virtualDisplay: VirtualDisplay? = null

        return try {
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            virtualDisplay = projection.createVirtualDisplay(
                "NexosCapture",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                backgroundHandler
            )

            val bitmap = suspendCancellableCoroutine<Bitmap?> { cont ->
                val timeoutRunnable = Runnable {
                    if (cont.isActive) {
                        Log.w(TAG, "Screen capture timed out after ${CAPTURE_TIMEOUT_MS}ms")
                        cont.resume(null)
                    }
                }
                backgroundHandler?.postDelayed(timeoutRunnable, CAPTURE_TIMEOUT_MS)

                imageReader!!.setOnImageAvailableListener({ reader ->
                    var image: Image? = null
                    try {
                        image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        val bmp = Bitmap.createBitmap(
                            width + rowPadding / pixelStride,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        bmp.copyPixelsFromBuffer(buffer)

                        val cropped = if (rowPadding == 0) bmp else
                            Bitmap.createBitmap(bmp, 0, 0, width, height)
                        if (cropped !== bmp) bmp.recycle()

                        reader.setOnImageAvailableListener(null, null)
                        backgroundHandler?.removeCallbacks(timeoutRunnable)
                        if (cont.isActive) cont.resume(cropped)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to read image", e)
                        if (cont.isActive) cont.resume(null)
                    } finally {
                        image?.close()
                    }
                }, backgroundHandler)
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "captureScreen failed", e)
            null
        } finally {
            try { imageReader?.close() } catch (_: Exception) {}
            try { virtualDisplay?.release() } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        try { mediaProjection?.stop() } catch (_: Exception) {}
        mediaProjection = null
        backgroundThread?.quitSafely()
        backgroundThread = null
        backgroundHandler = null
        if (instance === this) instance = null
        super.onDestroy()
    }

    private fun startForegroundCompat() {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, NexosChannels.SERVICE_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_service_title))
            .setContentText(getString(R.string.notification_service_capturing))
            .setSmallIcon(R.drawable.ic_nexos)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NexosChannels.SCREENSHOT_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NexosChannels.SCREENSHOT_NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val TAG = "NexOS/ScreenshotService"
        private const val CAPTURE_TIMEOUT_MS = 2_500L

        @Volatile
        @JvmStatic
        var instance: ScreenshotService? = null
            private set

        fun isReady(): Boolean = instance?.mediaProjection != null
    }
}

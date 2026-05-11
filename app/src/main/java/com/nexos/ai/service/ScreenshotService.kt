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
import com.nexos.ai.util.Constants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Captures a single screen frame via MediaProjection and returns the Bitmap.
 *
 * Architecture (Layer 1, Channel A):
 * - Lives as a foreground service with foregroundServiceType="mediaProjection"
 * - Holds the [MediaProjection] reference once granted
 * - Each [captureScreen] call creates fresh ImageReader + VirtualDisplay, captures one frame,
 *   and immediately tears them down to release GPU memory
 *
 * Critical Android rules followed:
 * - startForeground() called within onStartCommand
 * - MediaProjection.Callback registered before getMediaProjection (Android 14 requirement)
 * - VirtualDisplay released before MediaProjection.stop()
 * - Bitmap copied to ARGB_8888 so caller can use it after Image is closed
 */
class ScreenshotService : Service() {

    private val tag = "NexOS/ScreenshotService"

    private var mediaProjection: MediaProjection? = null
    private var resultCode: Int = 0
    private var resultData: Intent? = null

    private val handlerThread by lazy {
        HandlerThread("nexos-screenshot").also { it.start() }
    }
    private val handler by lazy { Handler(handlerThread.looper) }

    private val binder = LocalBinder()
    inner class LocalBinder : android.os.Binder() {
        val service: ScreenshotService get() = this@ScreenshotService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()
        when (intent?.action) {
            Constants.SVC_ACTION_START_PROJECTION -> {
                resultCode = intent.getIntExtra(Constants.EXTRA_RESULT_CODE, 0)
                @Suppress("DEPRECATION")
                resultData = intent.getParcelableExtra(Constants.EXTRA_RESULT_DATA)
                Log.i(tag, "Projection started: resultCode=$resultCode")
            }
            Constants.SVC_ACTION_STOP_PROJECTION -> {
                releaseProjection()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundCompat() {
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, Constants.CHANNEL_SERVICE)
            .setContentTitle(getString(R.string.fg_title))
            .setContentText(getString(R.string.fg_idle))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.NOTIF_SCREENSHOT,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(Constants.NOTIF_SCREENSHOT, notification)
        }
    }

    private fun ensureProjection(): MediaProjection? {
        val existing = mediaProjection
        if (existing != null) return existing
        val data = resultData ?: return null
        if (resultCode == 0) return null
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = manager.getMediaProjection(resultCode, data)
        projection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.i(tag, "MediaProjection stopped")
                mediaProjection = null
            }
        }, handler)
        mediaProjection = projection
        return projection
    }

    suspend fun captureScreen(): Bitmap? {
        val projection = ensureProjection() ?: run {
            Log.w(tag, "captureScreen called without projection grant")
            return null
        }
        val (width, height, density) = currentDisplayMetrics()

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        val deferred = CompletableDeferred<Bitmap?>()
        var virtualDisplay: VirtualDisplay?

        reader.setOnImageAvailableListener({ r ->
            var img: Image? = null
            try {
                img = r.acquireLatestImage()
                if (img != null && !deferred.isCompleted) {
                    val bitmap = imageToBitmap(img, width, height)
                    deferred.complete(bitmap)
                }
            } catch (t: Throwable) {
                Log.e(tag, "Image read failed", t)
                if (!deferred.isCompleted) deferred.complete(null)
            } finally {
                img?.close()
            }
        }, handler)

        try {
            virtualDisplay = projection.createVirtualDisplay(
                "nexos-vdisplay",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                handler
            )
        } catch (t: Throwable) {
            Log.e(tag, "Failed to create VirtualDisplay", t)
            reader.close()
            return null
        }

        val bitmap = withTimeoutOrNull(5_000L) { deferred.await() }

        runCatching { virtualDisplay?.release() }
        runCatching { reader.close() }
        return bitmap
    }

    /**
     * Real display metrics across API levels. Returns (width, height, densityDpi).
     */
    private fun currentDisplayMetrics(): Triple<Int, Int, Int> {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            val density = resources.configuration.densityDpi
            Triple(bounds.width(), bounds.height(), density)
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            Triple(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
        }
    }

    private fun imageToBitmap(image: Image, width: Int, height: Int): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val bmp = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bmp.copyPixelsFromBuffer(buffer)
        return if (rowPadding == 0) bmp
        else Bitmap.createBitmap(bmp, 0, 0, width, height).also { bmp.recycle() }
    }

    private fun releaseProjection() {
        runCatching { mediaProjection?.stop() }
        mediaProjection = null
    }

    override fun onDestroy() {
        releaseProjection()
        runCatching { handlerThread.quitSafely() }
        super.onDestroy()
    }

    companion object {
        fun startIntent(context: Context, resultCode: Int, data: Intent): Intent =
            Intent(context, ScreenshotService::class.java).apply {
                action = Constants.SVC_ACTION_START_PROJECTION
                putExtra(Constants.EXTRA_RESULT_CODE, resultCode)
                putExtra(Constants.EXTRA_RESULT_DATA, data)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, ScreenshotService::class.java).apply {
                action = Constants.SVC_ACTION_STOP_PROJECTION
            }
    }
}

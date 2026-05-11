package com.nexos.ai.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records short voice memos to the app's filesDir, returning the absolute path + duration.
 *
 * One recorder instance + one currently-active file per [AudioRecorder]. Calling [start]
 * while a recording is in progress is a no-op (logged); [stop] is idempotent.
 *
 * MediaRecorder is preferred over AudioRecord for this use case because:
 *  - the user wants compressed playback-ready files (AAC), not raw PCM
 *  - encoder is hardware-accelerated on every modern device
 *  - the resulting `.m4a` plays back in any media player without extra work
 */
@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val tag = "NexOS/AudioRecorder"
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startedAt: Long = 0L

    val isRecording: Boolean get() = recorder != null

    @SuppressLint("UnsafeOptInUsageError")
    fun start(): File? {
        if (recorder != null) {
            Log.w(tag, "start() called while already recording")
            return currentFile
        }
        val outputDir = File(context.filesDir, "voice_memos").also { it.mkdirs() }
        val file = File(outputDir, "memo_${System.currentTimeMillis()}.m4a")
        currentFile = file

        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }
        rec.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
        }
        return try {
            rec.prepare()
            rec.start()
            startedAt = System.currentTimeMillis()
            recorder = rec
            file
        } catch (t: Throwable) {
            Log.e(tag, "Failed to start recorder: ${t.message}", t)
            runCatching { rec.release() }
            recorder = null
            currentFile = null
            null
        }
    }

    /**
     * Stop the active recording and return (file, durationMs). Returns null if there was no
     * active recording or the encoder failed to flush.
     */
    fun stop(): Pair<File, Long>? {
        val rec = recorder ?: return null
        val file = currentFile ?: return null
        val durationMs = (System.currentTimeMillis() - startedAt).coerceAtLeast(0L)
        return try {
            rec.stop()
            rec.release()
            recorder = null
            currentFile = null
            startedAt = 0L
            if (file.exists() && file.length() > 0) file to durationMs else null
        } catch (t: Throwable) {
            Log.e(tag, "Failed to stop recorder: ${t.message}", t)
            runCatching { rec.release() }
            recorder = null
            currentFile = null
            null
        }
    }

    /** Abort without producing a usable file. Used when the user cancels mid-record. */
    fun cancel() {
        val rec = recorder ?: return
        val file = currentFile
        runCatching { rec.stop() }
        runCatching { rec.release() }
        recorder = null
        currentFile = null
        startedAt = 0L
        file?.let { runCatching { it.delete() } }
    }
}

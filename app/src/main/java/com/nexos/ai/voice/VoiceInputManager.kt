package com.nexos.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.nexos.ai.domain.model.VoiceState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live transcription via Android's built-in [SpeechRecognizer].
 *
 * Critical: SpeechRecognizer must be created and destroyed on the MAIN thread.
 * All [SpeechRecognizer] interactions in this class are scheduled on a main-thread Handler.
 */
@Singleton
class VoiceInputManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val tag = "NexOS/Voice"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun listen(): Flow<VoiceState> = callbackFlow {
        if (!isAvailable()) {
            trySend(VoiceState.Error("Speech recognition not available on this device"))
            close()
            return@callbackFlow
        }

        var recognizer: SpeechRecognizer? = null
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(VoiceState.Listening)
            }
            override fun onBeginningOfSpeech() {
                trySend(VoiceState.Listening)
            }
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onError(error: Int) {
                trySend(VoiceState.Error(errorMessage(error)))
                close()
            }
            override fun onResults(results: Bundle?) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val final = list?.firstOrNull().orEmpty()
                if (final.isBlank()) {
                    trySend(VoiceState.Error("No speech detected"))
                } else {
                    trySend(VoiceState.Result(final))
                }
                close()
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = list?.firstOrNull().orEmpty()
                if (partial.isNotBlank()) trySend(VoiceState.Partial(partial))
            }
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }

        mainHandler.post {
            try {
                recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(listener)
                    startListening(buildIntent())
                }
            } catch (t: Throwable) {
                Log.e(tag, "Failed to create SpeechRecognizer", t)
                trySend(VoiceState.Error("Speech recognizer failed: ${t.message}"))
                close()
            }
        }

        awaitClose {
            mainHandler.post {
                runCatching { recognizer?.stopListening() }
                runCatching { recognizer?.destroy() }
            }
        }
    }

    fun stopListening() {
        // Caller closes the flow; nothing to do here.
    }

    private fun buildIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

    private fun errorMessage(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Recognizer client error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Recognition error ($code)"
    }
}

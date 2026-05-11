package com.nexos.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Android's built-in [SpeechRecognizer].
 *
 * Crucial Android rule (SKILL.md §8): SpeechRecognizer instances MUST be
 * created and destroyed on the main thread. Every public entry point on
 * this class is a `suspend` that hops onto Dispatchers.Main internally so
 * callers don't have to remember it.
 */
@Singleton
class VoiceInputManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    @Volatile private var recognizer: SpeechRecognizer? = null
    @Volatile private var lastPartial: String = ""

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    suspend fun start() = withContext(Dispatchers.Main.immediate) {
        if (!isAvailable()) {
            _state.value = VoiceState.Error("Speech recognition not available on this device")
            return@withContext
        }
        stopInternal()
        lastPartial = ""

        val sr = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = sr
        sr.setRecognitionListener(listener)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        _state.value = VoiceState.Listening
        sr.startListening(intent)
    }

    suspend fun stop() = withContext(Dispatchers.Main.immediate) {
        recognizer?.stopListening()
    }

    suspend fun cancel() = withContext(Dispatchers.Main.immediate) {
        stopInternal()
        _state.value = VoiceState.Idle
    }

    fun reset() {
        _state.value = VoiceState.Idle
    }

    private fun stopInternal() {
        try { recognizer?.cancel() } catch (_: Exception) {}
        try { recognizer?.destroy() } catch (_: Exception) {}
        recognizer = null
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = VoiceState.Listening
        }

        override fun onBeginningOfSpeech() {
            _state.value = VoiceState.Partial("", amplitude = 0f)
        }

        override fun onRmsChanged(rmsdB: Float) {
            val normalised = ((rmsdB + 2f) / 10f).coerceIn(0f, 1f)
            _state.value = VoiceState.Partial(lastPartial, amplitude = normalised)
        }

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            _state.value = VoiceState.Partial(lastPartial, amplitude = 0f)
        }

        override fun onError(error: Int) {
            val message = errorText(error)
            Log.w(TAG, "SpeechRecognizer error: $message ($error)")
            stopInternal()
            if (lastPartial.isNotBlank() && error == SpeechRecognizer.ERROR_NO_MATCH) {
                _state.value = VoiceState.Result(lastPartial)
            } else {
                _state.value = VoiceState.Error(message)
            }
        }

        override fun onResults(results: Bundle?) {
            val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = texts?.firstOrNull()?.trim().orEmpty().ifBlank { lastPartial }
            stopInternal()
            _state.value = if (text.isNotBlank()) VoiceState.Result(text)
            else VoiceState.Error("No speech detected")
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            if (partial.isNotBlank()) {
                lastPartial = partial
                _state.value = VoiceState.Partial(partial, amplitude = 0f)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun errorText(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission missing"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognised"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recogniser busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Silence — try again"
        else -> "Unknown speech recognition error ($code)"
    }

    private companion object {
        const val TAG = "NexOS/VoiceInputManager"
    }
}

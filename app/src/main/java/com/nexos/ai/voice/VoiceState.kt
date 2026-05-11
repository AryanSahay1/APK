package com.nexos.ai.voice

/**
 * Coarse-grained states emitted by [VoiceInputManager] for the
 * VoiceInputBottomSheet to render.
 */
sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data class Partial(val text: String, val amplitude: Float = 0f) : VoiceState
    data class Result(val text: String) : VoiceState
    data class Error(val message: String) : VoiceState
}

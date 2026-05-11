package com.nexos.ai.domain.model

import com.nexos.ai.data.local.entity.Note

/**
 * Single source of truth for what the orchestrator is currently doing.
 * The floating button service observes this stream and updates its visual state.
 *
 * Law 2 (NexOS): every state is explicit — Idle, in-progress steps, terminal Done or Failed.
 */
sealed class WorkflowState {
    data object Idle : WorkflowState()
    data object Capturing : WorkflowState()
    data object ExtractingText : WorkflowState()
    data object AiProcessing : WorkflowState()
    data object Saving : WorkflowState()
    data class Done(val note: Note) : WorkflowState()
    data class Failed(val error: String, val step: String) : WorkflowState()
}

/**
 * Voice transcription state emitted by [com.nexos.ai.voice.VoiceInputManager].
 */
sealed class VoiceState {
    data object Idle : VoiceState()
    data object Listening : VoiceState()
    data class Partial(val text: String) : VoiceState()
    data class Result(val text: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

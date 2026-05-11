package com.nexos.ai.domain.model

/**
 * Coarse-grained states emitted by [com.nexos.ai.orchestrator.NexosOrchestrator]
 * for the floating button service, the in-app HUD, and the notification
 * builder to render.
 *
 * Law 2: every operation MUST emit a state. Silent failure is forbidden.
 */
sealed interface WorkflowState {
    data object Idle : WorkflowState
    data object Capturing : WorkflowState
    data object ExtractingText : WorkflowState
    data object AiProcessing : WorkflowState
    data object Saving : WorkflowState
    data class Done(val note: Note) : WorkflowState
    data class Failed(val error: String, val step: String) : WorkflowState
}

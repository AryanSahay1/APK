package com.nexos.ai.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-scoped publisher used by the orchestrator and deep-link receivers
 * to ask the foreground Activity to navigate to a saved note.
 */
@Singleton
class NavBridge @Inject constructor() {

    private val _pendingDeepLinkNoteId = MutableStateFlow<Long?>(null)
    val pendingDeepLinkNoteId: StateFlow<Long?> = _pendingDeepLinkNoteId.asStateFlow()

    fun publishDeepLink(noteId: Long) {
        _pendingDeepLinkNoteId.value = noteId
    }

    fun consume() {
        _pendingDeepLinkNoteId.value = null
    }
}

package com.nexos.ai

import androidx.lifecycle.ViewModel
import com.nexos.ai.util.NavBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin ViewModel that exposes the application-scoped [NavBridge] to the
 * Compose nav host so it can react to deep-link requests with lifecycle safety.
 */
@HiltViewModel
class NavBridgeViewModel @Inject constructor(
    private val navBridge: NavBridge
) : ViewModel() {
    val pendingDeepLinkNoteId: StateFlow<Long?> = navBridge.pendingDeepLinkNoteId
    fun consumePendingDeepLink() = navBridge.consume()
}

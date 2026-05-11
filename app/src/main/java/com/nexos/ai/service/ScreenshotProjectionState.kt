package com.nexos.ai.service

/**
 * Process-scoped flag exposing whether the user has currently granted the MediaProjection
 * permission. Set by MainActivity once `projectionLauncher` returns RESULT_OK; cleared if the
 * ScreenshotService loses its projection token.
 *
 * Why a static flag rather than a Hilt-scoped state holder?
 *   The capture quick action on the Notes tab is a pure Compose UI surface (no ViewModel
 *   would justify itself just for "do we have projection permission yet?"). A process-scoped
 *   AtomicBoolean is the simplest correct primitive and survives the same process lifetime
 *   that the underlying MediaProjection token does.
 */
object ScreenshotProjectionState {
    @Volatile var isGranted: Boolean = false
}

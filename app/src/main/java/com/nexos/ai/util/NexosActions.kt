package com.nexos.ai.util

/**
 * Intent action and extra constants used to bridge floating button taps,
 * BroadcastReceivers, and the orchestrator.
 */
object NexosActions {
    const val ACTION_CAPTURE_SCREENSHOT = "com.nexos.ACTION_CAPTURE_SCREENSHOT"
    const val ACTION_START_VOICE = "com.nexos.ACTION_START_VOICE"

    const val ACTION_STATE_BROADCAST = "com.nexos.ACTION_STATE_BROADCAST"
    const val EXTRA_STATE_NAME = "extra_state_name"
    const val EXTRA_STATE_MESSAGE = "extra_state_message"

    const val EXTRA_MEDIA_PROJECTION_RESULT_CODE = "extra_mp_result_code"
    const val EXTRA_MEDIA_PROJECTION_DATA = "extra_mp_data"

    const val EXTRA_NOTE_ID = "extra_note_id"
}

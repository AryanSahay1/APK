package com.nexos.ai.util

/**
 * App-wide constants. Keep all magic strings here so other modules cannot drift.
 */
object Constants {

    const val PACKAGE = "com.nexos.ai"

    // Notification channels
    const val CHANNEL_SERVICE = "nexos_service"
    const val CHANNEL_NOTES = "nexos_notes"
    const val CHANNEL_ALARMS = "nexos_alarms"

    // Notification ids
    const val NOTIF_FLOATING = 1001
    const val NOTIF_SCREENSHOT = 1002
    const val NOTIF_NOTE_SAVED_BASE = 2000
    const val NOTIF_ALARM_BASE = 3000

    // Broadcast actions (routed to NexosReceiver)
    const val ACTION_CAPTURE_SCREENSHOT = "$PACKAGE.ACTION_CAPTURE_SCREENSHOT"
    const val ACTION_START_VOICE = "$PACKAGE.ACTION_START_VOICE"
    const val ACTION_OPEN_APP = "$PACKAGE.ACTION_OPEN_APP"

    // Service intent extras (ScreenshotService)
    const val EXTRA_RESULT_CODE = "extra_result_code"
    const val EXTRA_RESULT_DATA = "extra_result_data"
    const val SVC_ACTION_START_PROJECTION = "$PACKAGE.SVC_START_PROJECTION"
    const val SVC_ACTION_STOP_PROJECTION = "$PACKAGE.SVC_STOP_PROJECTION"

    // Deep link extras
    const val EXTRA_NOTE_ID = "extra_note_id"
    const val EXTRA_NAV_ROUTE = "extra_nav_route"

    // Source types
    const val SOURCE_SCREENSHOT = "screenshot"
    const val SOURCE_VOICE = "voice"
    const val SOURCE_MANUAL = "manual"
    const val SOURCE_SHARED_TEXT = "shared"
    const val SOURCE_NEWS = "news"
    const val SOURCE_WEATHER = "weather"
    const val SOURCE_MAPS = "maps"

    // Provider keys
    const val PROVIDER_NONE = "none"
    const val PROVIDER_OPENAI = "openai"
    const val PROVIDER_GEMINI = "gemini"
    const val PROVIDER_ANTHROPIC = "anthropic"
    const val PROVIDER_GROQ = "groq"

    // Non-AI integrations (their keys live in the same SecureStorage)
    const val PROVIDER_NEWS_API = "newsapi"

    // Misc
    const val AI_REQUEST_TIMEOUT_MS = 30_000L
    const val OCR_MAX_BITMAP_DIMENSION = 2048
}

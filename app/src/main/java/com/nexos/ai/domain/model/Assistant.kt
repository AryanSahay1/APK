package com.nexos.ai.domain.model

import androidx.compose.runtime.Immutable

/**
 * Where the panda assistant is being invoked from. Determines the opening prompt the panda
 * generates, the icon shown on its display, and which specialised handlers (alarms / email /
 * food / weather / news / address) get prioritised in suggestion chips.
 */
enum class AssistantContext(val title: String, val opener: String) {
    Default(
        title = "Hi, I'm Panda",
        opener = "I can chat, set alarms, draft emails, parse Google Maps / WhatsApp location links, suggest food based on your mood, and read you the weather or the news."
    ),
    Weather(
        title = "Weather panda",
        opener = "Ask me about today's weather, or the week ahead. I can also save a snapshot as a note."
    ),
    News(
        title = "News panda",
        opener = "I can read you the top headlines. Tell me a topic or just say 'top news'."
    ),
    Email(
        title = "Mail panda",
        opener = "Tell me who to email and what to say — I'll draft it, you tap Send. I can also paraphrase a draft you paste."
    ),
    Alarm(
        title = "Alarm panda",
        opener = "Tell me when to remind you: 'remind me at 8am tomorrow' or 'in 30 minutes'."
    ),
    Map(
        title = "Map panda",
        opener = "Paste a Google Maps or WhatsApp location link and I'll turn it into a clean written address."
    ),
    Food(
        title = "Food panda",
        opener = "How are you feeling? Tell me your mood and I'll suggest what to order on Swiggy."
    ),
    Notes(
        title = "Notes panda",
        opener = "Want me to summarise something, or help write a note? Paste the text and I'll work on it."
    )
}

/** A single turn in the assistant chat — either the user or the panda. */
@Immutable
data class ChatMessage(
    val id: Long,
    val role: ChatRole,
    val text: String,
    val attachment: ChatAttachment? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/** What the panda is showing on its tablet right now. Drives the per-context icon. */
enum class ChatRole { User, Panda, System }

/**
 * Optional rich attachment on a panda message. Each variant carries the data the UI needs to
 * render an action button (e.g. "Open Swiggy", "Set alarm", "Send email") without the
 * ViewModel having to expose lambdas.
 */
sealed class ChatAttachment {
    /** Mood-driven Swiggy hand-off. */
    data class FoodSuggestion(val cuisine: String, val rationale: String) : ChatAttachment()

    /** Alarm ready to be scheduled. */
    data class AlarmPending(val title: String, val triggerAtMillis: Long, val phrase: String) : ChatAttachment()

    /** Email draft ready to be sent via Gmail composer. */
    data class EmailDraft(val to: String, val subject: String, val body: String) : ChatAttachment()

    /** Parsed address from a Maps/WhatsApp link. */
    data class ParsedAddress(val address: String, val latitude: Double, val longitude: Double) : ChatAttachment()

    /** Weather snapshot summary inline. */
    data class WeatherSummary(
        val locationLabel: String,
        val tempC: Double,
        val condition: WeatherCondition
    ) : ChatAttachment()

    /** News headline list. */
    data class NewsHeadlines(val articles: List<NewsHeadline>) : ChatAttachment()
}

data class NewsHeadline(val title: String, val source: String, val url: String)

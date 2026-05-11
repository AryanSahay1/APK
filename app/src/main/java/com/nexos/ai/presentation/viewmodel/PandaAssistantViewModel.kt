package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.ai.AIRouter
import com.nexos.ai.data.local.entity.Alarm
import com.nexos.ai.data.repository.AlarmRepository
import com.nexos.ai.data.repository.NewsRepository
import com.nexos.ai.data.repository.SettingsRepository
import com.nexos.ai.data.repository.WeatherRepository
import com.nexos.ai.domain.model.AssistantContext
import com.nexos.ai.domain.model.ChatAttachment
import com.nexos.ai.domain.model.ChatMessage
import com.nexos.ai.domain.model.ChatRole
import com.nexos.ai.domain.model.NewsHeadline
import com.nexos.ai.domain.usecase.ParseLocationLinkUseCase
import com.nexos.ai.util.NaturalLanguageTimeParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val context: AssistantContext = AssistantContext.Default,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isTyping: Boolean = false,
    val isAiConfigured: Boolean = false,
    /** Live weather condition for the panda's tablet glyph when in Weather context. */
    val weatherCondition: com.nexos.ai.domain.model.WeatherCondition? = null
)

/**
 * Single ViewModel powering the panda assistant overlay. Maintains chat history, routes
 * input to the right handler based on the active [AssistantContext] AND to opportunistic
 * detectors (e.g. URL detection regardless of context), and surfaces rich attachments
 * (alarm to schedule, email draft to send, food suggestion to open) so the UI can render
 * action buttons.
 *
 * Architecture: the ViewModel never talks to the network directly; it delegates to existing
 * repositories ([WeatherRepository], [NewsRepository], [AlarmRepository]) and use cases
 * ([ParseLocationLinkUseCase]). The AI provider is read through [AIRouter] so any of the
 * four supported providers (OpenAI / Gemini / Anthropic / Groq) can answer.
 */
@HiltViewModel
class PandaAssistantViewModel @Inject constructor(
    private val aiRouter: AIRouter,
    private val parseLocationLink: ParseLocationLinkUseCase,
    private val alarmRepository: AlarmRepository,
    private val newsRepository: NewsRepository,
    private val weatherRepository: WeatherRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AssistantUiState())
    val state: StateFlow<AssistantUiState> = _state.asStateFlow()

    private var nextMessageId: Long = 1

    init {
        viewModelScope.launch {
            _state.update { it.copy(isAiConfigured = aiRouter.isAiEnabled()) }
        }
    }

    /** Called when the assistant sheet opens. Sets context + seeds the panda's opener. */
    fun openWith(context: AssistantContext) {
        if (_state.value.context == context && _state.value.messages.isNotEmpty()) return
        val opener = ChatMessage(
            id = nextMessageId++,
            role = ChatRole.Panda,
            text = "${context.opener}"
        )
        _state.update {
            it.copy(
                context = context,
                messages = listOf(opener),
                input = "",
                isTyping = false
            )
        }
        // For Weather context, pre-fetch so the tablet glyph is correct from frame 1.
        if (context == AssistantContext.Weather) preloadWeatherGlyph()
        if (context == AssistantContext.News) preloadTopHeadlinesIfAi()
    }

    fun onInputChange(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun send() {
        val raw = _state.value.input.trim()
        if (raw.isBlank()) return
        appendUser(raw)
        _state.update { it.copy(input = "", isTyping = true) }
        viewModelScope.launch { dispatch(raw) }
    }

    /**
     * Single dispatcher: opportunistic detectors first (a Maps URL anywhere in the message
     * always becomes an address-parse, regardless of context), then context-specific
     * defaults, then a plain AI chat as the catch-all.
     */
    private suspend fun dispatch(text: String) {
        // 1. Map / WhatsApp link — high-confidence, always honoured
        if (looksLikeMapsLink(text)) {
            handleAddressParse(text)
            return
        }

        // 2. Context-specific handlers
        when (_state.value.context) {
            AssistantContext.Alarm -> if (handleAlarm(text)) return
            AssistantContext.Email -> if (handleEmailDraft(text)) return
            AssistantContext.Food -> if (handleMoodToFood(text)) return
            AssistantContext.Weather -> if (handleWeatherQuestion(text)) return
            AssistantContext.News -> if (handleNewsQuestion(text)) return
            else -> Unit
        }

        // 3. Fall through to a plain AI chat with the appropriate persona
        chatViaAI(text)
    }

    // -------- Maps / WhatsApp link → address --------

    private fun looksLikeMapsLink(text: String): Boolean =
        Regex("(?i)(https?://[^\\s]*(google|goo\\.gl|whatsapp|wa\\.me)[^\\s]*|geo:[^\\s]+)").containsMatchIn(text)

    private suspend fun handleAddressParse(text: String) {
        parseLocationLink(text)
            .onSuccess { parsed ->
                appendPanda(
                    "From the ${parsed.sourceLabel} link, that's:\n\n${parsed.address}",
                    ChatAttachment.ParsedAddress(parsed.address, parsed.latitude, parsed.longitude)
                )
            }
            .onFailure { e ->
                val msg = when (e) {
                    is ParseLocationLinkUseCase.ParseException.NoUrlFound ->
                        "I don't see a Maps or WhatsApp location link in that message."
                    is ParseLocationLinkUseCase.ParseException.NoCoordinates ->
                        "I see the link but couldn't pull coordinates out of it. WhatsApp text-only shares (wa.me) don't include location."
                    else -> "Couldn't reach OpenStreetMap to resolve that. Try again in a minute."
                }
                appendPanda(msg)
            }
        finishTyping()
    }

    // -------- Alarm --------

    private suspend fun handleAlarm(text: String): Boolean {
        val parsed = NaturalLanguageTimeParser.parse(text) ?: return false
        appendPanda(
            "Reminder scheduled for ${java.text.SimpleDateFormat("EEE HH:mm", java.util.Locale.getDefault()).format(java.util.Date(parsed.triggerAtMillis))}: \"${parsed.title}\".",
            ChatAttachment.AlarmPending(parsed.title, parsed.triggerAtMillis, text)
        )
        alarmRepository.add(
            Alarm(
                title = parsed.title,
                rawRequest = text,
                triggerAt = parsed.triggerAtMillis,
                isEnabled = true
            )
        )
        finishTyping()
        return true
    }

    // -------- Email --------

    private suspend fun handleEmailDraft(text: String): Boolean {
        if (!_state.value.isAiConfigured) {
            appendPanda("Add an AI key in Settings → AI provider and I'll draft this email for you.")
            finishTyping()
            return true
        }
        val provider = aiRouter.getActive()
        val prompt = """
            You are a polite, concise email assistant. The user describes who they want to email and what to say. Reply with valid JSON only — no commentary, no fences.
            {
              "to": "email address or person's name (leave blank if unspecified)",
              "subject": "concise subject line",
              "body": "3 to 6 sentence email body, friendly but professional, signed -NexOS user"
            }
            User request:
            $text
        """.trimIndent()
        val response = provider.complete(prompt, maxTokens = 500)
        if (!response.isSuccess) {
            appendPanda("AI provider error: ${response.error}. I'll skip drafting this one.")
            finishTyping()
            return true
        }
        val parsed = runCatching {
            val obj = com.google.gson.JsonParser.parseString(extractJsonObject(response.text)).asJsonObject
            ChatAttachment.EmailDraft(
                to = obj.get("to")?.asString.orEmpty(),
                subject = obj.get("subject")?.asString.orEmpty(),
                body = obj.get("body")?.asString.orEmpty()
            )
        }.getOrNull()
        if (parsed == null) {
            appendPanda("I drafted something but couldn't parse it cleanly:\n\n${response.text.take(600)}")
        } else {
            appendPanda(
                "Here's a draft. Tap Send to open Gmail prefilled, or paste it back to refine.",
                parsed
            )
        }
        finishTyping()
        return true
    }

    // -------- Mood → food --------

    private suspend fun handleMoodToFood(text: String): Boolean {
        if (!_state.value.isAiConfigured) {
            // Local mood → cuisine map fallback so the feature still works without AI
            val cuisine = guessLocalCuisine(text)
            appendPanda(
                "Based on what you said, how about $cuisine? Tap below to open Swiggy.",
                ChatAttachment.FoodSuggestion(
                    cuisine = cuisine,
                    rationale = "Quick suggestion based on common mood-food associations."
                )
            )
            finishTyping()
            return true
        }
        val provider = aiRouter.getActive()
        val prompt = """
            You are a friendly food panda. The user describes their mood; suggest ONE specific food or cuisine they should order on Swiggy that suits the mood. Reply ONLY in JSON.
            {
              "cuisine": "single search term to type into Swiggy (e.g. 'biryani', 'ramen', 'chocolate cake')",
              "rationale": "one sentence reason, friendly tone, panda voice"
            }
            User mood:
            $text
        """.trimIndent()
        val response = provider.complete(prompt, maxTokens = 200)
        if (!response.isSuccess) {
            val fallback = guessLocalCuisine(text)
            appendPanda(
                "AI is unreachable, but my paws say $fallback.",
                ChatAttachment.FoodSuggestion(fallback, "Quick fallback.")
            )
            finishTyping()
            return true
        }
        val parsed = runCatching {
            val obj = com.google.gson.JsonParser.parseString(extractJsonObject(response.text)).asJsonObject
            ChatAttachment.FoodSuggestion(
                cuisine = obj.get("cuisine")?.asString.orEmpty().ifBlank { "comfort food" },
                rationale = obj.get("rationale")?.asString.orEmpty()
            )
        }.getOrNull()
        if (parsed == null) {
            val fallback = guessLocalCuisine(text)
            appendPanda(
                "How about $fallback?",
                ChatAttachment.FoodSuggestion(fallback, "Quick suggestion.")
            )
        } else {
            appendPanda("${parsed.rationale}\n\nSuggested: ${parsed.cuisine}. Tap below to open Swiggy.", parsed)
        }
        finishTyping()
        return true
    }

    /** Pure-Kotlin fallback for the food panda when no AI is configured. */
    private fun guessLocalCuisine(mood: String): String {
        val lower = mood.lowercase()
        return when {
            "tired" in lower || "sleep" in lower -> "ramen"
            "sad" in lower || "down" in lower -> "chocolate"
            "happy" in lower || "celebrat" in lower -> "pizza"
            "cold" in lower || "rain" in lower -> "soup"
            "hot" in lower || "summer" in lower -> "ice cream"
            "stress" in lower || "anxious" in lower -> "khichdi"
            "energy" in lower || "energetic" in lower -> "salad bowl"
            "spicy" in lower -> "biryani"
            else -> "comfort food"
        }
    }

    // -------- Weather --------

    private suspend fun handleWeatherQuestion(text: String): Boolean {
        val lastCity = settings.weatherLastCity.first().ifBlank { "Bengaluru" }
        val cityRes = weatherRepository.searchCity(lastCity).getOrNull()?.firstOrNull()
            ?: return false
        val snapshot = weatherRepository.forecast(cityRes.latitude, cityRes.longitude, cityRes.label).getOrNull()
            ?: return false
        appendPanda(
            "Right now in ${snapshot.locationLabel}: ${snapshot.currentCode.emoji} ${snapshot.currentCode.label}, %.1f°C.".format(snapshot.currentTempC),
            ChatAttachment.WeatherSummary(snapshot.locationLabel, snapshot.currentTempC, snapshot.currentCode)
        )
        _state.update { it.copy(weatherCondition = snapshot.currentCode) }
        finishTyping()
        return true
    }

    private fun preloadWeatherGlyph() {
        viewModelScope.launch {
            val city = settings.weatherLastCity.first().ifBlank { return@launch }
            val res = weatherRepository.searchCity(city).getOrNull()?.firstOrNull() ?: return@launch
            val snap = weatherRepository.forecast(res.latitude, res.longitude, res.label).getOrNull() ?: return@launch
            _state.update { it.copy(weatherCondition = snap.currentCode) }
        }
    }

    // -------- News --------

    private suspend fun handleNewsQuestion(text: String): Boolean {
        if (!newsRepository.hasApiKey()) {
            appendPanda("I need a news key to read headlines. Add one in Settings → GNews API key.")
            finishTyping()
            return true
        }
        val query = text.removePrefix("top news").removePrefix("news about").trim()
        val res = if (query.isBlank()) newsRepository.topHeadlines(null) else newsRepository.search(query)
        res.onSuccess { articles ->
            val top = articles.take(3)
            if (top.isEmpty()) {
                appendPanda("I couldn't find anything matching that right now.")
            } else {
                val headlines = top.map { NewsHeadline(it.title, it.source, it.url) }
                appendPanda(
                    top.joinToString("\n\n") { "• ${it.title} — ${it.source}" },
                    ChatAttachment.NewsHeadlines(headlines)
                )
            }
        }.onFailure { e ->
            appendPanda("News fetch failed: ${e.message}")
        }
        finishTyping()
        return true
    }

    private fun preloadTopHeadlinesIfAi() {
        viewModelScope.launch {
            if (!newsRepository.hasApiKey()) return@launch
            newsRepository.topHeadlines(null).onSuccess { articles ->
                if (articles.isEmpty()) return@onSuccess
                val top = articles.take(3)
                val headlines = top.map { NewsHeadline(it.title, it.source, it.url) }
                appendPanda(
                    "Top headlines right now:\n\n" + top.joinToString("\n\n") { "• ${it.title} — ${it.source}" },
                    ChatAttachment.NewsHeadlines(headlines)
                )
            }
        }
    }

    // -------- Plain AI chat fallback --------

    private suspend fun chatViaAI(text: String) {
        if (!_state.value.isAiConfigured) {
            appendPanda(
                "I can't reach an AI provider yet — add a key in Settings → AI provider. In the meantime I can still parse Google Maps links and schedule alarms locally."
            )
            finishTyping()
            return
        }
        val provider = aiRouter.getActive()
        val prompt = """
            You are Panda, a friendly on-device assistant for an Android super-app called NexOS.
            Be concise (2–4 sentences) and helpful. The user said:
            $text
        """.trimIndent()
        val response = provider.complete(prompt, maxTokens = 350)
        if (response.isSuccess) {
            appendPanda(response.text.trim())
        } else {
            appendPanda("AI error: ${response.error}")
        }
        finishTyping()
    }

    // -------- Helpers --------

    private fun appendUser(text: String) {
        val msg = ChatMessage(id = nextMessageId++, role = ChatRole.User, text = text)
        _state.update { it.copy(messages = it.messages + msg) }
    }

    private fun appendPanda(text: String, attachment: ChatAttachment? = null) {
        val msg = ChatMessage(id = nextMessageId++, role = ChatRole.Panda, text = text, attachment = attachment)
        _state.update { it.copy(messages = it.messages + msg) }
    }

    private fun finishTyping() {
        _state.update { it.copy(isTyping = false) }
    }

    private fun extractJsonObject(raw: String): String {
        val trimmed = raw.trim()
            .removePrefix("```json").removePrefix("```JSON").removePrefix("```")
            .removeSuffix("```").trim()
        val first = trimmed.indexOf('{')
        val last = trimmed.lastIndexOf('}')
        return if (first >= 0 && last > first) trimmed.substring(first, last + 1) else trimmed
    }
}

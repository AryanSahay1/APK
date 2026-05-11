package com.nexos.ai.util

import java.util.Calendar
import java.util.Locale

/**
 * Pure-Kotlin parser for the natural-language reminder phrases the architecture mentions:
 *   "remind me at 8am tomorrow"
 *   "in 30 minutes"
 *   "tomorrow 7:30 pm"
 *   "tonight at 9"
 *   "next monday 8am"
 *
 * Returns a [Result] containing:
 *  - the absolute epoch millis to fire at, and
 *  - a cleaned title (the input with the time phrase stripped out, so the user sees
 *    "remind me" → title "Reminder", while "buy milk at 6pm" → title "buy milk").
 *
 * Returns null when no time can be inferred — callers fall back to asking the AI provider.
 *
 * The parser is deterministic and offline. It deliberately does not handle every English
 * phrase; the AI provider covers the long tail.
 */
object NaturalLanguageTimeParser {

    data class Result(
        val triggerAtMillis: Long,
        val title: String,
        val matchedPhrase: String
    )

    private val relativeRegex = Regex(
        "in\\s+(\\d+)\\s*(min(?:ute)?s?|hr|hour(?:s)?|sec(?:ond)?s?|day(?:s)?)",
        RegexOption.IGNORE_CASE
    )

    // "at 8am", "at 8:30 pm", "at 14:00"
    private val clockRegex = Regex(
        "(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm|a\\.m\\.|p\\.m\\.)?",
        RegexOption.IGNORE_CASE
    )

    private val weekdayMap = mapOf(
        "sunday" to Calendar.SUNDAY, "sun" to Calendar.SUNDAY,
        "monday" to Calendar.MONDAY, "mon" to Calendar.MONDAY,
        "tuesday" to Calendar.TUESDAY, "tue" to Calendar.TUESDAY, "tues" to Calendar.TUESDAY,
        "wednesday" to Calendar.WEDNESDAY, "wed" to Calendar.WEDNESDAY,
        "thursday" to Calendar.THURSDAY, "thu" to Calendar.THURSDAY, "thurs" to Calendar.THURSDAY,
        "friday" to Calendar.FRIDAY, "fri" to Calendar.FRIDAY,
        "saturday" to Calendar.SATURDAY, "sat" to Calendar.SATURDAY
    )

    fun parse(input: String, now: Long = System.currentTimeMillis()): Result? {
        if (input.isBlank()) return null
        val lower = input.lowercase(Locale.US).trim()
        val cal = Calendar.getInstance().apply { timeInMillis = now }

        relativeRegex.find(lower)?.let { match ->
            val amount = match.groupValues[1].toIntOrNull() ?: return@let
            val unit = match.groupValues[2]
            val target = Calendar.getInstance().apply { timeInMillis = now }
            when {
                unit.startsWith("sec") -> target.add(Calendar.SECOND, amount)
                unit.startsWith("min") -> target.add(Calendar.MINUTE, amount)
                unit.startsWith("hr") || unit.startsWith("hour") -> target.add(Calendar.HOUR_OF_DAY, amount)
                unit.startsWith("day") -> target.add(Calendar.DAY_OF_MONTH, amount)
                else -> return@let
            }
            return Result(
                triggerAtMillis = target.timeInMillis,
                title = cleanTitle(input, match.value),
                matchedPhrase = match.value
            )
        }

        // Day anchor (today / tomorrow / weekday / tonight)
        val (anchored, anchorPhrase) = applyDayAnchor(lower, cal)
        val clockMatch = clockRegex.find(anchored.ifBlank { lower })
        if (clockMatch != null) {
            val hour = clockMatch.groupValues[1].toIntOrNull() ?: return null
            val minute = clockMatch.groupValues[2].toIntOrNull() ?: 0
            val meridian = clockMatch.groupValues[3]
                .lowercase(Locale.US).replace(".", "").trim()
            if (hour !in 0..23 || minute !in 0..59) return null

            val target = Calendar.getInstance().apply { timeInMillis = cal.timeInMillis }
            val resolvedHour = when {
                meridian == "am" -> if (hour == 12) 0 else hour
                meridian == "pm" -> if (hour == 12) 12 else hour + 12
                else -> hour
            }
            target.set(Calendar.HOUR_OF_DAY, resolvedHour)
            target.set(Calendar.MINUTE, minute)
            target.set(Calendar.SECOND, 0)
            target.set(Calendar.MILLISECOND, 0)

            // If we anchored to "tonight" without an am/pm and the hour is < 12, assume PM.
            if (anchorPhrase == "tonight" && meridian.isEmpty() && resolvedHour < 12) {
                target.add(Calendar.HOUR_OF_DAY, 12)
            }
            // If no anchor was applied and the computed time is in the past, roll to tomorrow.
            if (anchorPhrase.isEmpty() && target.timeInMillis <= now) {
                target.add(Calendar.DAY_OF_MONTH, 1)
            }

            return Result(
                triggerAtMillis = target.timeInMillis,
                title = cleanTitle(input, listOf(anchorPhrase, clockMatch.value).filter { it.isNotBlank() }),
                matchedPhrase = listOf(anchorPhrase, clockMatch.value).filter { it.isNotBlank() }.joinToString(" ")
            )
        }
        return null
    }

    private fun applyDayAnchor(text: String, base: Calendar): Pair<String, String> {
        // tomorrow
        if ("tomorrow" in text) {
            base.add(Calendar.DAY_OF_MONTH, 1)
            return text.replace("tomorrow", "").trim() to "tomorrow"
        }
        if ("tonight" in text) {
            return text.replace("tonight", "").trim() to "tonight"
        }
        if ("today" in text) {
            return text.replace("today", "").trim() to "today"
        }
        // next <weekday>
        val nextRegex = Regex("next\\s+(${weekdayMap.keys.joinToString("|")})")
        nextRegex.find(text)?.let { m ->
            val day = weekdayMap[m.groupValues[1]] ?: return@let
            advanceToWeekday(base, day, atLeastOneWeekAhead = true)
            return text.replace(m.value, "").trim() to m.value
        }
        // bare <weekday>
        val plainRegex = Regex("\\b(${weekdayMap.keys.joinToString("|")})\\b")
        plainRegex.find(text)?.let { m ->
            val day = weekdayMap[m.groupValues[1]] ?: return@let
            advanceToWeekday(base, day, atLeastOneWeekAhead = false)
            return text.replace(m.value, "").trim() to m.groupValues[1]
        }
        return text to ""
    }

    private fun advanceToWeekday(base: Calendar, targetDay: Int, atLeastOneWeekAhead: Boolean) {
        val current = base.get(Calendar.DAY_OF_WEEK)
        var delta = (targetDay - current + 7) % 7
        if (delta == 0) delta = if (atLeastOneWeekAhead) 7 else 0
        if (atLeastOneWeekAhead && delta < 7) delta += 7 - delta
        base.add(Calendar.DAY_OF_MONTH, delta)
    }

    private fun cleanTitle(input: String, matched: String): String =
        cleanTitle(input, listOf(matched))

    private fun cleanTitle(input: String, matchedPhrases: List<String>): String {
        var cleaned = input
        matchedPhrases.filter { it.isNotBlank() }.forEach { phrase ->
            cleaned = cleaned.replace(phrase, "", ignoreCase = true)
        }
        cleaned = cleaned
            .replace(Regex("\\b(remind me( to)?|set (an? )?(alarm|reminder)( for| to)?|alarm|reminder)\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s{2,}"), " ")
            .replace(Regex("^[\\s.,:;-]+"), "")
            .replace(Regex("[\\s.,:;-]+$"), "")
            .trim()
        return cleaned.ifBlank { "Reminder" }
    }
}

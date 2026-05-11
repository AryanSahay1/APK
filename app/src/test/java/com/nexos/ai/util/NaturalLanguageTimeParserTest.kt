package com.nexos.ai.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class NaturalLanguageTimeParserTest {

    private fun fixedNow(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }
        return cal.timeInMillis
    }

    @Test
    fun `in 30 minutes adds half an hour from now`() {
        val now = fixedNow(2026, 1, 15, 9, 0)
        val result = NaturalLanguageTimeParser.parse("remind me in 30 minutes call mom", now)
        assertThat(result).isNotNull()
        assertThat(result!!.triggerAtMillis - now).isEqualTo(30 * 60 * 1000L)
        assertThat(result.title).contains("call mom")
    }

    @Test
    fun `in 2 hours adds two hours`() {
        val now = fixedNow(2026, 1, 15, 9, 0)
        val result = NaturalLanguageTimeParser.parse("in 2 hours stretch", now)
        assertThat(result).isNotNull()
        assertThat(result!!.triggerAtMillis - now).isEqualTo(2 * 60 * 60 * 1000L)
    }

    @Test
    fun `at 8am tomorrow lands on 8am next day`() {
        val now = fixedNow(2026, 1, 15, 9, 0)
        val result = NaturalLanguageTimeParser.parse("remind me at 8am tomorrow", now)
        assertThat(result).isNotNull()
        val cal = Calendar.getInstance().apply { timeInMillis = result!!.triggerAtMillis }
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(16)
        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(8)
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0)
    }

    @Test
    fun `tomorrow 7 30 pm preserves minute`() {
        val now = fixedNow(2026, 3, 1, 12, 0)
        val result = NaturalLanguageTimeParser.parse("tomorrow 7:30 pm meeting", now)
        assertThat(result).isNotNull()
        val cal = Calendar.getInstance().apply { timeInMillis = result!!.triggerAtMillis }
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(2)
        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(19)
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(30)
        assertThat(result!!.title).contains("meeting")
    }

    @Test
    fun `tonight at 9 maps to PM same day`() {
        val now = fixedNow(2026, 5, 20, 14, 0)
        val result = NaturalLanguageTimeParser.parse("tonight at 9 read book", now)
        assertThat(result).isNotNull()
        val cal = Calendar.getInstance().apply { timeInMillis = result!!.triggerAtMillis }
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(20)
        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(21)
    }

    @Test
    fun `bare clock with past time rolls to tomorrow`() {
        val now = fixedNow(2026, 1, 15, 14, 0)
        val result = NaturalLanguageTimeParser.parse("at 9am wake up", now)
        assertThat(result).isNotNull()
        val cal = Calendar.getInstance().apply { timeInMillis = result!!.triggerAtMillis }
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(16)
        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(9)
    }

    @Test
    fun `returns null for unrecognised input`() {
        assertThat(NaturalLanguageTimeParser.parse("")).isNull()
        assertThat(NaturalLanguageTimeParser.parse("hello world")).isNull()
    }

    @Test
    fun `title falls back to Reminder when only a time is given`() {
        val now = fixedNow(2026, 1, 15, 9, 0)
        val result = NaturalLanguageTimeParser.parse("in 5 minutes", now)
        assertThat(result).isNotNull()
        assertThat(result!!.title).isEqualTo("Reminder")
    }
}

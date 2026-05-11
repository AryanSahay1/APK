package com.nexos.ai.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `WMO 0 maps to Clear`() {
        assertThat(WeatherCondition.fromWmo(0)).isEqualTo(WeatherCondition.Clear)
    }

    @Test
    fun `WMO 45 and 48 map to Fog`() {
        assertThat(WeatherCondition.fromWmo(45)).isEqualTo(WeatherCondition.Fog)
        assertThat(WeatherCondition.fromWmo(48)).isEqualTo(WeatherCondition.Fog)
    }

    @Test
    fun `WMO 63 maps to Rain`() {
        assertThat(WeatherCondition.fromWmo(63)).isEqualTo(WeatherCondition.Rain)
    }

    @Test
    fun `WMO 95 maps to Thunderstorm`() {
        assertThat(WeatherCondition.fromWmo(95)).isEqualTo(WeatherCondition.Thunderstorm)
    }

    @Test
    fun `unknown WMO code maps to Unknown`() {
        assertThat(WeatherCondition.fromWmo(-1)).isEqualTo(WeatherCondition.Unknown)
        assertThat(WeatherCondition.fromWmo(999)).isEqualTo(WeatherCondition.Unknown)
    }

    @Test
    fun `every condition has a non-blank emoji and label`() {
        WeatherCondition.entries.forEach { condition ->
            assertThat(condition.emoji).isNotEmpty()
            assertThat(condition.label).isNotEmpty()
        }
    }
}

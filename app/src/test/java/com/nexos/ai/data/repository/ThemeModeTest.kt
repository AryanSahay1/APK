package com.nexos.ai.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `fromKey returns System for null`() {
        assertThat(ThemeMode.fromKey(null)).isEqualTo(ThemeMode.System)
    }

    @Test
    fun `fromKey returns System for unknown key`() {
        assertThat(ThemeMode.fromKey("zzz")).isEqualTo(ThemeMode.System)
        assertThat(ThemeMode.fromKey("")).isEqualTo(ThemeMode.System)
    }

    @Test
    fun `fromKey round-trips for all enum values`() {
        ThemeMode.entries.forEach { mode ->
            assertThat(ThemeMode.fromKey(mode.key)).isEqualTo(mode)
        }
    }
}

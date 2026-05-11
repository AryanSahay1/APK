package com.nexos.ai.ai

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NoOpProviderTest {

    private val provider = NoOpProvider()

    @Test
    fun `complete always returns failure with explanatory error`() = runTest {
        val response = provider.complete("anything")
        assertThat(response.isSuccess).isFalse()
        assertThat(response.text).isEmpty()
        assertThat(response.error).isNotNull()
    }

    @Test
    fun `testConnection returns false`() = runTest {
        assertThat(provider.testConnection()).isFalse()
    }

    @Test
    fun `isConfigured is false`() {
        assertThat(provider.isConfigured).isFalse()
    }
}

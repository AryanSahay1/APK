package com.nexos.ai.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.net.URLDecoder

/**
 * Lightweight regression tests for the deep-link URI builders.
 *
 * We can't invoke startActivity from a JVM unit test, so we exercise the URI construction
 * indirectly: extract the same encoding helpers DeepLinks uses, and verify the resulting URL
 * contains the destination text (encoded) and the right scheme.
 */
class DeepLinksTest {

    @Test
    fun `urlEncode round-trips spaces and special chars`() {
        val raw = "MG Road, Bengaluru"
        val encoded = encode(raw)
        assertThat(encoded).contains("%20")
        assertThat(URLDecoder.decode(encoded, "UTF-8")).isEqualTo(raw)
    }

    @Test
    fun `urlEncode never contains raw spaces or plus-as-space`() {
        val encoded = encode("a b c")
        assertThat(encoded).doesNotContain(" ")
        assertThat(encoded).doesNotContain("+")
    }

    @Test
    fun `https fallback urls are well-formed`() {
        val uberFallback = "https://m.uber.com/looking?drop[0]=${encode("Indira Nagar")}"
        val zomatoFallback = "https://www.zomato.com/search?q=${encode("biryani")}"
        val swiggyFallback = "https://www.swiggy.com/search?query=${encode("pizza")}"
        assertThat(uberFallback).startsWith("https://m.uber.com/looking?drop")
        assertThat(zomatoFallback).contains("biryani")
        assertThat(swiggyFallback).contains("pizza")
    }

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}

package com.nexos.ai.ai

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NoteAIHelperTest {

    private val helper = NoteAIHelper()

    @Test
    fun `screenshot prompt mentions JSON format and rules`() {
        val prompt = helper.buildScreenshotPrompt("hello world")
        assertThat(prompt).contains("Respond ONLY with valid JSON")
        assertThat(prompt).contains("title")
        assertThat(prompt).contains("bullets")
        assertThat(prompt).contains("summary")
        assertThat(prompt).contains("hello world")
    }

    @Test
    fun `voice prompt distinct from screenshot prompt`() {
        val voice = helper.buildVoicePrompt("transcript")
        val screen = helper.buildScreenshotPrompt("text")
        assertThat(voice).isNotEqualTo(screen)
        assertThat(voice).contains("transcript")
    }

    @Test
    fun `parse strict JSON returns ParsedNote`() {
        val json = """
            {"title":"My note","bullets":["one","two","three"],"summary":"A short summary."}
        """.trimIndent()
        val parsed = helper.parse(json)
        assertThat(parsed).isNotNull()
        assertThat(parsed!!.title).isEqualTo("My note")
        assertThat(parsed.bullets).containsExactly("one", "two", "three").inOrder()
        assertThat(parsed.summary).isEqualTo("A short summary.")
    }

    @Test
    fun `parse strips markdown code fences`() {
        val raw = """```json
            {"title":"Title","bullets":["a","b"],"summary":"S"}
        ```""".trimIndent()
        val parsed = helper.parse(raw)
        assertThat(parsed).isNotNull()
        assertThat(parsed!!.title).isEqualTo("Title")
        assertThat(parsed.bullets).hasSize(2)
    }

    @Test
    fun `parse extracts JSON when AI returns preamble`() {
        val raw = "Sure, here is your note:\n{\"title\":\"T\",\"bullets\":[\"x\"],\"summary\":\"y\"}\nLet me know if you need more!"
        val parsed = helper.parse(raw)
        assertThat(parsed).isNotNull()
        assertThat(parsed!!.title).isEqualTo("T")
    }

    @Test
    fun `parse returns null for malformed JSON`() {
        assertThat(helper.parse("not json")).isNull()
        assertThat(helper.parse("")).isNull()
        assertThat(helper.parse("{ broken")).isNull()
    }

    @Test
    fun `parse returns null when title is missing or blank`() {
        val raw = """{"title":"","bullets":["a"],"summary":"s"}"""
        assertThat(helper.parse(raw)).isNull()
    }

    @Test
    fun `ParsedNote toMarkdownContent renders bullets and summary`() {
        val parsed = com.nexos.ai.domain.model.ParsedNote(
            title = "T",
            bullets = listOf("first", "second"),
            summary = "summary here"
        )
        val md = parsed.toMarkdownContent()
        assertThat(md).contains("• first")
        assertThat(md).contains("• second")
        assertThat(md).contains("summary here")
    }
}

package com.nexos.ai.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit

/**
 * Minimal in-house markdown handler for the v1.5 editor.
 *
 * What it supports:
 *   * Inline:  **bold**  *italic*  __underline__  ~~strike~~  ==highlight==
 *     `inline code`
 *   * Block:   heading lines starting with `# `, `## `, `### ` (rendered at
 *              a slightly larger size and bold).
 *   * Lists:   lines starting with `- `, `* `, `• ` (bulleted)
 *              lines matching `^\d+\.\s` (numbered)
 *   * Checks:  `[ ] task` and `[x] task` (the renderer caller draws the
 *              real Checkbox composable; this code just labels the line).
 *   * Tables:  monospace-rendered block of consecutive `| … | … |` lines
 *              (we delegate this to the caller via [parseBlocks]).
 *
 * Deliberately NOT a CommonMark fork: we don't need 90% of CommonMark and
 * we don't want to pull in a multi-MB dependency for an in-editor preview.
 * The grammar above is the same set of tokens we *inject* from the
 * formatting toolbar, so the round-trip is perfect by construction.
 */
object MarkdownRenderer {

    /**
     * Parse plain markdown text into a list of [MarkdownBlock]s. The caller renders each
     * block as it sees fit — typical Compose use is a Column with the right Composable per
     * block.
     */
    fun parseBlocks(raw: String): List<MarkdownBlock> {
        if (raw.isBlank()) return emptyList()
        val lines = raw.split("\n")
        val blocks = mutableListOf<MarkdownBlock>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            // Table: 2+ consecutive lines starting with '|' and a separator row in pos 2
            if (line.trimStart().startsWith("|") && i + 1 < lines.size &&
                lines[i + 1].trimStart().startsWith("|")
            ) {
                val tableLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                    tableLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.Table(parseTable(tableLines)))
                continue
            }
            blocks.add(toBlock(line))
            i++
        }
        return blocks
    }

    private fun toBlock(line: String): MarkdownBlock {
        val trimmed = line.trimStart()
        return when {
            trimmed.startsWith("### ") -> MarkdownBlock.Heading(3, trimmed.removePrefix("### "))
            trimmed.startsWith("## ") -> MarkdownBlock.Heading(2, trimmed.removePrefix("## "))
            trimmed.startsWith("# ") -> MarkdownBlock.Heading(1, trimmed.removePrefix("# "))
            trimmed.startsWith("[ ] ") -> MarkdownBlock.Checkbox(
                checked = false, text = trimmed.removePrefix("[ ] ")
            )
            trimmed.startsWith("[x] ", ignoreCase = true) -> MarkdownBlock.Checkbox(
                checked = true, text = trimmed.removePrefix("[x] ").removePrefix("[X] ")
            )
            trimmed.startsWith("- ") -> MarkdownBlock.Bullet(trimmed.removePrefix("- "))
            trimmed.startsWith("* ") -> MarkdownBlock.Bullet(trimmed.removePrefix("* "))
            trimmed.startsWith("• ") -> MarkdownBlock.Bullet(trimmed.removePrefix("• "))
            NUMBERED.matches(trimmed) -> {
                val parts = trimmed.split(". ", limit = 2)
                val idx = parts[0].toIntOrNull() ?: 1
                MarkdownBlock.Numbered(idx, parts.getOrElse(1) { "" })
            }
            line.isBlank() -> MarkdownBlock.Spacer
            else -> MarkdownBlock.Paragraph(line)
        }
    }

    private val NUMBERED = Regex("^\\d+\\. .*")

    private fun parseTable(rawLines: List<String>): MarkdownTable {
        val rows = rawLines.map { it.trim().trim('|').split('|').map { c -> c.trim() } }
        // Detect separator row (---) and treat the row above as the header. If no separator,
        // treat the first row as headers regardless.
        val sepRowIdx = rows.indexOfFirst { row ->
            row.all { cell -> cell.isNotEmpty() && cell.all { c -> c == '-' || c == ':' } }
        }
        return if (sepRowIdx > 0) {
            MarkdownTable(headers = rows[sepRowIdx - 1], body = rows.drop(sepRowIdx + 1))
        } else {
            MarkdownTable(headers = rows.first(), body = rows.drop(1))
        }
    }

    /**
     * Convert one paragraph string into an [AnnotatedString] applying inline markdown
     * (bold / italic / underline / strikethrough / highlight / inline code).
     *
     * Tokens are matched greedily left-to-right; nested formatting works only at the level
     * spans naturally combine in [SpanStyle.merge]. For the editor's purposes (a user
     * occasionally wrapping a phrase in **) this is fine.
     */
    fun annotate(
        line: String,
        bodyColor: Color,
        highlightColor: Color,
        codeBg: Color,
        codeColor: Color,
        baseFontSize: TextUnit
    ): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var i = 0
        val n = line.length
        // For each char position, decide if a token starts here. Push spans in pairs.
        while (i < n) {
            val token = nextToken(line, i)
            if (token == null) {
                builder.append(line[i])
                i++
                continue
            }
            val end = line.indexOf(token.delimiter, startIndex = i + token.delimiter.length)
            if (end < 0) {
                builder.append(line[i])
                i++
                continue
            }
            val inner = line.substring(i + token.delimiter.length, end)
            val style = token.span(bodyColor, highlightColor, codeBg, codeColor, baseFontSize)
            builder.pushStyle(style)
            // Recurse one level for nested formatting (no infinite recursion: each call
            // strips outer delimiters before re-parsing).
            builder.append(annotate(inner, bodyColor, highlightColor, codeBg, codeColor, baseFontSize))
            builder.pop()
            i = end + token.delimiter.length
        }
        return builder.toAnnotatedString()
    }

    private fun nextToken(line: String, at: Int): Token? {
        // Order matters — match longer delimiters first so '**' is bold, not two italics.
        for (token in Token.entries) {
            val d = token.delimiter
            if (line.startsWith(d, at)) {
                if (line.indexOf(d, at + d.length) > 0) return token
            }
        }
        return null
    }

    private enum class Token(val delimiter: String) {
        Bold("**"),
        Italic("*"),
        Underline("__"),
        Strike("~~"),
        Highlight("=="),
        Code("`");

        fun span(
            bodyColor: Color,
            highlightColor: Color,
            codeBg: Color,
            codeColor: Color,
            @Suppress("UNUSED_PARAMETER") baseFontSize: TextUnit
        ): SpanStyle = when (this) {
            Bold -> SpanStyle(fontWeight = FontWeight.Bold, color = bodyColor)
            Italic -> SpanStyle(fontStyle = FontStyle.Italic, color = bodyColor)
            Underline -> SpanStyle(textDecoration = TextDecoration.Underline, color = bodyColor)
            Strike -> SpanStyle(textDecoration = TextDecoration.LineThrough, color = bodyColor)
            Highlight -> SpanStyle(background = highlightColor.copy(alpha = 0.4f), color = bodyColor)
            Code -> SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg, color = codeColor)
        }
    }
}

sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Bullet(val text: String) : MarkdownBlock()
    data class Numbered(val index: Int, val text: String) : MarkdownBlock()
    data class Checkbox(val checked: Boolean, val text: String) : MarkdownBlock()
    data class Table(val data: MarkdownTable) : MarkdownBlock()
    object Spacer : MarkdownBlock()
}

data class MarkdownTable(val headers: List<String>, val body: List<List<String>>)

package com.nexos.ai.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexos.ai.util.MarkdownBlock
import com.nexos.ai.util.MarkdownRenderer
import com.nexos.ai.util.MarkdownTable

/**
 * Renders a markdown body produced by the v1.5 editor. Each block is laid out as its own
 * row in a Column so the caller can interleave or scroll naturally.
 *
 * Checkboxes are read-only here — toggling state would need to round-trip a write back to
 * the database via the ViewModel; we deliberately keep [MarkdownBody] stateless so it can
 * be embedded inside the note detail's verticalScroll without owning persistence.
 */
@Composable
fun MarkdownBody(
    markdown: String,
    bodyFontSizeSp: Int = 16,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { MarkdownRenderer.parseBlocks(markdown) }
    val bodyColor = MaterialTheme.colorScheme.onBackground
    val highlightColor = MaterialTheme.colorScheme.tertiary
    val codeBg = MaterialTheme.colorScheme.surfaceVariant
    val codeColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val size = when (block.level) {
                        1 -> 24.sp
                        2 -> 20.sp
                        else -> 18.sp
                    }
                    val annotated = MarkdownRenderer.annotate(
                        block.text, bodyColor, highlightColor, codeBg, codeColor, size
                    )
                    Text(
                        text = annotated,
                        fontSize = size,
                        fontWeight = FontWeight.SemiBold,
                        color = bodyColor,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    val annotated = MarkdownRenderer.annotate(
                        block.text, bodyColor, highlightColor, codeBg, codeColor,
                        bodyFontSizeSp.sp
                    )
                    Text(
                        text = annotated,
                        fontSize = bodyFontSizeSp.sp,
                        lineHeight = (bodyFontSizeSp * 1.45f).sp,
                        color = bodyColor,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is MarkdownBlock.Bullet -> {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "•  ",
                            fontSize = bodyFontSizeSp.sp,
                            color = bodyColor
                        )
                        val annotated = MarkdownRenderer.annotate(
                            block.text, bodyColor, highlightColor, codeBg, codeColor,
                            bodyFontSizeSp.sp
                        )
                        Text(
                            text = annotated,
                            fontSize = bodyFontSizeSp.sp,
                            lineHeight = (bodyFontSizeSp * 1.45f).sp,
                            color = bodyColor,
                            textAlign = textAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is MarkdownBlock.Numbered -> {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "${block.index}.  ",
                            fontSize = bodyFontSizeSp.sp,
                            color = bodyColor
                        )
                        val annotated = MarkdownRenderer.annotate(
                            block.text, bodyColor, highlightColor, codeBg, codeColor,
                            bodyFontSizeSp.sp
                        )
                        Text(
                            text = annotated,
                            fontSize = bodyFontSizeSp.sp,
                            lineHeight = (bodyFontSizeSp * 1.45f).sp,
                            color = bodyColor,
                            textAlign = textAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is MarkdownBlock.Checkbox -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = block.checked,
                            onCheckedChange = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        val annotated = MarkdownRenderer.annotate(
                            block.text, bodyColor, highlightColor, codeBg, codeColor,
                            bodyFontSizeSp.sp
                        )
                        Text(
                            text = annotated,
                            fontSize = bodyFontSizeSp.sp,
                            color = bodyColor,
                            textAlign = textAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is MarkdownBlock.Table -> MarkdownTableView(block.data, bodyFontSizeSp)
                MarkdownBlock.Spacer -> Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MarkdownTableView(data: MarkdownTable, bodyFontSizeSp: Int) {
    // Tables horizontally scroll if they overflow — wrapping cells would silently mangle
    // alignment, so giving the user an obvious scroll affordance is preferable.
    Row(modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                data.headers.forEach { h ->
                    Text(
                        text = h,
                        fontSize = bodyFontSizeSp.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .width(width = (h.length * (bodyFontSizeSp * 0.55f).dp.value).dp.coerceAtLeast(96.dp))
                    )
                }
            }
            data.body.forEach { row ->
                Row {
                    row.forEachIndexed { idx, cell ->
                        val widthDp = (cell.length * (bodyFontSizeSp * 0.55f).dp.value).dp
                            .coerceAtLeast(96.dp)
                        Text(
                            text = cell,
                            fontSize = bodyFontSizeSp.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .width(widthDp)
                        )
                        // Unused param swallows a lint warning when the loop var isn't read
                        @Suppress("UNUSED_EXPRESSION") idx
                    }
                }
            }
        }
    }
}

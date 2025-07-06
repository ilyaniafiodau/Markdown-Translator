package com.example.markitdown

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Assert.*
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkdownParserTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun testHeaderParsing() {
        val parser = MarkdownParser(context)
        val views = parser.parse("# Header1\n## Header2")

        assertEquals(2, views.size)
        assertTrue(views[0] is TextView)
        assertEquals("Header1", (views[0] as TextView).text)

        assertTrue(views[1] is TextView)
        assertEquals("Header2", (views[1] as TextView).text)
    }

    @Test
    fun testTextFormatting() {
        val parser = MarkdownParser(context)
        val views = parser.parse("Normal **bold** *italic* ~~strike~~")

        assertEquals(1, views.size)
        val text = (views[0] as TextView).text as SpannableString

        val boldSpans = text.getSpans(7, 14, StyleSpan::class.java)
        assertEquals(1, boldSpans.size)
        assertEquals(Typeface.BOLD, boldSpans[0].style)

        val italicSpans = text.getSpans(16, 23, StyleSpan::class.java)
        assertEquals(1, italicSpans.size)
        assertEquals(Typeface.ITALIC, italicSpans[0].style)

        val strikeSpans = text.getSpans(25, 34, StrikethroughSpan::class.java)
        assertEquals(1, strikeSpans.size)
    }

    @Test
    fun testTableParsing() {
        val markdownTable = """
        | Header1 | Header2 |
        |---------|---------|
        | Cell1   | Cell2   |
    """.trimIndent()

        val parser = MarkdownParser(context)
        val views = parser.parse(markdownTable)

        assertEquals(1, views.size)
        assertTrue(views[0] is TableLayout)

        val table = views[0] as TableLayout
        assertEquals(3, table.childCount)

        assertTrue(table.getChildAt(0) is TableRow)
        assertTrue(table.getChildAt(1) is View)
        assertTrue(table.getChildAt(2) is TableRow)
    }
}

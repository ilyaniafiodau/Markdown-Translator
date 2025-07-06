package com.example.markitdown

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat

class MarkdownParser(private val context: Context) {
    private val headerPattern =
        """^(#{1,6})\s+(.*)""".toRegex()    //sequence of #, ##,.., ###### at the beginning of text followed by white space and any number of symbols
    private val boldPattern = """\*\*(.*?)\*\*""".toRegex()         //(.*)? -- any text or no text
    private val italicPattern = """\*(.*?)\*""".toRegex()
    private val strikePattern = """~~(.*?)~~""".toRegex()
    private val imagePattern = """!\[(.*?)]\((.*?)\)""".toRegex()
    private val tablePattern = """^\|(.+)\|$""".toRegex()

    fun parse(content: String): List<View> {
        val views = mutableListOf<View>()
        val lines = content.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()
            when {
                line.isEmpty() -> {}
                headerPattern.matches(line) -> parseHeader(line)?.let { views.add(it) }
                imagePattern.containsMatchIn(line) -> parseImage(line)?.let { views.add(it) }
                tablePattern.matches(line) -> {
                    val tableLines = collectTableLines(lines, i)
                    views.add(createTable(tableLines))
                    i += tableLines.size - 1
                }

                else -> views.add(createFormattedTextView(line))
            }
            i++
        }
        return views
    }

    private fun parseHeader(line: String): TextView? {
        val match = headerPattern.find(line) ?: return null
        val level = match.groupValues[1].length
        val text = match.groupValues[2]

        return TextView(context).apply {
            setText(text)
            textSize = (24 - (level * 2)).toFloat()
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }
    }

    private fun parseImage(line: String): ImageView? {
        val match = imagePattern.find(line) ?: return null
        val (alt, url) = match.destructured

        return ImageView(context).apply {
            contentDescription = alt
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            ImageUploader.load(url, this)
        }
    }

    private fun collectTableLines(lines: List<String>, start: Int): List<List<String>> {
        val tableLines = mutableListOf<List<String>>()
        var i = start

        while (i < lines.size && tablePattern.matches(lines[i].trim())) {
            tableLines.add(
                lines[i].split("|")
                    .drop(1).dropLast(1)
                    .map { it.trim() }
            )
            i++
        }
        return tableLines
    }

    private fun createTable(rows: List<List<String>>): TableLayout {
        return TableLayout(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 8, 0, 16)

            if (rows.isEmpty()) return@apply

            addView(createTableRow(rows[0], true))

            addView(createDivider())

            for (i in 2 until rows.size) {
                addView(createTableRow(rows[i], false))
            }
        }
    }

    private fun createTableRow(
        cells: List<String>,
        isHeader: Boolean
    ): TableRow {
        return TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )

            for (cell in cells) {
                addView(TextView(context).apply {
                    text = cell
                    setPadding(8, 4, 8, 4)
                    gravity = Gravity.CENTER

                    if (isHeader) {
                        setTypeface(null, Typeface.BOLD)
                        setBackgroundColor(
                            ContextCompat.getColor(context, R.color.tableHeader)
                        )
                    }
                })
            }
        }
    }

    private fun createDivider(): View {
        return View(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2
            )
            setBackgroundColor(Color.GRAY)
        }
    }

    private fun createFormattedTextView(text: String): TextView {
        val spannable = SpannableString(text)

        applySpans(spannable, boldPattern, StyleSpan(Typeface.BOLD))
        applySpans(spannable, italicPattern, StyleSpan(Typeface.ITALIC))
        applySpans(spannable, strikePattern, StrikethroughSpan())

        return TextView(context).apply {
            setText(spannable, TextView.BufferType.SPANNABLE)
            setPadding(0, 4, 0, 4)
            textSize = 16f
        }
    }

    private fun applySpans(spannable: Spannable, pattern: Regex, span: Any) {
        pattern.findAll(spannable).forEach { match ->
            spannable.setSpan(
                span,
                match.range.first,
                match.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}
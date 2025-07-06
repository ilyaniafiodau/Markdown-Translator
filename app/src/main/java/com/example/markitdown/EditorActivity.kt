package com.example.markitdown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.markitdown.databinding.ActivityEditorBinding

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding
    private lateinit var editor: EditText
    private val formattingActions = listOf("**Bold**", "*Italic*", "~~Strike~~", "# Heading")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editor.setText(intent.getStringExtra("content") ?: "")

        setupFormattingToolbar()

        binding.btnSave.setOnClickListener {
            val newContent = editor.text.toString()
            setResult(RESULT_OK, Intent().putExtra("content", newContent))
            finish()
        }
    }

    private fun setupFormattingToolbar() {
        formattingActions.forEach { action ->
            val button = Button(this).apply {
                text = action
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener { insertFormatting(action) }
            }
            binding.formattingToolbar.addView(button)
        }
    }

    private fun insertFormatting(format: String) {
        val start = editor.selectionStart
        val end = editor.selectionEnd
        val selectedText = editor.text.substring(start, end)

        val formattedText = when {
            format.startsWith("#") -> "$format "
            selectedText.isNotEmpty() -> format.replace("text", selectedText)
            else -> format
        }

        editor.text.replace(start, end, formattedText)
    }
}
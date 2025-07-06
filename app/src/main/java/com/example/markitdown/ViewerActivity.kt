package com.example.markitdown

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.markitdown.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val content = intent.getStringExtra("content") ?: ""

        val parser = MarkdownParser(this)
        val views = parser.parse(content)

        views.forEach { view ->
            binding.viewer.addView(view)
        }

        editLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.getStringExtra("content")?.let { newContent ->
                    setResult(RESULT_OK, Intent().putExtra("content", newContent))
                    finish()
                }
            }
        }

        binding.btnEdit.setOnClickListener {
            Intent(this, EditorActivity::class.java).apply {
                putExtra("content", content)
                editLauncher.launch(this)
            }
        }
    }
}
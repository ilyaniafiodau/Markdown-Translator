package com.example.markitdown

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.markitdown.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnFile.setOnClickListener { openFilePicker() }
        binding.btnUrl.setOnClickListener { downloadFromUrl(binding.edTxtUrl.text.toString()) }

        filePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val content = readFileContent(uri)
                            withContext(Dispatchers.Main) {
                                launchViewer(content)
                            }
                        }
                    }
                }
            }

        viewerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.getStringExtra("content")?.let { content ->
                    launchViewer(content)
                }
            }
        }
    }

    private fun openFilePicker() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            filePickerLauncher.launch(this)
        }
    }

    private fun downloadFromUrl(urlString: String) {
        if (urlString.isBlank()) {
            Toast.makeText(this, "Please enter URL", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val content = downloadContent(urlString)
                withContext(Dispatchers.Main) {
                    launchViewer(content)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Download failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun downloadContent(urlString: String): String = withContext(Dispatchers.IO) {
        URL(urlString).openStream().use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                val content = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    content.append(line).append('\n')
                }
                content.toString()
            }
        }
    }

    private fun readFileContent(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                reader.readText()
            }
        } ?: ""
    }

    private fun launchViewer(content: String) {
        Intent(this, ViewerActivity::class.java).apply {
            putExtra("content", content)
            viewerLauncher.launch(this)
        }
    }
}


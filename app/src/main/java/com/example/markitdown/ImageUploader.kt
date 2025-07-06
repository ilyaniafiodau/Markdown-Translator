package com.example.markitdown

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object ImageUploader {
    private val cache = object : LruCache<String, Bitmap>(maxMemory() / 8) {
        override fun sizeOf(key: String, bitmap: Bitmap) = bitmap.byteCount
    }

    fun load(url: String, imageView: ImageView) {
        val bitmap = cache.get(url)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = downloadBitmap(url)
                    bitmap?.let {
                        cache.put(url, it)
                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun downloadBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            BitmapFactory.decodeStream(connection.inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun maxMemory(): Int = (Runtime.getRuntime().maxMemory() / 1024).toInt()
}
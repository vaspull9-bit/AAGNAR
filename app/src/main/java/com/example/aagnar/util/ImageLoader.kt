package com.example.aagnar.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.example.aagnar.R
import kotlinx.coroutines.*
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoader @Inject constructor() {

    private val memoryCache = LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 8).toInt())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun loadImage(url: String, target: ImageView, placeholder: Int = R.drawable.ic_profile) {
        target.setImageResource(placeholder)

        memoryCache[url]?.let { bitmap ->
            target.setImageBitmap(bitmap)
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val bitmap = downloadBitmap(url)
                bitmap?.let {
                    memoryCache.put(url, it)
                    withContext(Dispatchers.Main) {
                        target.setImageBitmap(it)
                    }
                }
            } catch (e: Exception) {
                // Ошибка загрузки - остаётся placeholder
            }
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connect()
            val input = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            null
        }
    }

    fun clearCache() {
        memoryCache.evictAll()
    }

    fun shutdown() {
        scope.cancel()
    }
}

class LruCache<K, V>(private val maxSize: Int) {
    private val cache = LinkedHashMap<K, V>(16, 0.75f, true)
    private var size = 0

    operator fun get(key: K): V? = cache[key]

    fun put(key: K, value: V) {
        val previous = cache.put(key, value)
        previous?.let { size -= getSize(it) }
        size += getSize(value)

        while (size > maxSize && cache.isNotEmpty()) {
            val eldest = cache.entries.iterator().next()
            cache.remove(eldest.key)
            size -= getSize(eldest.value)
        }
    }

    fun evictAll() {
        cache.clear()
        size = 0
    }

    private fun getSize(value: V): Int {
        return when (value) {
            is Bitmap -> value.byteCount
            else -> 1
        }
    }
}
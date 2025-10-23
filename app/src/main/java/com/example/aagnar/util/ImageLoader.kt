object ImageLoader {

    private const val MEMORY_CACHE_SIZE = (Runtime.getRuntime().maxMemory() / 8).toInt()
    private const val DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB

    private val memoryCache = LruCache<String, Bitmap>(MEMORY_CACHE_SIZE)

    private val diskCache: DiskLruCache by lazy {
        val cacheDir = File(Application.context.cacheDir, "images")
        DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE)
    }

    // Оптимизация: предзагрузка и кэширование
    suspend fun loadImage(url: String, target: ImageView, placeholder: Int = R.drawable.ic_placeholder) {
        target.setImageResource(placeholder)

        // Проверяем кэш в памяти
        memoryCache[url]?.let { bitmap ->
            target.setImageBitmap(bitmap)
            return
        }

        // Проверяем дисковый кэш
        withContext(Dispatchers.IO) {
            try {
                val snapshot = diskCache.get(url)
                snapshot?.getInputStream(0)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    bitmap?.let {
                        // Сохраняем в память
                        memoryCache.put(url, it)

                        withContext(Dispatchers.Main) {
                            target.setImageBitmap(it)
                        }
                    }
                } ?: loadFromNetwork(url, target)
            } catch (e: Exception) {
                loadFromNetwork(url, target)
            }
        }
    }

    private suspend fun loadFromNetwork(url: String, target: ImageView) {
        withContext(Dispatchers.IO) {
            try {
                val bitmap = downloadBitmap(url)
                bitmap?.let {
                    // Сохраняем в кэши
                    memoryCache.put(url, it)
                    saveToDiskCache(url, it)

                    withContext(Dispatchers.Main) {
                        target.setImageBitmap(it)
                    }
                }
            } catch (e: Exception) {
                Log.e("ImageLoader", "Failed to load image: ${e.message}")
            }
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToDiskCache(url: String, bitmap: Bitmap) {
        try {
            val editor = diskCache.edit(url)
            editor?.let {
                it.newOutputStream(0).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
                }
                it.commit()
            }
        } catch (e: Exception) {
            Log.e("ImageLoader", "Failed to save to disk cache: ${e.message}")
        }
    }

    // Очистка кэша
    fun clearCache() {
        memoryCache.evictAll()
        diskCache.delete()
    }
}
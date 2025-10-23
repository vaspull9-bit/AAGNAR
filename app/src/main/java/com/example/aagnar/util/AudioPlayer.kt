package com.example.aagnar.util

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var currentFile: String? = null
    private var isPlaying = false
    private var progressCallback: ((Int, Int) -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    val currentPos = player.currentPosition
                    val duration = player.duration
                    progressCallback?.invoke(currentPos, duration)
                    handler.postDelayed(this, 100)
                }
            }
        }
    }

    fun playAudio(filePath: String, onCompletion: () -> Unit = {}) {
        try {
            // Останавливаем предыдущее воспроизведение
            stopAudio()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener {
                    start()
                    isPlaying = true
                    handler.post(progressRunnable)
                }
                setOnCompletionListener {
                    stopAudio()
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    stopAudio()
                    true
                }
                prepareAsync()
            }
            currentFile = filePath
        } catch (e: Exception) {
            e.printStackTrace()
            stopAudio()
        }
    }

    fun pauseAudio() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isPlaying = false
                handler.removeCallbacks(progressRunnable)
            }
        }
    }

    fun resumeAudio() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying && currentFile != null) {
                player.start()
                isPlaying = true
                handler.post(progressRunnable)
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
        currentFile = null
        isPlaying = false
        handler.removeCallbacks(progressRunnable)
        progressCallback?.invoke(0, 0)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun setProgressCallback(callback: (Int, Int) -> Unit) {
        this.progressCallback = callback
    }

    fun isPlaying(): Boolean = isPlaying

    fun getCurrentFile(): String? = currentFile

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0
}
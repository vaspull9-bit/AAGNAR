package com.example.aagnar.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var startTime: Long = 0

    companion object {
        private const val MAX_DURATION_MS = 2 * 60 * 1000 // 2 minutes max
    }

    fun startRecording(): Result<File> {
        return try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)

                outputFile = createAudioFile()
                setOutputFile(outputFile?.absolutePath)

                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            isRecording = true

            Result.success(outputFile!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopRecording(): Result<Pair<File, Int>> {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            isRecording = false

            Result.success(Pair(outputFile!!, duration))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            // Удаляем временный файл
            outputFile?.delete()
            outputFile = null
            isRecording = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentDuration(): Int {
        return if (isRecording) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }
    }

    fun isRecording(): Boolean = isRecording

    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File.createTempFile(
            "VOICE_${timeStamp}_",
            ".m4a",
            storageDir
        )
    }

    fun encodeAudioToBase64(audioFile: File): String {
        return android.util.Base64.encodeToString(audioFile.readBytes(), android.util.Base64.DEFAULT)
    }

    fun decodeAudioFromBase64(base64Data: String, fileName: String): File {
        val audioData = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
        val outputFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName)
        outputFile.writeBytes(audioData)
        return outputFile
    }

    fun getAudioDuration(file: File): Int {
        return try {
            val mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
            }
            val duration = mediaPlayer.duration / 1000
            mediaPlayer.release()
            duration
        } catch (e: Exception) {
            0
        }
    }
}
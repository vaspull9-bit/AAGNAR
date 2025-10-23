package com.example.aagnar.presentation.ui.chat

import androidx.lifecycle.ViewModel
import com.example.aagnar.util.AudioPlayer
import com.example.aagnar.util.AudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer,
    private val audioRecorder: AudioRecorder
) : ViewModel() {

    var currentPlayingMessageId: String? = null
    var currentRecordingMessageId: String? = null

    fun startRecording(): Result<Unit> {
        return try {
            audioRecorder.startRecording()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopRecording(): Result<Pair<String, Int>> {
        return try {
            val result = audioRecorder.stopRecording()
            result.map { (file, duration) ->
                val base64Audio = audioRecorder.encodeAudioToBase64(file)
                Pair(base64Audio, duration)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cancelRecording() {
        audioRecorder.cancelRecording()
    }

    fun getRecordingDuration(): Int {
        return audioRecorder.getCurrentDuration()
    }

    fun isRecording(): Boolean {
        return audioRecorder.isRecording()
    }

    fun playVoiceMessage(messageId: String, filePath: String) {
        currentPlayingMessageId = messageId
        audioPlayer.playAudio(filePath) {
            currentPlayingMessageId = null
        }
    }

    fun pauseVoiceMessage() {
        audioPlayer.pauseAudio()
    }

    fun resumeVoiceMessage() {
        audioPlayer.resumeAudio()
    }

    fun stopVoiceMessage() {
        audioPlayer.stopAudio()
        currentPlayingMessageId = null
    }

    fun seekVoiceMessage(position: Int) {
        audioPlayer.seekTo(position)
    }

    fun setAudioProgressCallback(callback: (Int, Int) -> Unit) {
        audioPlayer.setProgressCallback(callback)
    }

    fun isPlaying(messageId: String): Boolean {
        return currentPlayingMessageId == messageId && audioPlayer.isPlaying()
    }

    fun getCurrentPosition(): Int {
        return audioPlayer.getCurrentPosition()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAudio()
        if (audioRecorder.isRecording()) {
            audioRecorder.cancelRecording()
        }
    }
}
package com.example.aagnar.domain.model

data class VoiceMessageInfo(
    val duration: Int,
    val filePath: String? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val audioData: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VoiceMessageInfo

        if (duration != other.duration) return false
        if (filePath != other.filePath) return false
        if (isPlaying != other.isPlaying) return false
        if (currentPosition != other.currentPosition) return false
        if (!audioData.contentEquals(other.audioData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = duration
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + isPlaying.hashCode()
        result = 31 * result + currentPosition
        result = 31 * result + audioData.contentHashCode()
        return result
    }
}
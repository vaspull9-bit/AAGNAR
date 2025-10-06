package com.example.aagnar.domain.repository

interface SipRepository {
    suspend fun makeCall(uri: String)
    suspend fun answerCall()
    suspend fun endCall()
    suspend fun muteMicrophone()
    suspend fun unmuteMicrophone()
    suspend fun toggleSpeaker(isSpeakerOn: Boolean)
    suspend fun enableVideo()
    suspend fun disableVideo()
    suspend fun holdCall()
    suspend fun unholdCall()

}

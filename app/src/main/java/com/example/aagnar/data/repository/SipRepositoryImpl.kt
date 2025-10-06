package com.example.aagnar.data.repository

import com.example.aagnar.domain.repository.SipRepository
import com.example.aagnar.domain.service.LinphoneService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SipRepositoryImpl @Inject constructor(
    private val linphoneService: LinphoneService
) : SipRepository {

    override suspend fun makeCall(uri: String) = withContext(Dispatchers.IO) {
        linphoneService.makeCall(uri)
    }

    override suspend fun answerCall() = withContext(Dispatchers.IO) {
        linphoneService.answerCall()
    }

    override suspend fun endCall() = withContext(Dispatchers.IO) {
        linphoneService.endCall()
    }

    override suspend fun muteMicrophone() = withContext(Dispatchers.IO) {
        linphoneService.toggleMute(true)
    }

    override suspend fun unmuteMicrophone() = withContext(Dispatchers.IO) {
        linphoneService.toggleMute(false)
    }

    override suspend fun toggleSpeaker(isSpeakerOn: Boolean) = withContext(Dispatchers.IO) {
        linphoneService.toggleSpeaker(isSpeakerOn)
    }

    override suspend fun enableVideo() = withContext(Dispatchers.IO) {
        linphoneService.toggleVideo(true)
    }

    override suspend fun disableVideo() = withContext(Dispatchers.IO) {
        linphoneService.toggleVideo(false)
    }

    override suspend fun holdCall() = withContext(Dispatchers.IO) {
        linphoneService.holdCall()
    }

    override suspend fun unholdCall() = withContext(Dispatchers.IO) {
        linphoneService.unholdCall()
    }
}
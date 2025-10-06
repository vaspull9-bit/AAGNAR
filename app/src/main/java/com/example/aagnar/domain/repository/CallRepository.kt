package com.example.aagnar.domain.repository

interface CallRepository {
    suspend fun makeCall(uri: String)
    suspend fun getCallHistory(): List<Any>
    suspend fun recordCall(callData: Any)
    suspend fun answerCall(callId: String)
    suspend fun endCall(callId: String)
}
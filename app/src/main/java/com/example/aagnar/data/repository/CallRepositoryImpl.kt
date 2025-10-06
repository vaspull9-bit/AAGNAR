package com.example.aagnar.data.repository

import com.example.aagnar.domain.repository.CallRepository

class CallRepositoryImpl : CallRepository {
    override suspend fun makeCall(uri: String) {
        println("Making call to $uri")
    }

    override suspend fun getCallHistory(): List<Any> {
        return emptyList()
    }

    override suspend fun recordCall(callData: Any) {
        println("Call recorded: $callData")
    }

    override suspend fun answerCall(callId: String) {
        println("Answering call: $callId")
    }

    override suspend fun endCall(callId: String) {
        println("Ending call: $callId")
    }
}
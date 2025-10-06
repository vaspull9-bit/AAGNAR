 //domain/usecase/CallUseCase.kt
 package com.example.aagnar.domain.usecase

 import com.example.aagnar.domain.repository.CallRepository
 import javax.inject.Inject

 class CallUseCase @Inject constructor(
     private val callRepository: CallRepository
 ) {
     suspend fun makeCall(uri: String) = callRepository.makeCall(uri)

     suspend fun getCallHistory() = callRepository.getCallHistory()

     suspend fun recordCall(callData: Any) = callRepository.recordCall(callData)
 }
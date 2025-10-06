// domain/usecase/AccountUseCase.kt
package com.example.aagnar.domain.usecase

import com.example.aagnar.domain.repository.AccountRepository
import com.example.aagnar.domain.model.Account

class AccountUseCase(private val repository: AccountRepository) {
    suspend fun createAccount(account: Account) = repository.createAccount(account)
//    suspend fun getAccounts() = repository.getAccounts()
    suspend fun deleteAccount(accountId: Long) = repository.deleteAccount(accountId)
    suspend fun encryptPassword(password: String): String {
        // Реализация шифрования
        return password // TODO: Реальная имплементация
    }
}
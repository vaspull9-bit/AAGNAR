package com.example.aagnar.domain.repository

import com.example.aagnar.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getActiveAccounts(): Flow<List<Account>>
    fun getAllAccounts(): Flow<List<Account>>
    suspend fun getAccountById(accountId: Long): Account?
    suspend fun createAccount(account: Account): Long
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(accountId: Long)
}
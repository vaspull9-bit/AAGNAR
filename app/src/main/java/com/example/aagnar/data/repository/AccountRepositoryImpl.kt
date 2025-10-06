// data/repository/AccountRepositoryImpl.kt
package com.example.aagnar.data.repository
import com.example.aagnar.domain.repository.AccountRepository
import com.example.aagnar.data.local.entity.AccountEntity
import com.example.aagnar.data.local.entity.toEntity
import com.example.aagnar.data.local.dao.AccountDao
import com.example.aagnar.domain.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getActiveAccounts(): Flow<List<Account>> {
        return accountDao.getActiveAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAccountById(accountId: Long): Account? {
        return accountDao.getAccountById(accountId)?.toDomain()
    }

    override suspend fun createAccount(account: Account): Long {
        return accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun deleteAccount(accountId: Long) {
        accountDao.deleteAccount(accountId)
    }
}
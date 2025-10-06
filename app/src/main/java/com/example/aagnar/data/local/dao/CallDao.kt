package com.example.aagnar.data.local.dao

import androidx.room.*
import com.example.aagnar.data.local.entity.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY startTime DESC")
    fun getCallHistory(): Flow<List<CallEntity>>

    @Insert
    suspend fun insertCall(call: CallEntity): Long

    @Update
    suspend fun updateCall(call: CallEntity)

    @Query("DELETE FROM calls WHERE id = :callId")
    suspend fun deleteCall(callId: Long)
}
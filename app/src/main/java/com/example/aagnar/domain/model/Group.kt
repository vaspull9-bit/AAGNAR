package com.example.aagnar.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "group") // ← ДОЛЖНО СОВПАДАТЬ С "group" в DAO
data class Group(
    @PrimaryKey
    val id: String,
    val name: String,
    val creator: String,
    val members: List<String>,
    val admins: List<String>,
    val createdAt: Date,
    val lastMessage: String? = null,
    val lastMessageTime: Date? = null,
    val unreadCount: Int = 0,
    val avatarUrl: String? = null
)
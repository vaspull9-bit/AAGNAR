// domain/model/Contact.kt
package com.example.aagnar.domain.model

data class Contact(
    val id: Long = 0,
    val name: String,
    val address: String,
    val isOnline: Boolean = false,
    val lastSeen: Long = 0,
    val status: Status = Status.OFFLINE  // ← ДОБАВИТЬ

)

enum class Status {
    ONLINE, OFFLINE, UNREGISTERED
}
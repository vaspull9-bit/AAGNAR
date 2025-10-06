package com.example.aagnar.domain.repository

interface ContactRepository {
    suspend fun searchContacts(query: String): List<Any>
    suspend fun addContact(contactData: Any)
    suspend fun getContacts(): List<Any>
}
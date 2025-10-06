package com.example.aagnar.data.repository

import com.example.aagnar.domain.repository.ContactRepository

class ContactRepositoryImpl : ContactRepository {
    override suspend fun searchContacts(query: String): List<Any> {
        return emptyList()
    }

    override suspend fun addContact(contactData: Any) {
        println("Contact added: $contactData")
    }

    override suspend fun getContacts(): List<Any> {
        return emptyList()
    }
}
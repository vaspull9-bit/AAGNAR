// domain/usecase/ContactUseCase.kt
package com.example.aagnar.domain.usecase

import com.example.aagnar.domain.repository.ContactRepository
import javax.inject.Inject

class ContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend fun searchContacts(query: String) = contactRepository.searchContacts(query)
    suspend fun addContact(contactData: Any) = contactRepository.addContact(contactData)
    suspend fun getContacts() = contactRepository.getContacts()
}
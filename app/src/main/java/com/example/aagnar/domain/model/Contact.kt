// domain/model/Contact.kt
data class Contact(
    val id: Long = 0,
    val name: String,
    val address: String, // SIP URI или IAX номер
    val isOnline: Boolean = false,
    val lastSeen: Long = 0
)
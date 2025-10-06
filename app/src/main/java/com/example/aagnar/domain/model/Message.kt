// domain/model/Message.kt
data class Message(
    val id: Long = 0,
    val contactId: Long,
    val content: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val type: MessageType = MessageType.TEXT,
    val filePath: String? = null,
    val isDelivered: Boolean = false
)

enum class MessageType { TEXT, IMAGE, AUDIO, VIDEO, FILE }
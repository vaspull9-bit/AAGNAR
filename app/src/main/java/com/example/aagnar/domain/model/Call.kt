// domain/model/Call.kt
data class Call(
    val id: Long = 0,
    val contactId: Long,
    val direction: CallDirection,
    val status: CallStatus,
    val startTime: Long,
    val endTime: Long = 0,
    val isVideo: Boolean = false,
    val recordPath: String? = null
)

enum class CallDirection { INCOMING, OUTGOING }
enum class CallStatus { CONNECTING, ACTIVE, ENDED, MISSED }

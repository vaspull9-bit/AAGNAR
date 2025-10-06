
// app/src/main/java/com/example/aagnar/domain/model/Account.kt
package com.example.aagnar.domain.model

data class Account(
    val id: Long = 0,
    val username: String,
    val password: String,
    val server: String = "",  // ← УБРАТЬ ? сделать String
    val protocol: ProtocolType = ProtocolType.SIP,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ProtocolType {
    SIP, IAX2
}

// ДОБАВИТЬ недостающие enum классы:
enum class AudioCodec(val displayName: String) {
    OPUS("Opus"),
    SPEEX_8000("Speex 8000"),
    SPEEX_16000("Speex 16000"),
    SPEEX_32000("Speex 32000"),
    G711U("G.711u"),
    G711A("G.711a"),
    GSM("GSM"),
    G722("G.722")
}

enum class VideoCodec(val displayName: String) {
    VP8("VP8"),
    VP9("VP9"),
    H264("H.264")
}
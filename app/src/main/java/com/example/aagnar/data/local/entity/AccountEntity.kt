 //data/local/entity/AccountEntity.kt
package com.example.aagnar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.example.aagnar.data.local.converters.ProtocolTypeConverter
import com.example.aagnar.domain.model.ProtocolType

@Entity(
    tableName = "accounts",
    indices = [androidx.room.Index(value = ["username"], unique = true)]
)
@TypeConverters(ProtocolTypeConverter::class)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "server", defaultValue = "")
    val server: String = "",

    @ColumnInfo(name = "protocol", defaultValue = "SIP")
    val protocol: ProtocolType = ProtocolType.SIP,

    @ColumnInfo(name = "isEnabled", defaultValue = "1")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): com.example.aagnar.domain.model.Account {
        return com.example.aagnar.domain.model.Account(
            id = id,
            username = username,
            password = password,
            server = server,
            protocol = protocol,
            isEnabled = isEnabled,
            createdAt = createdAt
        )
    }
}

fun com.example.aagnar.domain.model.Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        username = username,
        password = password,
        server = server,
        protocol = protocol,
        isEnabled = isEnabled,
        createdAt = createdAt
    )
 }
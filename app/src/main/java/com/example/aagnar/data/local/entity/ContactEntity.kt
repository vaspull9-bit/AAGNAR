package com.example.aagnar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "contacts")
data class ContactEntity(
   @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

   @ColumnInfo(name = "name")
   val name: String,

  @ColumnInfo(name = "address")
 val address: String,

   @ColumnInfo(name = "isOnline", defaultValue = "0")
   val isOnline: Boolean = false,

    @ColumnInfo(name = "lastSeen", defaultValue = "0")
   val lastSeen: Long = 0
)
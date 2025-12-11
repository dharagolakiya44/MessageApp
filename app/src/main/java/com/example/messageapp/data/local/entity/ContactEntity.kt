package com.example.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val phone: String,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val isBlocked: Boolean = false
)


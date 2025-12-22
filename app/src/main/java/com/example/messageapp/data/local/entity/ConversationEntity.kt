package com.example.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.messageapp.domain.model.MessageStatus

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: Long,
    val contactId: Long,
    val contactName: String,
    val contactPhone: String,
    val contactOnline: Boolean,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val archived: Boolean,
    val lastStatus: MessageStatus,
    val hasFailedMessage: Boolean,
    val pinned: Boolean = false,
    val blocked: Boolean = false
)


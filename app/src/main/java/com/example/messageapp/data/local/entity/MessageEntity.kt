package com.example.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.messageapp.domain.model.MessageStatus

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val senderId: Long,
    val content: String,
    val timestamp: Long,
    val status: MessageStatus,
    val isOutgoing: Boolean
)


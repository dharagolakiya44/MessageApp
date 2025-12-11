package com.example.messageapp.domain.model

data class Conversation(
    val id: Long,
    val contact: Contact,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val isArchived: Boolean,
    val lastStatus: MessageStatus,
    val hasFailedMessage: Boolean
)


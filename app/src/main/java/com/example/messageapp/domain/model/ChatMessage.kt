package com.example.messageapp.domain.model

data class ChatMessage(
    val id: Long,
    val conversationId: Long,
    val senderId: Long,
    val content: String,
    val timestamp: Long,
    val status: MessageStatus,
    val isOutgoing: Boolean
)


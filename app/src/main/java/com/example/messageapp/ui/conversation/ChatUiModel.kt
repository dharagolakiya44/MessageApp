package com.example.messageapp.ui.conversation

import com.example.messageapp.domain.model.ChatMessage

sealed class ChatUiModel {
    data class MessageItem(val message: ChatMessage) : ChatUiModel()
    data class DateHeader(val date: String) : ChatUiModel()
}

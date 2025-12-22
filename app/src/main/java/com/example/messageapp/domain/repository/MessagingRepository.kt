package com.example.messageapp.domain.repository

import android.content.Context
import com.example.messageapp.domain.model.ChatMessage
import com.example.messageapp.domain.model.Contact
import com.example.messageapp.domain.model.Conversation
import com.example.messageapp.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface MessagingRepository {
    fun observeConversations(): Flow<List<Conversation>>
    fun observeArchivedConversations(): Flow<List<Conversation>>
    fun observeBlockedConversations(): Flow<List<Conversation>>
    fun observeConversation(conversationId: Long): Flow<Conversation?>
    fun observeMessages(conversationId: Long): Flow<List<ChatMessage>>
    fun observeContacts(): Flow<List<Contact>>
    fun getScheduledMessages(): Flow<List<MessageEntity>>

    suspend fun sendMessage(conversationId: Long, content: String): ChatMessage
    suspend fun scheduleMessage(conversationId: Long, content: String, timestamp: Long): ChatMessage
    suspend fun retryMessage(messageId: Long)
    suspend fun archiveConversation(conversationId: Long)
    suspend fun unarchiveConversation(conversationId: Long)
    suspend fun deleteConversation(conversationId: Long)
    suspend fun markAllAsRead()
    suspend fun markConversationRead(conversationId: Long)
    suspend fun getOrCreateConversation(contactId: Long): Long
    suspend fun syncDeviceContacts(context: Context)

    suspend fun markConversationUnread(conversationId: Long)
    suspend fun pinConversation(conversationId: Long, isPinned: Boolean)
    suspend fun blockConversation(conversationId: Long, isBlocked: Boolean)
}


package com.example.messageapp.domain.repository

import com.example.messageapp.domain.model.ChatMessage
import com.example.messageapp.domain.model.Contact
import com.example.messageapp.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface MessagingRepository {
    fun observeConversations(): Flow<List<Conversation>>
    fun observeArchivedConversations(): Flow<List<Conversation>>
    fun observeConversation(conversationId: Long): Flow<Conversation?>
    fun observeMessages(conversationId: Long): Flow<List<ChatMessage>>
    fun observeContacts(): Flow<List<Contact>>

    suspend fun sendMessage(conversationId: Long, content: String): ChatMessage
    suspend fun retryMessage(messageId: Long)
    suspend fun archiveConversation(conversationId: Long)
    suspend fun unarchiveConversation(conversationId: Long)
    suspend fun markAllAsRead()
    suspend fun markConversationRead(conversationId: Long)
    suspend fun getOrCreateConversation(contactId: Long): Long
}


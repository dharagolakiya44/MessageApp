package com.example.messageapp.data.mapper

import com.example.messageapp.data.local.entity.ContactEntity
import com.example.messageapp.data.local.entity.ConversationEntity
import com.example.messageapp.data.local.entity.MessageEntity
import com.example.messageapp.domain.model.ChatMessage
import com.example.messageapp.domain.model.Contact
import com.example.messageapp.domain.model.Conversation

fun ContactEntity.toDomain(): Contact = Contact(
    id = id,
    name = name,
    phone = phone,
    isOnline = isOnline,
    lastSeen = lastSeen
)

fun ConversationEntity.toDomain(contact: Contact = Contact(contactId, contactName, contactPhone, contactOnline)): Conversation =
    Conversation(
        id = id,
        contact = contact,
        lastMessage = if (lastMessage == "Say hello 👋") "" else lastMessage,
        lastTimestamp = lastTimestamp,
        unreadCount = unreadCount,
        isArchived = archived,
        lastStatus = lastStatus,
        hasFailedMessage = hasFailedMessage,
        isPinned = pinned,
        isBlocked = blocked
    )

fun MessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    content = content,
    timestamp = timestamp,
    status = status,
    isOutgoing = isOutgoing
)


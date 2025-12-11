package com.example.messageapp.data.local

import com.example.messageapp.data.local.entity.ContactEntity
import com.example.messageapp.data.local.entity.ConversationEntity
import com.example.messageapp.data.local.entity.MessageEntity
import com.example.messageapp.domain.model.MessageStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FakeDataSeeder {
    suspend fun seed(db: AppDatabase) = withContext(Dispatchers.IO) {
        val contactDao = db.contactDao()
        val conversationDao = db.conversationDao()
        val messageDao = db.messageDao()

        val existingContacts = contactDao.getContact(1)
        if (existingContacts != null) return@withContext

        val contacts = listOf(
            ContactEntity(1, "Alex Johnson", "+1234567890", isOnline = true),
            ContactEntity(2, "Priya Singh", "+1987654321", isOnline = false),
            ContactEntity(3, "Work Support", "+441234567890", isOnline = true),
            ContactEntity(4, "Delivery Agent", "+491234567890", isOnline = false),
            ContactEntity(5, "Jamie Lee", "+33123456789", isOnline = true)
        )

        val now = System.currentTimeMillis()
        val conversations = listOf(
            ConversationEntity(
                id = 100,
                contactId = 1,
                contactName = "Alex Johnson",
                contactPhone = "+1234567890",
                contactOnline = true,
                lastMessage = "Let's catch up later today!",
                lastTimestamp = now - 3_600_000,
                unreadCount = 2,
                archived = false,
                lastStatus = MessageStatus.DELIVERED,
                hasFailedMessage = false
            ),
            ConversationEntity(
                id = 101,
                contactId = 2,
                contactName = "Priya Singh",
                contactPhone = "+1987654321",
                contactOnline = false,
                lastMessage = "Presentation slides updated.",
                lastTimestamp = now - 86_400_000,
                unreadCount = 0,
                archived = false,
                lastStatus = MessageStatus.READ,
                hasFailedMessage = false
            ),
            ConversationEntity(
                id = 102,
                contactId = 3,
                contactName = "Work Support",
                contactPhone = "+441234567890",
                contactOnline = true,
                lastMessage = "Can you review the ticket?",
                lastTimestamp = now - 2_400_000,
                unreadCount = 3,
                archived = false,
                lastStatus = MessageStatus.SENT,
                hasFailedMessage = true
            ),
            ConversationEntity(
                id = 103,
                contactId = 4,
                contactName = "Delivery Agent",
                contactPhone = "+491234567890",
                contactOnline = false,
                lastMessage = "Order is arriving soon.",
                lastTimestamp = now - 172_800_000,
                unreadCount = 0,
                archived = true,
                lastStatus = MessageStatus.DELIVERED,
                hasFailedMessage = false
            )
        )

        val messages = listOf(
            MessageEntity(
                id = 1,
                conversationId = 100,
                senderId = 1,
                content = "Hi! Long time no see.",
                timestamp = now - 10_000_000,
                status = MessageStatus.READ,
                isOutgoing = false
            ),
            MessageEntity(
                id = 2,
                conversationId = 100,
                senderId = 0,
                content = "Absolutely, let's meet up later.",
                timestamp = now - 9_000_000,
                status = MessageStatus.DELIVERED,
                isOutgoing = true
            ),
            MessageEntity(
                id = 3,
                conversationId = 102,
                senderId = 0,
                content = "Sharing the ticket details now.",
                timestamp = now - 2_000_000,
                status = MessageStatus.FAILED,
                isOutgoing = true
            ),
            MessageEntity(
                id = 4,
                conversationId = 102,
                senderId = 3,
                content = "Please check the logs.",
                timestamp = now - 1_800_000,
                status = MessageStatus.SENT,
                isOutgoing = false
            )
        )

        contactDao.insertAll(contacts)
        conversationDao.insertAll(conversations)
        messageDao.insertAll(messages)

        // Add a few more messages to the archived conversation for realism.
        val archivedMessages = (0 until 6).map { index ->
            MessageEntity(
                conversationId = 103,
                senderId = if (index % 2 == 0) 0 else 4,
                content = "Archived message #$index",
                timestamp = now - (200_000L * index),
                status = MessageStatus.READ,
                isOutgoing = index % 2 == 0
            )
        }
        messageDao.insertAll(archivedMessages)
        // Add a varied stream for conversation 101
        val scheduledMessages = (1..5).map { index ->
            MessageEntity(
                conversationId = 101,
                senderId = if (index % 2 == 0) 0 else 2,
                content = "Update #$index",
                timestamp = now - (500_000L * index),
                status = MessageStatus.DELIVERED,
                isOutgoing = index % 2 == 0
            )
        }
        messageDao.insertAll(scheduledMessages)
    }
}


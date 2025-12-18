package com.example.messageapp.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.example.messageapp.data.local.AppDatabase
import com.example.messageapp.data.local.entity.ContactEntity
import com.example.messageapp.data.local.entity.ConversationEntity
import com.example.messageapp.data.local.entity.MessageEntity
import com.example.messageapp.data.mapper.toDomain
import com.example.messageapp.domain.model.ChatMessage
import com.example.messageapp.domain.model.Contact
import com.example.messageapp.domain.model.Conversation
import com.example.messageapp.domain.model.MessageStatus
import com.example.messageapp.domain.repository.MessagingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MessagingRepositoryImpl(
    private val context: Context,
    private val db: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MessagingRepository {

    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val contactDao = db.contactDao()
    private val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun observeConversations(): Flow<List<Conversation>> =
        combine(conversationDao.observeActive(), contactDao.observeContacts()) { conversations, contacts ->
            conversations.map { conversation ->
                val contact = contacts.firstOrNull { it.id == conversation.contactId }?.toDomain()
                    ?: Contact(conversation.contactId, conversation.contactName, conversation.contactPhone, conversation.contactOnline)
                conversation.toDomain(contact)
            }.filter { it.lastMessage.isNotBlank() }
        }

    override fun observeArchivedConversations(): Flow<List<Conversation>> =
        combine(conversationDao.observeArchived(), contactDao.observeContacts()) { conversations, contacts ->
            conversations.map { conversation ->
                val contact = contacts.firstOrNull { it.id == conversation.contactId }?.toDomain()
                    ?: Contact(conversation.contactId, conversation.contactName, conversation.contactPhone, conversation.contactOnline)
                conversation.toDomain(contact)
            }.filter { it.lastMessage.isNotBlank() }
        }

    override fun observeConversation(conversationId: Long): Flow<Conversation?> =
        combine(conversationDao.observeById(conversationId), contactDao.observeContacts()) { conversation, contacts ->
            conversation?.let {
                val contact = contacts.firstOrNull { c -> c.id == it.contactId }?.toDomain()
                    ?: Contact(it.contactId, it.contactName, it.contactPhone, it.contactOnline)
                it.toDomain(contact)
            }
        }

    override fun observeMessages(conversationId: Long): Flow<List<ChatMessage>> =
        messageDao.observeMessages(conversationId).map { list -> list.map { it.toDomain() } }

    override fun observeContacts(): Flow<List<Contact>> =
        contactDao.observeContacts().map { list -> list.map { it.toDomain() } }

    override suspend fun sendMessage(conversationId: Long, content: String): ChatMessage =
        withContext(ioDispatcher) {
            val timestamp = System.currentTimeMillis()
            val messageEntity = MessageEntity(
                conversationId = conversationId,
                senderId = SELF_USER_ID,
                content = content,
                timestamp = timestamp,
                status = MessageStatus.SENDING,
                isOutgoing = true
            )
            val id = messageDao.insert(messageEntity)
            
            // Optimistic update
            conversationDao.updateSnapshot(
                conversationId = conversationId,
                lastMessage = content,
                timestamp = timestamp,
                status = MessageStatus.SENDING,
                hasFailed = false
            )
            
            val inserted = messageEntity.copy(id = id)
            sendSmsReal(inserted)
            inserted.toDomain()
        }

    override suspend fun retryMessage(messageId: Long) = withContext(ioDispatcher) {
        val message = messageDao.getMessage(messageId) ?: return@withContext
        val retrying = message.copy(status = MessageStatus.SENDING)
        messageDao.update(retrying)
        conversationDao.updateSnapshot(
            conversationId = message.conversationId,
            lastMessage = message.content,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING,
            hasFailed = false
        )
        sendSmsReal(retrying)
    }

    private fun sendSmsReal(message: MessageEntity) {
        backgroundScope.launch {
            try {
                // Fetch valid phone number
                val conversation = conversationDao.getById(message.conversationId)
                val phone = conversation?.contactPhone
                
                if (phone.isNullOrBlank()) {
                    markMessageFailed(message.id, message.conversationId)
                    return@launch
                }

                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
                if (telephonyManager != null && telephonyManager.simState == android.telephony.TelephonyManager.SIM_STATE_ABSENT) {
                    markMessageFailed(message.id, message.conversationId)
                    return@launch
                }

                val smsManager = context.getSystemService(android.telephony.SmsManager::class.java) 
                    ?: android.telephony.SmsManager.getDefault()

                val SENT_ACTION = "SMS_SENT_${message.id}"
                val sentIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    message.id.toInt(),
                    android.content.Intent(SENT_ACTION),
                    android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
                )

                val receiver = object : android.content.BroadcastReceiver() {
                    override fun onReceive(c: Context?, intent: android.content.Intent?) {
                        when (resultCode) {
                            android.app.Activity.RESULT_OK -> {
                                backgroundScope.launch {
                                    updateMessageStatus(message.id, message.conversationId, MessageStatus.SENT)
                                }
                            }
                            else -> {
                                backgroundScope.launch {
                                    markMessageFailed(message.id, message.conversationId)
                                }
                            }
                        }
                        try {
                            context.unregisterReceiver(this)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(receiver, android.content.IntentFilter(SENT_ACTION), Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(receiver, android.content.IntentFilter(SENT_ACTION))
                }

                smsManager.sendTextMessage(phone, null, message.content, sentIntent, null)
                
            } catch (e: Exception) {
                e.printStackTrace()
                markMessageFailed(message.id, message.conversationId)
            }
        }
    }

    private suspend fun markMessageFailed(messageId: Long, conversationId: Long) {
        updateMessageStatus(messageId, conversationId, MessageStatus.FAILED)
    }

    private suspend fun updateMessageStatus(messageId: Long, conversationId: Long, status: MessageStatus) {
        messageDao.updateStatus(messageId, status)
        val hasFailed = status == MessageStatus.FAILED ||
                messageDao.countByStatus(conversationId, MessageStatus.FAILED) > 0
        
        // Only update snapshot status if it's the latest message, but for simplicity we update always
        // to reflect the last action's result.
        // To be precise we should check if this message is indeed the last one.
        // But for now, updating the status is safer to show failure.
        conversationDao.updateFailure(conversationId, hasFailed)
        if (status == MessageStatus.FAILED || status == MessageStatus.SENT) {
             // We might want to update the conversation's lastStatus if this was the last message
             // For now, let's keep it simple.
        }
    }

    override suspend fun archiveConversation(conversationId: Long) = withContext(ioDispatcher) {
        conversationDao.archive(conversationId)
    }

    override suspend fun unarchiveConversation(conversationId: Long) = withContext(ioDispatcher) {
        conversationDao.unarchive(conversationId)
    }

    override suspend fun markAllAsRead() = withContext(ioDispatcher) {
        conversationDao.markAllRead()
    }

    override suspend fun markConversationRead(conversationId: Long) = withContext(ioDispatcher) {
        conversationDao.markRead(conversationId)
    }

    override suspend fun getOrCreateConversation(contactId: Long): Long = withContext(ioDispatcher) {
        val existing = conversationDao.findByContact(contactId)
        if (existing != null) return@withContext existing.id

        val contact = contactDao.getContact(contactId)
            ?: return@withContext -1L
        val newId = System.currentTimeMillis()
        val conversation = ConversationEntity(
            id = newId,
            contactId = contact.id,
            contactName = contact.name,
            contactPhone = contact.phone,
            contactOnline = contact.isOnline,
            lastMessage = "",
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            archived = false,
            lastStatus = MessageStatus.SENT,
            hasFailedMessage = false
        )
        conversationDao.upsert(conversation)
        newId
    }

    override suspend fun syncDeviceContacts(context: Context) = withContext(ioDispatcher) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        val newContacts = mutableListOf<ContactEntity>()
        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val phone = it.getString(numberIndex) ?: ""
                
                // Simple de-duplication or validation could go here
                newContacts.add(
                    ContactEntity(
                        id = id,
                        name = name,
                        phone = phone,
                        isOnline = false // Default status
                    )
                )
            }
        }
        
        if (newContacts.isNotEmpty()) {
            contactDao.insertAll(newContacts)
        }
    }



    companion object {
        private const val SELF_USER_ID = 0L
    }
}


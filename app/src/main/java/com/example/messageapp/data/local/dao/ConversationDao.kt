package com.example.messageapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.messageapp.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE archived = 0 ORDER BY lastTimestamp DESC")
    fun observeActive(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE archived = 1 ORDER BY lastTimestamp DESC")
    fun observeArchived(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE contactId = :contactId LIMIT 1")
    suspend fun findByContact(contactId: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("UPDATE conversations SET archived = 1 WHERE id = :conversationId")
    suspend fun archive(conversationId: Long)

    @Query("UPDATE conversations SET archived = 0 WHERE id = :conversationId")
    suspend fun unarchive(conversationId: Long)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteById(conversationId: Long)

    @Query("UPDATE conversations SET unreadCount = 0")
    suspend fun markAllRead()

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun markRead(conversationId: Long)

    @Query(
        "UPDATE conversations SET lastMessage = :lastMessage, lastTimestamp = :timestamp, lastStatus = :status, hasFailedMessage = :hasFailed WHERE id = :conversationId"
    )
    suspend fun updateSnapshot(
        conversationId: Long,
        lastMessage: String,
        timestamp: Long,
        status: com.example.messageapp.domain.model.MessageStatus,
        hasFailed: Boolean
    )

    @Query("UPDATE conversations SET hasFailedMessage = :hasFailed WHERE id = :conversationId")
    suspend fun updateFailure(conversationId: Long, hasFailed: Boolean)
}


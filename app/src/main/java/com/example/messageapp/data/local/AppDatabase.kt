package com.example.messageapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.messageapp.data.local.dao.ContactDao
import com.example.messageapp.data.local.dao.ConversationDao
import com.example.messageapp.data.local.dao.MessageDao
import com.example.messageapp.data.local.entity.ContactEntity
import com.example.messageapp.data.local.entity.ConversationEntity
import com.example.messageapp.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [ContactEntity::class, ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "message_app.db"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                scope.launch { FakeDataSeeder.seed(instance) }
                instance
            }
        }
    }
}


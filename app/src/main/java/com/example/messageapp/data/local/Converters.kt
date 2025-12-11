package com.example.messageapp.data.local

import androidx.room.TypeConverter
import com.example.messageapp.domain.model.MessageStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): MessageStatus = MessageStatus.valueOf(value)
}


package com.example.messageapp.ui.scheduled

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.messageapp.data.local.entity.MessageEntity
import com.example.messageapp.domain.repository.MessagingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ScheduledViewModel(private val repository: MessagingRepository) : ViewModel() {

    // Ideally repository should have getScheduledMessages(), but I added it to Dao.
    // I need to update Repository interface and implementation.
    // For now, I will add it to Repository.
    
    val scheduledMessages: Flow<List<MessageEntity>> = repository.getScheduledMessages()

    class Factory(private val repository: MessagingRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScheduledViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ScheduledViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

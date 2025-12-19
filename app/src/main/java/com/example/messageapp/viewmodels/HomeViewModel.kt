package com.example.messageapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.messageapp.domain.model.Conversation
import com.example.messageapp.domain.repository.MessagingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MessagingRepository
) : ViewModel() {

    val conversations: StateFlow<List<Conversation>> =
        repository.observeConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun archiveConversation(id: Long) {
        viewModelScope.launch { repository.archiveConversation(id) }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch { repository.deleteConversation(id) }
    }

    fun markAllRead() {
        viewModelScope.launch { repository.markAllAsRead() }
    }

    class Factory(private val repository: MessagingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


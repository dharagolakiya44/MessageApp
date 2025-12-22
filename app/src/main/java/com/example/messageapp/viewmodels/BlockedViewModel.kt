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

class BlockedViewModel(
    private val repository: MessagingRepository
) : ViewModel() {

    val conversations: StateFlow<List<Conversation>> =
        repository.observeBlockedConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun unblockConversations(ids: List<Long>) {
        viewModelScope.launch {
            ids.forEach { repository.blockConversation(it, false) }
        }
    }

    fun deleteConversations(ids: List<Long>) {
        viewModelScope.launch {
            ids.forEach { repository.deleteConversation(it) }
        }
    }

    class Factory(private val repository: MessagingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BlockedViewModel::class.java)) {
                return BlockedViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

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

class ArchivedViewModel(
    private val repository: MessagingRepository
) : ViewModel() {

    val conversations: StateFlow<List<Conversation>> =
        repository.observeArchivedConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun unarchiveConversation(id: Long) {
        viewModelScope.launch { repository.unarchiveConversation(id) }
    }

    class Factory(private val repository: MessagingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArchivedViewModel::class.java)) {
                return ArchivedViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


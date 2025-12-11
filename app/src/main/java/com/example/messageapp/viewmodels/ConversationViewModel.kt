package com.example.messageapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.messageapp.domain.model.ChatMessage
import com.example.messageapp.domain.model.Conversation
import com.example.messageapp.domain.repository.MessagingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val conversationId: Long,
    private val repository: MessagingRepository
) : ViewModel() {

    val conversation: StateFlow<Conversation?> =
        repository.observeConversation(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages: StateFlow<List<ChatMessage>> =
        repository.observeMessages(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _inputEnabled = MutableStateFlow(true)
    val inputEnabled: StateFlow<Boolean> = _inputEnabled

    init {
        viewModelScope.launch { repository.markConversationRead(conversationId) }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        _inputEnabled.value = false
        viewModelScope.launch {
            repository.sendMessage(conversationId, content.trim())
            _inputEnabled.value = true
        }
    }

    fun retry(messageId: Long) {
        viewModelScope.launch { repository.retryMessage(messageId) }
    }

    class Factory(
        private val conversationId: Long,
        private val repository: MessagingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
                return ConversationViewModel(conversationId, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


package com.example.messageapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.messageapp.domain.model.ChatMessage
import com.example.messageapp.domain.model.Conversation
import com.example.messageapp.domain.repository.MessagingRepository
import com.example.messageapp.ui.conversation.ChatUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val conversationId: Long,
    private val repository: MessagingRepository
) : ViewModel() {

    val conversation: StateFlow<Conversation?> =
        repository.observeConversation(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val messagesList: StateFlow<List<ChatMessage>> =
        repository.observeMessages(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiList: StateFlow<List<ChatUiModel>> = messagesList.map { messages ->
        val result = mutableListOf<ChatUiModel>()
        var lastDate = ""
        
        messages.forEach { message ->
            val date = com.example.messageapp.utils.formatDateHeader(message.timestamp) 
            if (date != lastDate) {
                result.add(ChatUiModel.DateHeader(date))
                lastDate = date
            }
            result.add(ChatUiModel.MessageItem(message))
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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


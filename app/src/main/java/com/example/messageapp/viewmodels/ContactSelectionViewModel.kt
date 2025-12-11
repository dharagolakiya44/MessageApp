package com.example.messageapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.messageapp.domain.model.Contact
import com.example.messageapp.domain.repository.MessagingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactSelectionViewModel(
    private val repository: MessagingRepository
) : ViewModel() {

    val contacts: StateFlow<List<Contact>> =
        repository.observeContacts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _createdConversationId = MutableStateFlow<Long?>(null)
    val createdConversationId: StateFlow<Long?> = _createdConversationId

    fun startConversation(contactId: Long) {
        viewModelScope.launch {
            val id = repository.getOrCreateConversation(contactId)
            if (id != -1L) {
                _createdConversationId.value = id
            }
        }
    }

    fun resetNavigation() {
        _createdConversationId.value = null
    }

    class Factory(private val repository: MessagingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactSelectionViewModel::class.java)) {
                return ContactSelectionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


package com.example.messageapp.domain.model

data class Contact(
    val id: Long,
    val name: String,
    val phone: String,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null
)


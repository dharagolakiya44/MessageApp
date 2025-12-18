package com.example.messageapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatMessageTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateHeader(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneDay = 24 * 60 * 60 * 1000L
    
    return when {
        diff < oneDay && isSameDay(now, timestamp) -> "Today"
        diff < 2 * oneDay && isSameDay(now - oneDay, timestamp) -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return sdf.format(Date(t1)) == sdf.format(Date(t2))
}

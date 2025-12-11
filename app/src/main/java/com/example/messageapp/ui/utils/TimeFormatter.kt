package com.example.messageapp.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(time: Long): String {
    val now = System.currentTimeMillis()
    val oneDay = 24 * 60 * 60 * 1000
    val pattern = if (now - time < oneDay) "HH:mm" else "dd MMM"
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(time))
}


package com.example.messageapp.utils

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.ChecksSdkIntAtLeast


fun Context.showToast(strMessage: String?) {
    Toast.makeText(
        this,
        strMessage,
        Toast.LENGTH_SHORT
    ).show()
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isTiramisuPlusNew() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

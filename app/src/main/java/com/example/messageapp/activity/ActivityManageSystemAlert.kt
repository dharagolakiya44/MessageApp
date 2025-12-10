package com.example.messageapp.activity

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.messageapp.databinding.ActivityManageSystemAlertBinding
import com.example.messageapp.dialog.DialogSystemAlertWindow
import com.example.messageapp.extention.viewBinding
import kotlin.getValue

class ActivityManageSystemAlert : AppCompatActivity() {
    private val binding by viewBinding(ActivityManageSystemAlertBinding::inflate)
    private var dialogSystemAlertWindow: DialogSystemAlertWindow? = null
    private val calendarHandler = Handler(Looper.myLooper()!!)
    private val alertWindowTimeoutMillis = 6000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val dialogSystemAlertWindow = DialogSystemAlertWindow(this)
        this@ActivityManageSystemAlert.dialogSystemAlertWindow = dialogSystemAlertWindow

        with(this@ActivityManageSystemAlert.dialogSystemAlertWindow!!) {
            setCanceledOnTouchOutside(true)
            setCancelable(true)
            setOnDismissListener(dialogOnDismissListener)
        }

        if (!isFinishing && this@ActivityManageSystemAlert.dialogSystemAlertWindow != null) {
            this@ActivityManageSystemAlert.dialogSystemAlertWindow?.show()
        }
        calendarHandler.postDelayed(dialogRunnable, alertWindowTimeoutMillis)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@ActivityManageSystemAlert.dialogSystemAlertWindow?.dismiss()
            }
        })

    }

    private val dialogOnDismissListener = DialogInterface.OnDismissListener { finish() }

    private val dialogRunnable = Runnable { dialogSystemAlertWindow?.dismiss() }

    override fun onDestroy() {
        super.onDestroy()
        dialogSystemAlertWindow?.dismiss()
    }
}
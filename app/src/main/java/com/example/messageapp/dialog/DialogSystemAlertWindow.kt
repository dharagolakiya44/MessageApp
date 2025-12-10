package com.example.messageapp.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import com.example.messageapp.R
import com.example.messageapp.databinding.DialogSystemAlertWindowBinding

class DialogSystemAlertWindow(
    activityContext: Context,
) : Dialog(activityContext, R.style.DefaultAnimationTheme) {

    override fun onCreate(configurationBundle: Bundle?) {
        super.onCreate(configurationBundle)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window!!.setGravity(Gravity.BOTTOM)
        val bottomWindowAttributes = window!!.attributes as WindowManager.LayoutParams
        window!!.attributes = bottomWindowAttributes
        val dialogSystemAlertWindowBinding = DialogSystemAlertWindowBinding.inflate(layoutInflater)
        setContentView(dialogSystemAlertWindowBinding.root)
//        val lottieView = dialogSystemAlertWindowBinding.displayOverTheAppLottieAnimationView
//        lottieView.setAnimation( R.raw.calendar_system_alert_raw)

        dialogSystemAlertWindowBinding.root.setOnTouchListener { _, event ->
            dismiss()
            false
        }

    }
} 
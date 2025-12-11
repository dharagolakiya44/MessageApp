package com.example.messageapp.activity

import android.os.Bundle
import com.example.messageapp.databinding.ActivitySelectThemeModeBinding
import com.example.messageapp.extention.viewBinding
import com.example.messageapp.ui.common.BaseActivity

class SelectThemeModeActivity : BaseActivity() {

    private val binding by viewBinding(ActivitySelectThemeModeBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}
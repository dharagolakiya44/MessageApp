package com.example.messageapp.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.messageapp.R
import com.example.messageapp.databinding.ActivitySelectThemeModeBinding
import com.example.messageapp.databinding.ActivitySettingBinding
import com.example.messageapp.databinding.ActivitySettingBinding.inflate
import com.example.messageapp.extention.viewBinding
import kotlin.getValue

class SelectThemeModeActivity : BaseActivity() {

    private val binding by viewBinding(ActivitySelectThemeModeBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}
package com.example.messageapp.ui.afterCall

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.messageapp.R
import com.example.messageapp.data.PreferenceHelper
import com.example.messageapp.databinding.ActivitySettingBinding
import com.example.messageapp.databinding.ActivitySettingBinding.inflate
import com.example.messageapp.extention.viewBinding
import kotlin.getValue

class AfterCallActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivitySettingBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setToolbar()
    }

    private fun setToolbar() {
        binding.incToolbar.tvTitle.text = getString(R.string.after_call_feature)

        binding.incToolbar.ivMenu.visibility = View.GONE
        binding.incToolbar.ivSearch.visibility = View.GONE
        binding.incToolbar.ivBack.visibility = View.VISIBLE
        binding.incToolbar.icDone.visibility = View.GONE

        binding.incToolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}
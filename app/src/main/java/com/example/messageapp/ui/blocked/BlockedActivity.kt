package com.example.messageapp.ui.blocked

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.messageapp.databinding.FragmentPlaceholderBinding

class BlockedActivity : AppCompatActivity() {

    private lateinit var binding: FragmentPlaceholderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPlaceholderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Blocked"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.textPlaceholder.text = "Blocked contacts will appear here."
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

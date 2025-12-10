package com.example.messageapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.messageapp.R
import com.example.messageapp.adapter.MessageAdapter
import com.example.messageapp.databinding.ActivityMainBinding
import com.example.messageapp.extention.viewBinding
import com.example.messageapp.model.Message

class MainActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setAdapter()

    }

    private fun setAdapter() {
        binding.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val messages = listOf(
            Message("Samsung Helpline", "Hhj", "05 Dec")
        )

        binding.recyclerView.adapter = MessageAdapter(messages)
    }

}
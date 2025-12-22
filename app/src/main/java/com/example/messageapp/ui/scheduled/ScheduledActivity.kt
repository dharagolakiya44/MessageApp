package com.example.messageapp.ui.scheduled

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messageapp.Controller
import com.example.messageapp.databinding.ActivityScheduledBinding
import com.example.messageapp.ui.common.BaseActivity
import com.example.messageapp.ui.contacts.ContactSelectionActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScheduledActivity : BaseActivity() {

    private lateinit var binding: ActivityScheduledBinding

    private val repository by lazy { (application as Controller).repository }
    private val viewModel: ScheduledViewModel by viewModels {
        ScheduledViewModel.Factory(repository)
    }

    private val adapter by lazy {
        ScheduledMessageAdapter(
            onDeleteClick = {
                // Handle Delete if needed, or open conversation
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduledBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun setupToolbar() {
        binding.incToolbar.tvTitle.text = "Schedule"
        binding.incToolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // Hide other toolbar items if not needed
        binding.incToolbar.ivMenu.isVisible = false
        binding.incToolbar.ivSearch.isVisible = false
         binding.incToolbar.ivBack.isVisible = true
    }

    private fun setupRecyclerView() {
        binding.recyclerScheduled.layoutManager = LinearLayoutManager(this)
        binding.recyclerScheduled.adapter = adapter
    }

    private fun setupFab() {
        binding.btnStartChat.setOnClickListener {
            val intent = Intent(this, ContactSelectionActivity::class.java).apply {
                putExtra("EXTRA_IS_SCHEDULED", true)
            }
            startActivity(intent)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.scheduledMessages.collectLatest { messages ->
                adapter.submitList(messages)
                binding.layoutEmpty.isVisible = messages.isEmpty()
                binding.recyclerScheduled.isVisible = messages.isNotEmpty()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

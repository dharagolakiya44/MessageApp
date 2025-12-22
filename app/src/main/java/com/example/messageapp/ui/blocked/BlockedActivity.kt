package com.example.messageapp.ui.blocked

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.messageapp.R
import com.example.messageapp.databinding.ActivityBlockedBinding
import com.example.messageapp.ui.adapters.ConversationAdapter
import com.example.messageapp.ui.common.BaseActivity
import com.example.messageapp.viewmodels.BlockedViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockedActivity : BaseActivity() {

    private lateinit var binding: ActivityBlockedBinding
    private val viewModel: BlockedViewModel by viewModels {
        BlockedViewModel.Factory((application as com.example.messageapp.Controller).repository)
    }
    
    private val adapter: ConversationAdapter by lazy {
        ConversationAdapter(
            onConversationClick = { conversation ->
                 // Typically blocked users can't be chatted with until unblocked.
            },
            onSelectionChanged = { count ->
                updateSelectionUi(count)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // supportActionBar?.title = "Blocked" // Removed
        // supportActionBar?.setDisplayHomeAsUpEnabled(true) // Removed

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupBottomActions()
        
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }
    
    private fun setupRecyclerView() {
        binding.recyclerBlocked.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.recyclerBlocked.adapter = adapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.conversations.collectLatest { list ->
                adapter.submitList(list)
                binding.layoutEmpty.isVisible = list.isEmpty()
                binding.recyclerBlocked.isVisible = list.isNotEmpty()
            }
        }
    }
    
    private fun setupBottomActions() {
        binding.actionDelete.setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                viewModel.deleteConversations(selected)
                adapter.clearSelection()
            }
        }

        binding.actionUnblock.setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                viewModel.unblockConversations(selected)
                adapter.clearSelection()
            }
        }
    }

    private fun setupToolbar() {
        binding.incToolbar.ivBack.setOnClickListener {
            handleBackPress()
        }
        binding.incToolbar.tvTitle.text = "Blocked" // Or use string resource if available

        binding.incToolbar.ivBack.visibility = android.view.View.VISIBLE
        binding.incToolbar.ivMenu.visibility = android.view.View.GONE
        binding.incToolbar.ivSearch.visibility = android.view.View.GONE
    }

    private fun updateSelectionUi(count: Int) {
        if (count > 0) {
            binding.incToolbar.tvTitle.text = "$count Selected"
            binding.incToolbar.ivBack.setImageResource(R.drawable.ic_close)
            binding.layoutBottomActions.visibility = android.view.View.VISIBLE
        } else {
            binding.incToolbar.tvTitle.text = "Blocked"
            binding.incToolbar.ivBack.setImageResource(R.drawable.ic_back)
            binding.layoutBottomActions.visibility = android.view.View.GONE
        }
    }

    private fun handleBackPress() {
        if (adapter.selectionMode) {
            adapter.clearSelection()
        } else {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        handleBackPress()
        return true
    }
}

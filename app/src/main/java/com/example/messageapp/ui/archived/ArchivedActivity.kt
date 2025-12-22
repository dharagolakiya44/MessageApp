package com.example.messageapp.ui.archived

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.messageapp.Controller
import com.example.messageapp.R
import com.example.messageapp.ui.adapters.ConversationAdapter
import com.example.messageapp.databinding.FragmentArchivedBinding
import com.example.messageapp.ui.common.BaseActivity
import com.example.messageapp.ui.common.ConversationSwipeCallback
import com.example.messageapp.ui.conversation.ConversationActivity
import com.example.messageapp.viewmodels.ArchivedViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArchivedActivity : BaseActivity() {

    private lateinit var binding: FragmentArchivedBinding

    private val repository by lazy { (application as Controller).repository }
    private val viewModel: ArchivedViewModel by viewModels { ArchivedViewModel.Factory(repository) }

    private val adapter by lazy {
        ConversationAdapter(
            onConversationClick = { conversation ->
                val intent = Intent(this, ConversationActivity::class.java).apply {
                    putExtra("conversationId", conversation.id)
                    putExtra("contactName", conversation.contact.name)
                }
                startActivity(intent)
            },
            onSelectionChanged = { count ->
                updateSelectionUi(count)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentArchivedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })

        setupToolbar()
        setupBottomActions()

        binding.recyclerArchived.apply {
            adapter = this@ArchivedActivity.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@ArchivedActivity)
        }



        lifecycleScope.launch {
            viewModel.conversations.collectLatest { list ->
                adapter.submitList(list)
                binding.viewEmpty.isVisible = list.isEmpty()
            }
        }
    }

    private fun setupToolbar() {
        binding.incToolbar.ivBack.setOnClickListener {
            handleBackPress()
        }
        binding.incToolbar.tvTitle.text = getString(R.string.archived)

        binding.incToolbar.ivBack.visibility= View.VISIBLE
        binding.incToolbar.ivMenu.visibility= View.GONE
        binding.incToolbar.ivSearch.visibility= View.GONE
    }
    
    private fun setupBottomActions() {
        binding.actionDelete.setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                viewModel.deleteConversations(selected)
                adapter.clearSelection()
            }
        }
        
        binding.actionUnarchive.setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                viewModel.unarchiveConversations(selected)
                adapter.clearSelection()
            }
        }
    }
    
    private fun updateSelectionUi(count: Int) {
        if (count > 0) {
            binding.incToolbar.tvTitle.text = "$count Selected"
            binding.incToolbar.ivBack.setImageResource(R.drawable.ic_close)
            binding.layoutBottomActions.visibility = View.VISIBLE
        } else {
            binding.incToolbar.tvTitle.text = getString(R.string.archived)
            binding.incToolbar.ivBack.setImageResource(R.drawable.ic_back)
            binding.layoutBottomActions.visibility = View.GONE
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

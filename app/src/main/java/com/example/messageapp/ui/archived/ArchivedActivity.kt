package com.example.messageapp.ui.archived

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.messageapp.Controller
import com.example.messageapp.ui.adapters.ConversationAdapter
import com.example.messageapp.databinding.FragmentArchivedBinding
import com.example.messageapp.ui.common.SwipeToArchiveCallback
import com.example.messageapp.ui.conversation.ConversationActivity
import com.example.messageapp.viewmodels.ArchivedViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArchivedActivity : AppCompatActivity() {

    private lateinit var binding: FragmentArchivedBinding

    private val repository by lazy { (application as Controller).repository }
    private val viewModel: ArchivedViewModel by viewModels { ArchivedViewModel.Factory(repository) }

    private val adapter by lazy {
        ConversationAdapter { conversation ->
            val intent = Intent(this, ConversationActivity::class.java).apply {
                putExtra("conversationId", conversation.id)
                putExtra("contactName", conversation.contact.name)
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentArchivedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Reusing fragment layout effectively requires handling toolbar if present, 
        // or just wrapping content. FragmentArchivedBinding likely contains a recycler.
        // If it lacks a Toolbar, we might need a new layout. Assuming it's just content for now,
        // and add a back button support if possible.
        supportActionBar?.title = "Archived"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerArchived.apply {
            adapter = this@ArchivedActivity.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@ArchivedActivity)
        }
        
        ItemTouchHelper(
            SwipeToArchiveCallback(this) { position ->
                val conversation = adapter.getItemAt(position)
                viewModel.unarchiveConversation(conversation.id)
            }
        ).attachToRecyclerView(binding.recyclerArchived)

        lifecycleScope.launch {
            viewModel.conversations.collectLatest { list ->
                adapter.submitList(list)
                binding.viewEmpty.isVisible = list.isEmpty()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

package com.example.messageapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messageapp.Controller
import com.example.messageapp.R
import com.example.messageapp.databinding.ActivityMainBinding
import com.example.messageapp.extention.viewBinding
import com.example.messageapp.ui.adapters.ConversationAdapter
import com.example.messageapp.ui.archived.ArchivedActivity
import com.example.messageapp.ui.blocked.BlockedActivity
import com.example.messageapp.ui.common.SwipeToArchiveCallback
import com.example.messageapp.ui.contacts.ContactSelectionActivity
import com.example.messageapp.ui.conversation.ConversationActivity
import com.example.messageapp.ui.scheduled.ScheduledActivity
import com.example.messageapp.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val repository by lazy { (application as Controller).repository }
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory(repository) }

    private val conversationAdapter by lazy {
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
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupDrawer()
        setupHomeUi()
    }

    private fun setupDrawer() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu) // Assuming ic_menu exists or using default drawer icon
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.archivedFragment -> startActivity(Intent(this, ArchivedActivity::class.java))
                R.id.scheduledFragment -> startActivity(Intent(this, ScheduledActivity::class.java))
                R.id.blockedFragment -> startActivity(Intent(this, BlockedActivity::class.java))
                R.id.menu_mark_read -> viewModel.markAllRead()
                // Settings is ignored as requested
            }
            true // Close drawer on selection
        }
    }

    private fun setupHomeUi() {
        binding.recyclerConversations.apply {
            adapter = conversationAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        ItemTouchHelper(
            SwipeToArchiveCallback(this) { position ->
                val conversation = conversationAdapter.getItemAt(position)
                viewModel.archiveConversation(conversation.id)
            }
        ).attachToRecyclerView(binding.recyclerConversations)

        binding.fabStartChat.setOnClickListener {
            startActivity(Intent(this, ContactSelectionActivity::class.java))
        }

        lifecycleScope.launch {
            viewModel.conversations.collectLatest { list ->
                conversationAdapter.submitList(list)
                binding.viewEmpty.isVisible = list.isEmpty()
            }
        }
    }
}
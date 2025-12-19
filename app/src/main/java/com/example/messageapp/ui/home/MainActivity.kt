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
import com.example.messageapp.ui.common.BaseActivity
import com.example.messageapp.ui.common.ConversationSwipeCallback
import com.example.messageapp.ui.contacts.ContactSelectionActivity
import com.example.messageapp.ui.conversation.ConversationActivity
import com.example.messageapp.ui.scheduled.ScheduledActivity
import com.example.messageapp.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val repository by lazy { (application as Controller).repository }
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory(repository) }

    private val conversationAdapter by lazy {
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
        setContentView(binding.root)

        setupDrawer()
        setupToolbar()
        setupHomeUi()
        setupBottomActions()
        
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    private fun setupToolbar() {
        with(binding.incToolbar) {
            ivSearch.setOnClickListener {
                tvTitle.isVisible = false
                ivMenu.isVisible = false
                ivSearch.isVisible = false
                ivBack.isVisible = true
                etSearch.isVisible = true
                etSearch.requestFocus()
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }

            ivBack.setOnClickListener {
                if (conversationAdapter.selectionMode) {
                    conversationAdapter.clearSelection()
                } else if (etSearch.isVisible) {
                    etSearch.isVisible = false
                    etSearch.text?.clear()
                    ivBack.isVisible = false
                    ivMenu.isVisible = true
                    tvTitle.isVisible = true
                    ivSearch.isVisible = true
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
                }
            }

            tvTitle.text=getString(R.string.messages)
        }
    }
    
    private fun setupBottomActions() {
        binding.actionDelete.setOnClickListener {
            val selected = conversationAdapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                viewModel.deleteConversations(selected)
                conversationAdapter.clearSelection()
            }
        }

        binding.actionArchive.setOnClickListener {
            val selected = conversationAdapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                viewModel.archiveConversations(selected)
                conversationAdapter.clearSelection()
            }
        }
        
        binding.actionBlock.setOnClickListener {
            val selected = conversationAdapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                viewModel.blockConversations(selected)
                conversationAdapter.clearSelection()
            }
        }
        
        binding.actionMore.setOnClickListener { view ->
            showMoreMenu(view)
        }
    }

    private fun showMoreMenu(view: android.view.View) {
        val selectedIds = conversationAdapter.getSelectedItems()
        if (selectedIds.isEmpty()) return

        // We need to know the state of selected items to decide what to show
        // Ideally we should query the current list from the adapter or viewmodel
        // For simplicity, we can get items from adapter if exposed, or just show both/toggle blindly.
        // Let's retrieve items from adapter.
        
        val selectedItems = selectedIds.mapNotNull { id -> conversationAdapter.currentList.find { it.id == id } }
        
        val allPinned = selectedItems.all { it.isPinned }
        val allUnread = selectedItems.all { it.unreadCount > 0 }
        
        val popup = androidx.appcompat.widget.PopupMenu(this, view)
        
        val pinTitle = if (allPinned) getString(R.string.unpin) else getString(R.string.pin)
        val readTitle = if (allUnread) getString(R.string.mark_read) else getString(R.string.mark_unread)

        popup.menu.add(0, 1, 0, pinTitle)
        popup.menu.add(0, 2, 0, readTitle)
        
        popup.setOnMenuItemClickListener { item ->
            val finalSelected = conversationAdapter.getSelectedItems()
            if (finalSelected.isNotEmpty()) {
                when (item.itemId) {
                    1 -> viewModel.pinConversations(finalSelected, !allPinned)
                    2 -> {
                        if (allUnread) viewModel.markAsRead(finalSelected)
                        else viewModel.markAsUnread(finalSelected)
                    }
                }
                conversationAdapter.clearSelection()
            }
            true
        }
        popup.show()
    }
    
    private fun updateSelectionUi(count: Int) {
        if (count > 0) {
            binding.incToolbar.tvTitle.text = "$count Selected"
            binding.incToolbar.ivBack.setImageResource(R.drawable.ic_close)
            binding.incToolbar.ivBack.visibility = android.view.View.VISIBLE
            binding.incToolbar.ivMenu.visibility = android.view.View.GONE
            binding.incToolbar.ivSearch.visibility = android.view.View.GONE
            
            binding.layoutBottomActions.visibility = android.view.View.VISIBLE
            binding.fabStartChat.visibility = android.view.View.GONE
        } else {
            binding.incToolbar.tvTitle.text = getString(R.string.messages)
            binding.incToolbar.ivBack.setImageResource(R.drawable.ic_back)
            if (!binding.incToolbar.etSearch.isVisible) {
               binding.incToolbar.ivBack.visibility = android.view.View.GONE
               binding.incToolbar.ivMenu.visibility = android.view.View.VISIBLE
               binding.incToolbar.ivSearch.visibility = android.view.View.VISIBLE
            }
            
            binding.layoutBottomActions.visibility = android.view.View.GONE
            binding.fabStartChat.visibility = android.view.View.VISIBLE
        }
    }
    
    private fun handleBackPress() {
        if (conversationAdapter.selectionMode) {
            conversationAdapter.clearSelection()
        } else if (binding.incToolbar.etSearch.isVisible) {
             binding.incToolbar.ivBack.performClick()
        } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

    private fun setupDrawer() {
        binding.incToolbar.ivMenu.setOnClickListener {
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

        // Add badge to Messages item
        val messagesItem = binding.navView.menu.findItem(R.id.homeFragment)
        messagesItem.setActionView(R.layout.layout_drawer_badge)
        val badgeView = messagesItem.actionView?.findViewById<android.widget.TextView>(R.id.tv_badge)
        badgeView?.text = "1"
    }

    private fun setupHomeUi() {
        binding.recyclerConversations.apply {
            adapter = conversationAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        ItemTouchHelper(
            ConversationSwipeCallback(
                context = this,
                onArchive = { position ->
                    val conversation = conversationAdapter.getItemAt(position)
                    viewModel.archiveConversation(conversation.id)
                },
                onDelete = { position ->
                    val conversation = conversationAdapter.getItemAt(position)
                    viewModel.deleteConversation(conversation.id)
                }
            )
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
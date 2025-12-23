package com.example.messageapp.ui.conversation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messageapp.Controller
import com.example.messageapp.R
import com.example.messageapp.ui.adapters.ChatMessageAdapter
import com.example.messageapp.databinding.FragmentConversationBinding
import com.example.messageapp.ui.common.BaseActivity
import com.example.messageapp.ui.dialog.FailedMessageDialogFragment
import com.example.messageapp.ui.dialog.ScheduledMessageMenuDialogFragment
import com.example.messageapp.utils.formatTimestamp
import com.example.messageapp.viewmodels.ConversationViewModel
import kotlinx.coroutines.launch

class ConversationActivity : BaseActivity() {

    private lateinit var binding: FragmentConversationBinding

    private val conversationId: Long by lazy { intent.getLongExtra("conversationId", -1L) }
    private val contactNameArg: String? by lazy { intent.getStringExtra("contactName") }

    private val repository by lazy { (application as Controller).repository }
    private val viewModel: ConversationViewModel by viewModels {
        ConversationViewModel.Factory(conversationId, repository)
    }

    private val adapter by lazy { 
        ChatMessageAdapter(
            onFailedMessageClick = { messageId, _ -> showFailedMessageDialog(messageId) },
            onScheduledMessageClick = { messageId, content -> showScheduledMessageMenu(messageId, content) }
        )
    }

    private val scheduledTimestamp: Long by lazy { intent.getLongExtra("EXTRA_SCHEDULED_TIMESTAMP", 0L) }

    // ...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Hide system action bar if present
        supportActionBar?.hide()

        setupCustomToolbar()
        setupScheduleBanner()

        binding.recyclerMessages.apply {
            adapter = this@ConversationActivity.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@ConversationActivity)
        }

        binding.buttonSend.setOnClickListener {
            if (binding.layoutScheduleBanner.isVisible) {
                viewModel.scheduleMessage(binding.inputMessage.text.toString(), scheduledTimestamp)
            } else {
                viewModel.sendMessage(binding.inputMessage.text.toString())
            }
            binding.inputMessage.text?.clear()
        }
        
        
        binding.buttonAdd?.setOnClickListener {
             // Placeholder for future functionality
        }
        
        addMenuProvider(ConversationMenuProvider(
            onCall = { startCall() },
            onInfo = { showInfo() }
        ))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiList.collect { list ->
                        adapter.submitList(list) {
                            if (list.isNotEmpty()) {
                                binding.recyclerMessages.scrollToPosition(list.lastIndex)
                            }
                        }
                        binding.emptyState.isVisible = list.isEmpty()
                    }
                }
                launch {
                    viewModel.conversation.collect { conversation ->
                        val name = conversation?.contact?.name ?: contactNameArg
                        binding.textTitle.text = name
                    }
                }
                launch {
                    viewModel.inputEnabled.collect { enabled ->
                        binding.buttonSend.isEnabled = enabled
                        binding.inputMessage.isEnabled = enabled
                    }
                }
            }
        }
    }

    private fun setupScheduleBanner() {
        if (scheduledTimestamp > 0) {
            binding.layoutScheduleBanner.isVisible = true
            val date = java.text.SimpleDateFormat("dd MMM, yyyy hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(scheduledTimestamp))
            binding.textScheduleInfo.text = "Schedule at $date"
            
            binding.buttonCloseSchedule.setOnClickListener {
                binding.layoutScheduleBanner.isVisible = false
            }
        } else {
            binding.layoutScheduleBanner.isVisible = false
        }
    }

    private fun setupCustomToolbar() {
        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.buttonCall.setOnClickListener {
            startCall()
        }
        binding.buttonInfo.setOnClickListener {
            showInfo()
        }
        
        binding.textTitle.text = contactNameArg ?: "Chat"
    }

    private fun startCall() {
        val phone = viewModel.conversation.value?.contact?.phone
        if (phone.isNullOrBlank()) {
            Toast.makeText(this, R.string.no_phone_number_available, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        startActivity(intent)
    }

    private fun showInfo() {
        Toast.makeText(this, R.string.contact_details_coming_soon, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun showFailedMessageDialog(messageId: Long) {
        FailedMessageDialogFragment(
            onRetry = {
                viewModel.retry(messageId)
            },
            onCancel = {
                // Dialog dismissed, do nothing
            }
        ).show(supportFragmentManager, FailedMessageDialogFragment.TAG)
    }

    private fun showScheduledMessageMenu(messageId: Long, content: String) {
        ScheduledMessageMenuDialogFragment(
            messageContent = content,
            onSendNow = {
                viewModel.sendScheduledMessageNow(messageId)
            },
            onCopyText = {
                // Already handled in the fragment
            },
            onReschedule = {
                // Show schedule bottom sheet to pick new time
                com.example.messageapp.ui.dialog.ScheduleBottomSheetFragment { newTimestamp ->
                    viewModel.rescheduleMessage(messageId, newTimestamp)
                }.show(supportFragmentManager, com.example.messageapp.ui.dialog.ScheduleBottomSheetFragment.TAG)
            },
            onDelete = {
                viewModel.deleteMessage(messageId)
            },
            onEdit = {
                // Put message content in input field for editing
                binding.inputMessage.setText(content)
                binding.inputMessage.requestFocus()
            }
        ).show(supportFragmentManager, ScheduledMessageMenuDialogFragment.TAG)
    }
}

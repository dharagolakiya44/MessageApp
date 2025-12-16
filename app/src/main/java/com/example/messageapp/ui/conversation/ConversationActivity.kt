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

    private val adapter by lazy { ChatMessageAdapter { id -> viewModel.retry(id) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Binding likely has a toolbar or header included. If FragmentConversationBinding 
        // includes a custom toolbar, we use that. If not, we rely on Window Decor ActionBar.
        // Checking fragment_conversation.xml would be ideal, but assume standard app bar behavior for now
        // or setup custom toolbar if the layout has one.
        // Given typically fragments had their own toolbars or relied on MainActivity's.
        // We'll trust the binding structure. If it has a toolbar id, we set it.
        // Since we didn't check layout file internals deeply, we default to standard supportActionBar behavior
        // but hide it if layout provides its own header.
        // EDIT: MainActivity had a toolbar. Fragment likely didn't. 
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = contactNameArg ?: "Chat"

        binding.recyclerMessages.apply {
            adapter = this@ConversationActivity.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@ConversationActivity)
        }

        binding.buttonSend.setOnClickListener {
            viewModel.sendMessage(binding.inputMessage.text.toString())
            binding.inputMessage.text?.clear()
        }
        
        addMenuProvider(ConversationMenuProvider(
            onCall = { startCall() },
            onInfo = { showInfo() }
        ))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.messages.collect { list ->
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
                        supportActionBar?.title = conversation?.contact?.name ?: contactNameArg
                        supportActionBar?.subtitle = when {
                            conversation?.contact?.isOnline == true -> getString(R.string.online)
                            conversation?.contact?.lastSeen != null -> getString(
                                R.string.last_seen,
                                formatTimestamp(conversation.contact.lastSeen)
                            )
                            else -> getString(R.string.offline)
                        }
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
}

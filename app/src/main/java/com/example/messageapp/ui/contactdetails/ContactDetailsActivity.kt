package com.example.messageapp.ui.contactdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.messageapp.Controller
import com.example.messageapp.R
import com.example.messageapp.databinding.ActivityContactDetailsBinding
import com.example.messageapp.ui.common.BaseActivity
import com.example.messageapp.ui.conversation.ConversationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityContactDetailsBinding
    
    private val contactId: Long by lazy { intent.getLongExtra("contactId", -1L) }
    private val contactName: String? by lazy { intent.getStringExtra("contactName") }
    private val contactPhone: String? by lazy { intent.getStringExtra("contactPhone") }
    private val conversationId: Long by lazy { intent.getLongExtra("conversationId", -1L) }

    private val repository by lazy { (application as Controller).repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupContactInfo()
        setupActionButtons()
    }

    private fun setupToolbar() {
        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupContactInfo() {
        binding.textContactName.text = contactName ?: "Unknown"
        binding.textPhoneNumber.text = "Mobile ${contactPhone ?: "N/A"}"
    }

    private fun setupActionButtons() {
        // Call button
        binding.buttonCall.setOnClickListener {
            if (contactPhone.isNullOrBlank()) {
                Toast.makeText(this, R.string.no_phone_number_available, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactPhone"))
            startActivity(intent)
        }

        // Message button
        binding.buttonMessage.setOnClickListener {
            if (conversationId > 0) {
                // Navigate to existing conversation
                val intent = Intent(this, ConversationActivity::class.java).apply {
                    putExtra("conversationId", conversationId)
                    putExtra("contactName", contactName)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "No conversation available", Toast.LENGTH_SHORT).show()
            }
        }

        // Archive button
        binding.buttonArchive.setOnClickListener {
            if (conversationId > 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    repository.archiveConversation(conversationId)
                    Toast.makeText(this@ContactDetailsActivity, "Conversation archived", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // Block button
        binding.buttonBlock.setOnClickListener {
            if (conversationId > 0) {
                showBlockConfirmationDialog()
            }
        }

    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                if (conversationId > 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        repository.deleteConversation(conversationId)
                        Toast.makeText(this@ContactDetailsActivity, "Contact deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .show()
    }

    private fun showBlockConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Block Contact")
            .setMessage("Are you sure you want to block this contact?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Block") { _, _ ->
                if (conversationId > 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        repository.blockConversation(conversationId, true)
                        Toast.makeText(this@ContactDetailsActivity, "Contact blocked", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .show()
    }
}


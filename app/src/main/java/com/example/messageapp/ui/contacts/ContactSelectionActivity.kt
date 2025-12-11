package com.example.messageapp.ui.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.messageapp.Controller
import com.example.messageapp.adapters.ContactAdapter
import com.example.messageapp.databinding.FragmentContactSelectionBinding
import com.example.messageapp.ui.conversation.ConversationActivity
import com.example.messageapp.viewmodels.ContactSelectionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactSelectionActivity : AppCompatActivity() {

    private lateinit var binding: FragmentContactSelectionBinding

    private val repository by lazy { (application as Controller).repository }
    private val viewModel: ContactSelectionViewModel by viewModels {
        ContactSelectionViewModel.Factory(repository)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.syncContacts(this)
        }
    }

    private val adapter by lazy {
        ContactAdapter { contact ->
            viewModel.startConversation(contact.id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentContactSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Select Contact"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerContacts.apply {
            adapter = this@ContactSelectionActivity.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@ContactSelectionActivity)
        }

        checkPermissionAndSync()

        lifecycleScope.launch {
            viewModel.contacts.collectLatest { contacts ->
                adapter.submitList(contacts)
            }
        }

        lifecycleScope.launch {
            viewModel.createdConversationId.collectLatest { id ->
                if (id != null) {
                    val intent = Intent(this@ContactSelectionActivity, ConversationActivity::class.java).apply {
                        putExtra("conversationId", id)
                    }
                    startActivity(intent)
                    finish() // Close contact selection after starting chat
                    viewModel.resetNavigation()
                }
            }
        }
    }

    private fun checkPermissionAndSync() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.syncContacts(this)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

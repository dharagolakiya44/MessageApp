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
import com.example.messageapp.ui.adapters.ContactAdapter
import com.example.messageapp.databinding.FragmentContactSelectionBinding
import com.example.messageapp.ui.common.BaseActivity
import com.example.messageapp.ui.conversation.ConversationActivity
import com.example.messageapp.viewmodels.ContactSelectionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactSelectionActivity : BaseActivity() {

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

    private var isScheduled = false
    private var scheduledTimestamp: Long? = null

    private val adapter by lazy {
        ContactAdapter { contact ->
            if (isScheduled) {
                com.example.messageapp.ui.dialog.ScheduleBottomSheetFragment { timestamp ->
                    scheduledTimestamp = timestamp
                    viewModel.startConversation(contact.id)
                }.show(supportFragmentManager, com.example.messageapp.ui.dialog.ScheduleBottomSheetFragment.TAG)
            } else {
                viewModel.startConversation(contact.id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentContactSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isScheduled = intent.getBooleanExtra("EXTRA_IS_SCHEDULED", false)

        setupToolbar()

        binding.recyclerContacts.apply {
            adapter = this@ContactSelectionActivity.adapter
            layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this@ContactSelectionActivity)
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
                    val intent = Intent(
                        this@ContactSelectionActivity,
                        ConversationActivity::class.java
                    ).apply {
                        putExtra("conversationId", id)
                        if (isScheduled && scheduledTimestamp != null) {
                            putExtra("EXTRA_SCHEDULED_TIMESTAMP", scheduledTimestamp)
                        }
                    }
                    startActivity(intent)
                    finish() // Close contact selection after starting chat
                    viewModel.resetNavigation()
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.incToolbar.ivMenu.visibility = android.view.View.GONE
        binding.incToolbar.tvTitle.visibility = android.view.View.GONE
        binding.incToolbar.ivSearch.visibility = android.view.View.GONE
        binding.incToolbar.ivBack.visibility = android.view.View.VISIBLE
        binding.incToolbar.etSearch.visibility = android.view.View.VISIBLE

        binding.incToolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.incToolbar.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.onSearchQueryChanged(s.toString())
            }
        })
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

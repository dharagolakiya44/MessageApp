package com.example.messageapp.ui.conversation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messageapp.Controller
import com.example.messageapp.R
import com.example.messageapp.adapters.ChatMessageAdapter
import com.example.messageapp.databinding.FragmentConversationBinding
import com.example.messageapp.ui.utils.formatTimestamp
import com.example.messageapp.viewmodels.ConversationViewModel
import kotlinx.coroutines.launch

class ConversationFragment : Fragment() {

    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    private val conversationId: Long by lazy { requireArguments().getLong("conversationId") }
    private val contactNameArg: String? by lazy { requireArguments().getString("contactName") }

    private val repository by lazy { (requireActivity().application as Controller).repository }
    private val viewModel: ConversationViewModel by viewModels {
        ConversationViewModel.Factory(conversationId, repository)
    }

    private val adapter by lazy { ChatMessageAdapter { id -> viewModel.retry(id) } }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerMessages.apply {
            adapter = this@ConversationFragment.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }

        requireActivity().addMenuProvider(ConversationMenuProvider(
            onCall = { startCall() },
            onInfo = { showInfo() }
        ), viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.buttonSend.setOnClickListener {
            viewModel.sendMessage(binding.inputMessage.text.toString())
            binding.inputMessage.text?.clear()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                        binding.textTitle.text = conversation?.contact?.name ?: contactNameArg
                        binding.textSubtitle.text = when {
                            conversation?.contact?.isOnline == true -> getString(R.string.status_online)
                            conversation?.contact?.lastSeen != null -> getString(
                                R.string.status_last_seen,
                                formatTimestamp(conversation.contact.lastSeen)
                            )
                            else -> getString(R.string.status_offline)
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
            Toast.makeText(requireContext(), R.string.no_phone_available, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        startActivity(intent)
    }

    private fun showInfo() {
        Toast.makeText(requireContext(), R.string.conversation_info_hint, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


package com.example.messageapp.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.messageapp.Controller
import com.example.messageapp.R
import com.example.messageapp.adapters.ContactAdapter
import com.example.messageapp.databinding.FragmentContactSelectionBinding
import com.example.messageapp.viewmodels.ContactSelectionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactSelectionFragment : Fragment() {

    private var _binding: FragmentContactSelectionBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { (requireActivity().application as Controller).repository }
    private val viewModel: ContactSelectionViewModel by viewModels {
        ContactSelectionViewModel.Factory(repository)
    }

    private val adapter by lazy {
        ContactAdapter { contact ->
            viewModel.startConversation(contact.id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerContacts.apply {
            adapter = this@ContactSelectionFragment.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contacts.collectLatest { contacts ->
                adapter.submitList(contacts)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createdConversationId.collectLatest { id ->
                if (id != null) {
                    findNavController().navigate(
                        R.id.action_contactSelectionFragment_to_conversationFragment,
                        Bundle().apply { putLong("conversationId", id) }
                    )
                    viewModel.resetNavigation()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


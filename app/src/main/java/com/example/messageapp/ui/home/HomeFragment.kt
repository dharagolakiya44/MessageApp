package com.example.messageapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.messageapp.Controller
import com.example.messageapp.R
import com.example.messageapp.adapters.ConversationAdapter
import com.example.messageapp.databinding.FragmentHomeBinding
import com.example.messageapp.ui.common.SwipeToArchiveCallback
import com.example.messageapp.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { (requireActivity().application as Controller).repository }
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory(repository) }

    private val conversationAdapter by lazy {
        ConversationAdapter { conversation ->
            findNavController().navigate(
                R.id.action_homeFragment_to_conversationFragment,
                bundleOf(
                    "conversationId" to conversation.id,
                    "contactName" to conversation.contact.name
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerConversations.apply {
            adapter = conversationAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }
        ItemTouchHelper(
            SwipeToArchiveCallback(requireContext()) { position ->
                val conversation = conversationAdapter.getItemAt(position)
                viewModel.archiveConversation(conversation.id)
            }
        ).attachToRecyclerView(binding.recyclerConversations)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.conversations.collectLatest { list ->
                conversationAdapter.submitList(list)
                binding.viewEmpty.isVisible = list.isEmpty()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


package com.example.messageapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messageapp.databinding.ItemConversationBinding
import com.example.messageapp.domain.model.Conversation
import com.example.messageapp.ui.utils.formatTimestamp

class ConversationAdapter(
    private val onConversationClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(Diff) {

    inner class ConversationViewHolder(val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding =
            ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            textName.text = item.contact.name
            textMessage.text = item.lastMessage
            textTime.text = formatTimestamp(item.lastTimestamp)
            badgeUnread.isVisible = item.unreadCount > 0
            badgeUnread.text = item.unreadCount.toString()
            iconError.isVisible = item.hasFailedMessage
            textStatus.isVisible = !item.hasFailedMessage
            textStatus.text = item.lastStatus.name.lowercase().replaceFirstChar { it.uppercase() }
        }
        holder.itemView.setOnClickListener { onConversationClick(item) }
    }

    fun getItemAt(position: Int): Conversation = getItem(position)

    private object Diff : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean =
            oldItem == newItem
    }
}


package com.example.messageapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messageapp.databinding.ItemConversationBinding
import com.example.messageapp.domain.model.Conversation
import com.example.messageapp.utils.formatTimestamp

class ConversationAdapter(
    private val onConversationClick: (Conversation) -> Unit,
    private val onConversationLongClick: ((Conversation) -> Unit)? = null,
    private val onSelectionChanged: ((Int) -> Unit)? = null
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(Diff) {

    private val selectedItems = mutableSetOf<Long>()
    var selectionMode = false
        private set


    inner class ConversationViewHolder(val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding =
            ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val item = getItem(position)
        val isSelected = selectedItems.contains(item.id)

        holder.binding.apply {
            textName.text = item.contact.name
            textMessage.text = item.lastMessage
            textTime.text = formatTimestamp(item.lastTimestamp)
            badgeUnread.isVisible = item.unreadCount > 0 && !selectionMode
            badgeUnread.text = item.unreadCount.toString()
            iconError.isVisible = item.hasFailedMessage
            textStatus.isVisible = !item.hasFailedMessage
            textStatus.text = item.lastStatus.name.lowercase().replaceFirstChar { it.uppercase() }
            
            // Selection UI
            if (isSelected) {
                root.setBackgroundColor(holder.itemView.context.getColor(com.example.messageapp.R.color.selected_item_background)) 
                imageAvatar.isVisible = false
                imageSelected.isVisible = true
            } else {
                root.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                imageAvatar.isVisible = true
                imageSelected.isVisible = false
            }
        }
        
        holder.itemView.setOnClickListener { 
            if (selectionMode) {
                toggleSelection(item.id)
            } else {
                onConversationClick(item) 
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (onSelectionChanged != null && !selectionMode) {
                onConversationLongClick?.invoke(item)
                toggleSelection(item.id)
                true
            } else {
                false
            }
        }
    }

    fun toggleSelection(id: Long) {
        if (selectedItems.contains(id)) {
            selectedItems.remove(id)
        } else {
            selectedItems.add(id)
        }
        if (selectedItems.isEmpty()) {
            selectionMode = false
        } else {
            selectionMode = true
        }
        onSelectionChanged?.invoke(selectedItems.size)
        notifyDataSetChanged() // Ideally use notifyItemChanged but this is simpler for now
    }

    fun clearSelection() {
        selectedItems.clear()
        selectionMode = false
        onSelectionChanged?.invoke(0)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Long> = selectedItems.toList()


    fun getItemAt(position: Int): Conversation = getItem(position)

    private object Diff : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean =
            oldItem == newItem
    }
}


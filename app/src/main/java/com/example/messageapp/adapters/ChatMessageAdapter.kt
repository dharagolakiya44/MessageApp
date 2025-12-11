package com.example.messageapp.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messageapp.R
import com.example.messageapp.databinding.ItemChatMessageBinding
import com.example.messageapp.domain.model.ChatMessage
import com.example.messageapp.domain.model.MessageStatus
import com.example.messageapp.ui.utils.formatTimestamp

class ChatMessageAdapter(
    private val onRetry: (Long) -> Unit
) : ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(Diff) {

    inner class MessageViewHolder(val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding =
            ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            bubbleText.text = item.content
            textTime.text = formatTimestamp(item.timestamp)
            textStatus.text = when (item.status) {
                MessageStatus.SENDING -> "Sending"
                MessageStatus.SENT -> "Sent"
                MessageStatus.DELIVERED -> "Delivered"
                MessageStatus.READ -> "Read"
                MessageStatus.FAILED -> "Failed"
            }
            iconRetry.isVisible = item.status == MessageStatus.FAILED
            iconRetry.setOnClickListener { onRetry(item.id) }
            textStatus.isVisible = item.isOutgoing

            if (item.isOutgoing) {
                bubbleContainer.gravity = Gravity.END
                bubbleCard.setCardBackgroundColor(root.context.getColor(R.color.colorPrimary))
                bubbleText.setTextColor(root.context.getColor(R.color.colorWhiteCommon))
                textStatus.setTextColor(root.context.getColor(R.color.colorWhiteCommon))
            } else {
                bubbleContainer.gravity = Gravity.START
                bubbleCard.setCardBackgroundColor(root.context.getColor(android.R.color.white))
                bubbleText.setTextColor(root.context.getColor(R.color.colorTextBlack))
                textStatus.setTextColor(root.context.getColor(R.color.colorTextHint))
            }
            bubbleCard.invalidate()
        }
    }

    private object Diff : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
            oldItem == newItem
    }
}


package com.example.messageapp.ui.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messageapp.R
import com.example.messageapp.databinding.ItemChatMessageBinding
import com.example.messageapp.databinding.ItemDateHeaderBinding
import com.example.messageapp.domain.model.MessageStatus
import com.example.messageapp.ui.conversation.ChatUiModel
import com.example.messageapp.utils.formatMessageTime

class ChatMessageAdapter(
    private val onRetry: (Long) -> Unit
) : ListAdapter<ChatUiModel, RecyclerView.ViewHolder>(Diff) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatUiModel.MessageItem -> TYPE_MESSAGE
            is ChatUiModel.DateHeader -> TYPE_Header
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MESSAGE -> {
                val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MessageViewHolder(binding)
            }
            TYPE_Header -> {
                val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChatUiModel.MessageItem -> (holder as MessageViewHolder).bind(item)
            is ChatUiModel.DateHeader -> (holder as HeaderViewHolder).bind(item)
        }
    }

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(uiModel: ChatUiModel.MessageItem) {
            val item = uiModel.message
            binding.apply {
                bubbleText.text = item.content
                textTime.text = formatMessageTime(item.timestamp)
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
                    bubbleContainer.apply {
                        // Remove gravity logic if using ConstraintLayout, or adapt params
                        // Since we switched to ConstraintLayout in item_chat_message, we use layout params
                         val params = bubbleCard.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                         params.horizontalBias = 1.0f // End
                         bubbleCard.layoutParams = params
                    }
                    bubbleCard.setCardBackgroundColor(root.context.getColor(R.color.colorPrimary))
                    bubbleText.setTextColor(root.context.getColor(R.color.colorWhiteCommon))
                    textStatus.setTextColor(root.context.getColor(R.color.colorTextHint)) // Kept hint color for subtlety or change to white if needed
                } else {
                    bubbleContainer.apply {
                         val params = bubbleCard.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                         params.horizontalBias = 0.0f // Start
                         bubbleCard.layoutParams = params
                    }
                    bubbleCard.setCardBackgroundColor(root.context.getColor(android.R.color.white))
                    bubbleText.setTextColor(root.context.getColor(R.color.colorTextBlack))
                }
            }
        }
    }

    inner class HeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatUiModel.DateHeader) {
            binding.textDateHeader.text = item.date
        }
    }

    private object Diff : DiffUtil.ItemCallback<ChatUiModel>() {
        override fun areItemsTheSame(oldItem: ChatUiModel, newItem: ChatUiModel): Boolean {
            return when {
                oldItem is ChatUiModel.MessageItem && newItem is ChatUiModel.MessageItem -> oldItem.message.id == newItem.message.id
                oldItem is ChatUiModel.DateHeader && newItem is ChatUiModel.DateHeader -> oldItem.date == newItem.date
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ChatUiModel, newItem: ChatUiModel): Boolean =
            oldItem == newItem
    }

    companion object {
        private const val TYPE_MESSAGE = 0
        private const val TYPE_Header = 1
    }
}


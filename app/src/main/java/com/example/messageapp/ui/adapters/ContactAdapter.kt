package com.example.messageapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messageapp.databinding.ItemContactBinding
import com.example.messageapp.domain.model.Contact

class ContactAdapter(
    private val onContactSelected: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(Diff) {

    inner class ContactViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding =
            ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            textName.text = item.name
            textPhone.text = item.phone
            textStatus.text = if (item.isOnline) "Online" else "Last seen recently"
        }
        holder.itemView.setOnClickListener { onContactSelected(item) }
    }

    private object Diff : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean =
            oldItem == newItem
    }
}


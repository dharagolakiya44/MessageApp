package com.example.messageapp.ui.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.messageapp.R
import com.example.messageapp.databinding.DialogScheduledMessageMenuBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScheduledMessageMenuBottomSheetFragment(
    private val messageContent: String,
    private val onSendNow: () -> Unit,
    private val onCopyText: () -> Unit = {},
    private val onReschedule: () -> Unit,
    private val onDelete: () -> Unit,
    private val onEdit: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogScheduledMessageMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogScheduledMessageMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Make it behave like a popup menu instead of sliding from bottom
        dialog?.setOnShowListener { dialog ->
            val bottomSheetDialog = dialog as? BottomSheetDialog
            val bottomSheet = bottomSheetDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        binding.optionSendNow.setOnClickListener {
            onSendNow()
            dismiss()
        }

        binding.optionCopyText.setOnClickListener {
            copyToClipboard(messageContent)
            onCopyText()
            dismiss()
        }

        binding.optionReschedule.setOnClickListener {
            onReschedule()
            dismiss()
        }

        binding.optionDelete.setOnClickListener {
            onDelete()
            dismiss()
        }

        binding.optionEdit.setOnClickListener {
            onEdit()
            dismiss()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Message", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "ScheduledMessageMenu"
    }
}

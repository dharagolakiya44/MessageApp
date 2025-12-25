package com.example.messageapp.ui.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.example.messageapp.databinding.DialogScheduledMessageMenuBinding
import androidx.fragment.app.DialogFragment

class ScheduledMessageMenuDialogFragment(
    private val messageContent: String,
    private val onSendNow: () -> Unit,
    private val onCopyText: () -> Unit = {},
    private val onReschedule: () -> Unit,
    private val onDelete: () -> Unit,
    private val onEdit: () -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogScheduledMessageMenuBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

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

    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return
        window.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(android.view.Gravity.BOTTOM or android.view.Gravity.END)
        
        // Add margins from edges
        val displayMetrics = resources.displayMetrics
        val marginDp = 16f
        val marginPx = (marginDp * displayMetrics.density).toInt()
        val layoutParams = window.attributes
        layoutParams.x = marginPx
        layoutParams.y = marginPx
        window.attributes = layoutParams
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

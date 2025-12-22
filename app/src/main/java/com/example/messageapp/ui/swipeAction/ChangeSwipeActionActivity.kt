package com.example.messageapp.ui.swipeAction

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import com.example.messageapp.R
import com.example.messageapp.databinding.ActivityChangeSwipeActionBinding
import com.example.messageapp.databinding.DialogChangeSwipeActionBinding
import com.example.messageapp.extention.viewBinding
import com.example.messageapp.ui.common.BaseActivity

class ChangeSwipeActionActivity : BaseActivity() {
    private val binding by viewBinding(ActivityChangeSwipeActionBinding::inflate)

    private var rightSwipeAction = "delete"
    private var leftSwipeAction = "archive"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applySystemBarInsets(R.color.colorBgMain, !isNightModeActive())

        setToolbar()
        setupInitialState()
        setupClickListeners()
    }

    private fun setToolbar() {
        binding.incToolbar.tvTitle.text = getString(R.string.swipe_action)

        binding.incToolbar.ivMenu.visibility = View.GONE
        binding.incToolbar.ivSearch.visibility = View.GONE
        binding.incToolbar.ivBack.visibility = View.VISIBLE

        binding.incToolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupInitialState() {
        updateRightSwipeUI(rightSwipeAction)
        updateLeftSwipeUI(leftSwipeAction)
    }

    private fun setupClickListeners() {
        binding.btnChangeRightSwipe.setOnClickListener {
            showChangeActionDialog(true) // true for right swipe
        }

        binding.btnChangeLeftSwipe.setOnClickListener {
            showChangeActionDialog(false) // false for left swipe
        }
    }

    private fun showChangeActionDialog(isRightSwipe: Boolean) {
        val dialog = Dialog(this, R.style.DefaultAnimationTheme)
        val dialogBinding = DialogChangeSwipeActionBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setContentView(dialogBinding.root)

        val currentAction = if (isRightSwipe) rightSwipeAction else leftSwipeAction

        // Set up radio buttons
        val radioButtons = listOf(
            dialogBinding.rbNone,
            dialogBinding.rbDelete,
            dialogBinding.rbMarkRead,
            dialogBinding.rbMarkUnread,
            dialogBinding.rbArchive,
            dialogBinding.rbBlock
        )

        val actionValues = listOf("none", "delete", "mark_read", "mark_unread", "archive", "block")

        // Select current action
        val currentIndex = actionValues.indexOf(currentAction)
        if (currentIndex >= 0) {
            radioButtons[currentIndex].isChecked = true
        }

        // Set click listeners for radio buttons
        dialogBinding.llNone.setOnClickListener {
            selectAction(dialogBinding.rbNone, radioButtons)
            handleActionSelection("none", isRightSwipe, dialog)
        }

        dialogBinding.llDelete.setOnClickListener {
            selectAction(dialogBinding.rbDelete, radioButtons)
            handleActionSelection("delete", isRightSwipe, dialog)
        }

        dialogBinding.llMarkRead.setOnClickListener {
            selectAction(dialogBinding.rbMarkRead, radioButtons)
            handleActionSelection("mark_read", isRightSwipe, dialog)
        }

        dialogBinding.llMarkUnread.setOnClickListener {
            selectAction(dialogBinding.rbMarkUnread, radioButtons)
            handleActionSelection("mark_unread", isRightSwipe, dialog)
        }

        dialogBinding.llArchive.setOnClickListener {
            selectAction(dialogBinding.rbArchive, radioButtons)
            handleActionSelection("archive", isRightSwipe, dialog)
        }

        dialogBinding.llBlock.setOnClickListener {
            selectAction(dialogBinding.rbBlock, radioButtons)
            handleActionSelection("block", isRightSwipe, dialog)
        }

        // Set click listeners for radio buttons directly
        radioButtons.forEachIndexed { index, radioButton ->
            radioButton.setOnClickListener {
                selectAction(radioButton, radioButtons)
                handleActionSelection(actionValues[index], isRightSwipe, dialog)
            }
        }

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        
        // Adjust dialog window
        val window = dialog.window
        val displayMetrics = resources.displayMetrics
        val margin = 20 * displayMetrics.density // 20dp margin as Float
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        
        // Add margin to dialog
        val layoutParams = window?.attributes
        layoutParams?.horizontalMargin = margin
        window?.attributes = layoutParams
        
        dialog.show()
    }

    private fun selectAction(selectedRadio: android.widget.RadioButton, allRadios: List<android.widget.RadioButton>) {
        allRadios.forEach { it.isChecked = false }
        selectedRadio.isChecked = true
    }

    private fun handleActionSelection(action: String, isRightSwipe: Boolean, dialog: Dialog) {
        if (isRightSwipe) {
            rightSwipeAction = action
            updateRightSwipeUI(action)
        } else {
            leftSwipeAction = action
            updateLeftSwipeUI(action)
        }
        dialog.dismiss()
    }

    private fun updateRightSwipeUI(action: String) {
        val actionName = getActionName(action)
        binding.tvRightSwipeAction.text = actionName

        val (iconRes, backgroundRes) = getActionIconAndBackground(action)
        binding.ivRightSwipeIcon.setImageResource(iconRes)
        binding.ivRightSwipeIcon.setBackgroundResource(backgroundRes)
    }

    private fun updateLeftSwipeUI(action: String) {
        val actionName = getActionName(action)
        binding.tvLeftSwipeAction.text = actionName

        val (iconRes, backgroundRes) = getActionIconAndBackground(action)
        binding.ivLeftSwipeIcon.setImageResource(iconRes)
        binding.ivLeftSwipeIcon.setBackgroundResource(backgroundRes)
    }

    private fun getActionName(action: String): String {
        return when (action) {
            "none" -> getString(R.string.none)
            "delete" -> getString(R.string.delete)
            "mark_read" -> getString(R.string.mark_as_read)
            "mark_unread" -> getString(R.string.mark_as_unread)
            "archive" -> getString(R.string.archive)
            "block" -> getString(R.string.block)
            else -> getString(R.string.none)
        }
    }

    private fun getActionIconAndBackground(action: String): Pair<Int, Int> {
        return when (action) {
            "delete" -> Pair(R.drawable.ic_delete, R.drawable.bg_swipe_action_icon_red)
            "mark_read" -> Pair(R.drawable.ic_mark_read, R.drawable.bg_swipe_action_icon_blue)
            "mark_unread" -> Pair(R.drawable.ic_mark_unread, R.drawable.bg_swipe_action_icon_blue)
            "archive" -> Pair(R.drawable.ic_archive, R.drawable.bg_swipe_action_icon_blue)
            "block" -> Pair(R.drawable.ic_block, R.drawable.bg_swipe_action_icon_red)
            else -> Pair(R.drawable.ic_user_placeholder, R.drawable.bg_swipe_preview_item)
        }
    }
}

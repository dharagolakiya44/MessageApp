package com.example.messageapp.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.messageapp.R

class ConversationSwipeCallback(
    context: Context,
    private val onArchive: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val archiveIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_archive)
    private val deleteIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    
    // Backgrounds
    private val archiveBackground = ColorDrawable(Color.parseColor("#4CAF50")) // Green
    private val deleteBackground = ColorDrawable(Color.parseColor("#F44336")) // Red

    private val iconMargin = 32 // Margin for icon from edge

    init {
        // Ensure icons are white for better visibility on colored backgrounds
        archiveIcon?.setTint(Color.WHITE)
        deleteIcon?.setTint(Color.WHITE)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // We don't support moving items up/down
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (direction == ItemTouchHelper.LEFT) {
            // Swipe Left -> Archive
            onArchive(position)
        } else if (direction == ItemTouchHelper.RIGHT) {
            // Swipe Right -> Delete
            onDelete(position)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val iconHeight = archiveIcon?.intrinsicHeight ?: 0
        val iconWidth = archiveIcon?.intrinsicWidth ?: 0

        // Determine swipe direction and draw appropriate background and icon
        if (dX > 0) { 
            // Swiping Right -> Delete (Red)
            deleteBackground.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
            deleteBackground.draw(c)

            // Draw Delete Icon
            if (deleteIcon != null) {
                val iconTop = itemView.top + (itemView.height - iconHeight) / 2
                val iconBottom = iconTop + iconHeight
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + iconWidth
                
                // Only draw icon if the swipe is large enough to show it partially
                if (dX > iconMargin) {
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(c)
                }
            }

        } else if (dX < 0) { 
            // Swiping Left -> Archive (Green)
            archiveBackground.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            archiveBackground.draw(c)

            // Draw Archive Icon
            if (archiveIcon != null) {
                val iconTop = itemView.top + (itemView.height - iconHeight) / 2
                val iconBottom = iconTop + iconHeight
                val iconRight = itemView.right - iconMargin
                val iconLeft = itemView.right - iconMargin - iconWidth

                // Only draw icon if the swipe is large enough
                if (Math.abs(dX) > iconMargin) {
                    archiveIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    archiveIcon.draw(c)
                }
            }
        } else {
            // No swipe
            archiveBackground.setBounds(0, 0, 0, 0)
            deleteBackground.setBounds(0, 0, 0, 0)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}

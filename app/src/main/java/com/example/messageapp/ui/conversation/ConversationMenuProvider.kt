package com.example.messageapp.ui.conversation

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.example.messageapp.R

class ConversationMenuProvider(
    private val onCall: () -> Unit,
    private val onInfo: () -> Unit
) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.conversation_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_call -> {
                onCall(); true
            }
            R.id.menu_info -> {
                onInfo(); true
            }
            else -> false
        }
    }
}


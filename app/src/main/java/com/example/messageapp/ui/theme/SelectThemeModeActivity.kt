package com.example.messageapp.ui.theme

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.messageapp.R
import com.example.messageapp.data.PreferenceHelper
import com.example.messageapp.databinding.ActivitySelectThemeModeBinding
import com.example.messageapp.extention.viewBinding
import com.example.messageapp.utils.Constants

class SelectThemeModeActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivitySelectThemeModeBinding::inflate)
    private var selectedThemeMode: Int = Constants.THEME_SYSTEM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(PreferenceHelper.getThemeMode(this))
        setContentView(binding.root)

        setToolbar()
        setUpClick()
        loadSavedTheme()
    }

    private fun setToolbar() {
        binding.incToolbar.tvTitle.text = getString(R.string.theme_mode)

        binding.incToolbar.ivMenu.visibility = View.GONE
        binding.incToolbar.ivSearch.visibility = View.GONE
        binding.incToolbar.ivBack.visibility = View.VISIBLE
        binding.incToolbar.icDone.visibility = View.VISIBLE

        binding.incToolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.incToolbar.icDone.setOnClickListener {
            PreferenceHelper.setThemeMode(this, selectedThemeMode)
            AppCompatDelegate.setDefaultNightMode(selectedThemeMode)
            finish()
        }
    }

    private fun setUpClick() {

        binding.llSystemMode.setOnClickListener {
            selectedThemeMode = Constants.THEME_SYSTEM
            setChecked("system")
        }

        binding.llDayMode.setOnClickListener {
            selectedThemeMode = Constants.THEME_DAY
            setChecked("day")
        }

        binding.llNightMode.setOnClickListener {
            selectedThemeMode = Constants.THEME_NIGHT
            setChecked("night")
        }
    }

    private fun setChecked(mode: String) {
        binding.viewSystemModeSelector.isSelected = mode == "system"
        binding.viewDayModeSelector.isSelected = mode == "day"
        binding.viewNightModeSelector.isSelected = mode == "night"
    }

    private fun loadSavedTheme() {
        selectedThemeMode = PreferenceHelper.getThemeMode(this)
        when (selectedThemeMode) {
            Constants.THEME_SYSTEM -> setChecked("system")
            Constants.THEME_DAY -> setChecked("day")
            Constants.THEME_NIGHT -> setChecked("night")
        }
    }

}
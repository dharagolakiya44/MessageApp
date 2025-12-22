package com.example.messageapp.ui.fontSize

import android.os.Bundle
import android.view.View
import com.example.messageapp.R
import com.example.messageapp.data.PreferenceHelper
import com.example.messageapp.databinding.ActivityChangeFontSizeBinding
import com.example.messageapp.extention.viewBinding
import com.example.messageapp.ui.common.BaseActivity

class ChangeFontSizeActivity : BaseActivity() {
    private val binding by viewBinding(ActivityChangeFontSizeBinding::inflate)
    private var selectedFontSize: String = "normal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applySystemBarInsets(R.color.colorBgMain, !isNightModeActive())

        setToolbar()
        setupClickListeners()
        loadSavedFontSize()
    }

    private fun setToolbar() {
        binding.incToolbar.tvTitle.text = getString(R.string.font_size)

        binding.incToolbar.ivMenu.visibility = View.GONE
        binding.incToolbar.ivSearch.visibility = View.GONE
        binding.incToolbar.ivBack.visibility = View.VISIBLE
        binding.incToolbar.icDone.visibility = View.VISIBLE

        binding.incToolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.incToolbar.icDone.setOnClickListener {
            PreferenceHelper.setFontSize(this, selectedFontSize)
            // Set result to indicate font size was changed
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.llSmall.setOnClickListener {
            selectedFontSize = "small"
            setChecked("small")
        }

        binding.llNormal.setOnClickListener {
            selectedFontSize = "normal"
            setChecked("normal")
        }

        binding.llLarge.setOnClickListener {
            selectedFontSize = "large"
            setChecked("large")
        }

        binding.llExtraLarge.setOnClickListener {
            selectedFontSize = "xlarge"
            setChecked("xlarge")
        }
    }

    private fun setChecked(size: String) {
        binding.viewSmallSelector.isSelected = size == "small"
        binding.viewNormalSelector.isSelected = size == "normal"
        binding.viewLargeSelector.isSelected = size == "large"
        binding.viewExtraLargeSelector.isSelected = size == "xlarge"
    }

    private fun loadSavedFontSize() {
        selectedFontSize = PreferenceHelper.getFontSize(this)
        setChecked(selectedFontSize)
    }
}
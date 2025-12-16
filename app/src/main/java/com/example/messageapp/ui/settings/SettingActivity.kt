package com.example.messageapp.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.example.messageapp.R
import com.example.messageapp.databinding.ActivitySettingBinding
import com.example.messageapp.extention.viewBinding
import com.example.messageapp.ui.common.BaseActivity

class SettingActivity : BaseActivity() {
    private val binding by viewBinding(ActivitySettingBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applySystemBarInsets(R.color.colorBgMain, !isNightModeActive())

        setToolbar()

        setupClicks()
    }

    private fun setToolbar() {
        binding.incToolbar.tvTitle.text = getString(R.string.settings)

        binding.incToolbar.ivMenu.visibility = View.GONE
        binding.incToolbar.ivSearch.visibility = View.GONE
        binding.incToolbar.ivBack.visibility = View.VISIBLE

        binding.incToolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClicks() {

        binding.llLanguage.setOnClickListener {
//            val intent = Intent(this, LanguageActivity::class.java)
//            intent.putExtra("fromWhere", "settings")
        }

        binding.llTheme.setOnClickListener {

        }

        binding.llSwipe.setOnClickListener {

        }

        binding.llFontSize.setOnClickListener {

        }

        binding.llAfterCall.setOnClickListener {

        }

        binding.llRate.setOnClickListener {
            rateUs()
        }

        binding.llShare.setOnClickListener {
            shareApp()
        }

        binding.llPrivacy.setOnClickListener {
            openPrivacyPolicy()
        }
    }

    private fun rateUs() {
        val uri = Uri.parse("market://details?id=$packageName")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.messageapp))
            val shareMessage =
                "\n${getString(R.string.let_me_recommend_you_this_application)}\n\n https://play.google.com/store/apps/details?id=${packageName}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_one)))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
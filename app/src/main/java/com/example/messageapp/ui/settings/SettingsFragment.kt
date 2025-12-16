package com.example.messageapp.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.messageapp.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        setupSummaries()
        setupActions()
    }

    private fun setupSummaries() {
        val language = findPreference<ListPreference>("language")
        val theme = findPreference<ListPreference>("theme_mode")
        val fontSize = findPreference<ListPreference>("font_size")
        val swipe = findPreference<ListPreference>("swipe_action")

        listOf(language, theme, fontSize, swipe).forEach { pref ->
            pref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }
    }

    private fun setupActions() {
        findPreference<Preference>("rate_app")?.setOnPreferenceClickListener {
            openPlayStore(); true
        }
        findPreference<Preference>("share_app")?.setOnPreferenceClickListener {
            shareApp(); true
        }
        findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
            openPrivacyPolicy(); true
        }
    }

    private fun openPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}"))
        startActivity(intent)
    }

    private fun shareApp() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.check_out_this_messaging_app))
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun openPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy"))
        startActivity(intent)
    }
}


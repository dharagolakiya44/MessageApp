package com.example.messageapp.utils

import android.content.Context
import com.example.messageapp.data.PreferenceHelper
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun Context.getLocalizedContext(): Context {
        val langCode = PreferenceHelper.getLanguageCode(this) // e.g. "hi"
        if (langCode.isNullOrEmpty()) return this

        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return createConfigurationContext(config)
    }


}
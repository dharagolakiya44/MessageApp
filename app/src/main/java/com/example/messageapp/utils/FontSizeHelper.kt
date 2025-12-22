package com.example.messageapp.utils

import android.content.Context
import android.content.res.Configuration
import com.example.messageapp.data.PreferenceHelper

object FontSizeHelper {

    fun getFontScale(fontSize: String): Float {
        return when (fontSize) {
            "small" -> 0.85f
            "normal" -> 1.0f
            "large" -> 1.15f
            "xlarge" -> 1.3f
            else -> 1.0f
        }
    }

    fun applyFontScale(context: Context): Context {
        val fontSize = PreferenceHelper.getFontSize(context)
        val fontScale = getFontScale(fontSize)
        
        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.fontScale = fontScale
        
        return context.createConfigurationContext(config)
    }

    fun getFontScale(context: Context): Float {
        val fontSize = PreferenceHelper.getFontSize(context)
        return getFontScale(fontSize)
    }
}


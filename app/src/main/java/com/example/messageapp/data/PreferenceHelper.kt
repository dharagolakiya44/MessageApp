package com.example.messageapp.data

import android.content.Context
import android.content.SharedPreferences
import com.example.messageapp.utils.Constants.THEME_SYSTEM

object PreferenceHelper {
    private const val PREF_NAME = "message_prefs"
    private const val LANGUAGE_CODE = "language_code"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_FONT_SIZE = "font_size"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setLanguageCode(context: Context, code: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_CODE, code).apply()
    }

    fun getLanguageCode(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_CODE, "en") ?: "en"
    }

    fun setThemeMode(context: Context, mode: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getThemeMode(context: Context): Int {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM) // default: system
    }

    fun setFontSize(context: Context, fontSize: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FONT_SIZE, fontSize).apply()
    }

    fun getFontSize(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FONT_SIZE, "normal") ?: "normal"
    }
}
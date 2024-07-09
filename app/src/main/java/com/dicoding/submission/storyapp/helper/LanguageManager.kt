package com.dicoding.submission.storyapp.helper

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LanguageManager {
    private const val LANGUAGE_PREF = "LANGUAGE_PREF"
    private const val LANGUAGE_KEY = "LANGUAGE_KEY"

    fun setLocale(context: Context, languageCode: String) {
        saveLanguage(context, languageCode)
        updateResources(context, languageCode)
    }

    fun loadLocale(context: Context) {
        val languageCode = getSavedLanguage(context) ?: Locale.getDefault().language
        updateResources(context, languageCode)
    }

    fun getSavedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(LANGUAGE_PREF, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, Locale.getDefault().language)
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(LANGUAGE_PREF, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }

    private fun updateResources(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}


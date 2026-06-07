package com.maktab.app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun applyLocale(activity: Activity, languageCode: String) {
        activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit().putString("language", languageCode).apply()
        activity.recreate()
    }

    fun getSavedLanguage(context: Context): String =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("language", "uz") ?: "uz"

    fun wrap(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
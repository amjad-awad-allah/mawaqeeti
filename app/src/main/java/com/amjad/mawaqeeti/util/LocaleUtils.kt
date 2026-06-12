package com.amjad.mawaqeeti.util

import android.content.Context
import android.content.Intent
import android.os.LocaleList
import java.util.Locale

object LocaleUtils {

    /**
     * Applies the given language code ("en" or "ar") to the context.
     * Also sets the correct layout direction (LTR for English, RTL for Arabic).
     * Should be called in Activity.attachBaseContext().
     */
    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Returns the display name for a prayer given its English key and the current language code.
     * This keeps all data-layer keys in English ("Fajr", "Dhuhr", etc.)
     * while the UI shows the correct localized name.
     */
    fun getPrayerDisplayName(context: Context, englishKey: String): String {
        return when (englishKey) {
            "Fajr"    -> context.getString(com.amjad.mawaqeeti.R.string.prayer_fajr)
            "Dhuhr"   -> context.getString(com.amjad.mawaqeeti.R.string.prayer_dhuhr)
            "Asr"     -> context.getString(com.amjad.mawaqeeti.R.string.prayer_asr)
            "Maghrib" -> context.getString(com.amjad.mawaqeeti.R.string.prayer_maghrib)
            "Isha"    -> context.getString(com.amjad.mawaqeeti.R.string.prayer_isha)
            else      -> englishKey
        }
    }

    /**
     * Converts a localized display prayer name back to its English key
     * (needed when the data layer expects English keys like "Fajr").
     */
    fun getEnglishKey(localizedName: String): String {
        return when (localizedName) {
            "الفجر", "Fajr"    -> "Fajr"
            "الظهر", "Dhuhr"   -> "Dhuhr"
            "العصر", "Asr"     -> "Asr"
            "المغرب", "Maghrib" -> "Maghrib"
            "العشاء", "Isha"   -> "Isha"
            else               -> localizedName
        }
    }

    /**
     * Restarts the app's main activity to immediately apply the new language.
     */
    fun restartApp(context: Context) {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }
}

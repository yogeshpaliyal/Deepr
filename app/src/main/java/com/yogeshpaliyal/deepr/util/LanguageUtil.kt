package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LanguageUtil {
    // Language code constants
    const val SYSTEM_DEFAULT = ""
    const val ENGLISH = "en"
    const val HINDI = "hi"
    const val SPANISH = "es"
    const val FRENCH = "fr"
    const val GERMAN = "de"
    const val URDU = "ur"

    // Available languages
    data class Language(
        val code: String,
        val name: String,
        val nativeName: String,
    )

    val availableLanguages =
        listOf(
            Language(SYSTEM_DEFAULT, "System Default", "System Default"),
            Language(ENGLISH, "English", "English"),
            Language(HINDI, "Hindi", "हिंदी"),
            Language(SPANISH, "Spanish", "Español"),
            Language(FRENCH, "French", "Français"),
            Language(GERMAN, "German", "Deutsch"),
            Language(URDU, "Urdu", "اردو"),
        )

    /**
     * Updates the app's locale configuration
     */
    fun updateLocale(
        context: Context,
        languageCode: String,
    ): Context {
        val locale =
            if (languageCode.isEmpty()) {
                // Use system default locale
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
            } else {
                Locale(languageCode)
            }

        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            return context
        }
    }

    /**
     * Gets the display name for a language code
     */
    fun getLanguageDisplayName(languageCode: String): String = availableLanguages.find { it.code == languageCode }?.name ?: "System Default"

    /**
     * Gets the native name for a language code
     */
    fun getLanguageNativeName(languageCode: String): String =
        availableLanguages.find { it.code == languageCode }?.nativeName ?: "System Default"
}

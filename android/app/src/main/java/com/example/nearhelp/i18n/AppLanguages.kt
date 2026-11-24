package com.example.nearhelp.i18n

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

data class LanguageOption(val code: String, val label: String)

object AppLanguages {
    val interfaceLanguages = listOf(LanguageOption("en", "English"), LanguageOption("bn", "বাংলা · Bengali"), LanguageOption("hi", "हिन्दी · Hindi"))
    // English and 30 Indian languages. These are communication preferences, not a claim
    // that all 31 languages have complete interface translations.
    val communicationLanguages = interfaceLanguages + listOf(
        LanguageOption("as", "অসমীয়া · Assamese"), LanguageOption("brx", "बड़ो · Bodo"),
        LanguageOption("doi", "डोगरी · Dogri"), LanguageOption("gu", "ગુજરાતી · Gujarati"),
        LanguageOption("kn", "ಕನ್ನಡ · Kannada"), LanguageOption("ks", "کٲشُر · Kashmiri"),
        LanguageOption("kok", "कोंकणी · Konkani"), LanguageOption("mai", "मैथिली · Maithili"),
        LanguageOption("ml", "മലയാളം · Malayalam"), LanguageOption("mni", "ꯃꯤꯇꯩꯂꯣꯟ · Meitei (Manipuri)"),
        LanguageOption("mr", "मराठी · Marathi"), LanguageOption("ne", "नेपाली · Nepali"),
        LanguageOption("or", "ଓଡ଼ିଆ · Odia"), LanguageOption("pa", "ਪੰਜਾਬੀ · Punjabi"),
        LanguageOption("sa", "संस्कृतम् · Sanskrit"), LanguageOption("sat", "ᱥᱟᱱᱛᱟᱲᱤ · Santali"),
        LanguageOption("sd", "سنڌي · Sindhi"), LanguageOption("ta", "தமிழ் · Tamil"),
        LanguageOption("te", "తెలుగు · Telugu"), LanguageOption("ur", "اردو · Urdu"),
        LanguageOption("bho", "भोजपुरी · Bhojpuri"), LanguageOption("raj", "राजस्थानी · Rajasthani"),
        LanguageOption("hne", "छत्तीसगढ़ी · Chhattisgarhi"), LanguageOption("mag", "मगही · Magahi"),
        LanguageOption("gbm", "गढ़वळि · Garhwali"), LanguageOption("kfy", "कुमाऊँनी · Kumaoni"),
        LanguageOption("tcy", "ತುಳು · Tulu"), LanguageOption("kha", "Khasi")
    )
    fun normalizedInterfaceCode(code: String?): String = code?.takeIf { candidate -> interfaceLanguages.any { it.code == candidate } } ?: "en"

    fun selected(context: Context): String {
        if (Build.VERSION.SDK_INT >= 33) {
            val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
            if (!locales.isEmpty) return normalizedInterfaceCode(locales[0].language)
            return normalizedInterfaceCode(context.resources.configuration.locales[0].language)
        }
        return normalizedInterfaceCode(context.getSharedPreferences("communication_settings", Context.MODE_PRIVATE).getString("app_language", "en"))
    }

    /** Android 13+ also exposes the same three supported languages in system app settings. */
    fun select(activity: Activity, code: String) {
        val selected = normalizedInterfaceCode(code)
        activity.getSharedPreferences("communication_settings", Context.MODE_PRIVATE).edit().putString("app_language", selected).apply()
        if (Build.VERSION.SDK_INT >= 33) activity.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(selected)
        else activity.recreate()
    }

    fun wrap(context: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return context
        val locale = Locale.forLanguageTag(selected(context))
        return context.createConfigurationContext(Configuration(context.resources.configuration).apply { setLocale(locale); setLayoutDirection(locale) })
    }
}

fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.activity()
    else -> null
}

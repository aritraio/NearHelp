package com.example.nearhelp.i18n

import android.app.Application
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = Application::class)
class AppLanguagesTest {
    private fun context(code: String): Context = RuntimeEnvironment.getApplication().createConfigurationContext(
        Configuration(RuntimeEnvironment.getApplication().resources.configuration).apply { setLocale(Locale.forLanguageTag(code)) })

    @Test fun interfaceOnlyHasThreeLanguages_communicationHas31UniqueOptions_andNoSpanish() {
        assertEquals(listOf("en", "bn", "hi"), AppLanguages.interfaceLanguages.map { it.code })
        assertEquals(31, AppLanguages.communicationLanguages.size)
        assertEquals(31, AppLanguages.communicationLanguages.map { it.code }.distinct().size)
        assertFalse(AppLanguages.communicationLanguages.any { it.code == "es" })
    }

    @Test fun resourcesChangeTheInterfaceAndKeepUnrecognizedUserContent() {
        assertEquals("Settings", UiTranslations.text(context("en"), "Settings"))
        assertEquals("সেটিংস", UiTranslations.text(context("bn"), "Settings"))
        assertEquals("सेटिंग", UiTranslations.text(context("hi"), "Settings"))
        assertEquals("हिंदी में मेरी बात", UiTranslations.text(context("bn"), "हिंदी में मेरी बात"))
        assertEquals("Barasat Hospital", UiTranslations.text(context("hi"), "Barasat Hospital"))
    }

    @Test fun formattedCountdownAndStatusTranslateWithoutChangingNumbersOrNames() {
        assertEquals("সাধারণ চিকিৎসার SOS পাঠাতে আর 3 সেকেন্ড", UiTranslations.text(context("bn"), "3s until a general medical SOS"))
        assertEquals("5 में से 2 संपर्क जोड़े गए", UiTranslations.text(context("hi"), "2 of 5 contacts added"))
        assertEquals("चिकित्सा आपातकाल: सीने में दर्द", UiTranslations.text(context("hi"), "Medical emergency: Chest pain"))
        assertEquals("Riya को हटाएँ?", UiTranslations.text(context("hi"), "Remove Riya?"))
    }

    @Test fun selectedLanguageSurvivesNewContextOnPreAndroid13_andUnsupportedCodesUseEnglish() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        val activity = controller.get()
        AppLanguages.select(activity, "bn")
        val wrapped = AppLanguages.wrap(activity.applicationContext)
        assertEquals("bn", wrapped.resources.configuration.locales[0].language)
        assertEquals("বাংলা · Bengali", AppLanguages.interfaceLanguages.first { it.code == AppLanguages.selected(wrapped) }.label)
        AppLanguages.select(activity, "hi")
        assertEquals("hi", AppLanguages.wrap(activity.applicationContext).resources.configuration.locales[0].language)
        assertEquals("en", AppLanguages.normalizedInterfaceCode("es"))
        controller.pause().stop().destroy()
    }

    @Test
    @Config(sdk = [33])
    fun android13LanguageChoiceUsesSystemPerAppLocale() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        val activity = controller.get()
        AppLanguages.select(activity, "hi")
        assertEquals("hi", activity.getSystemService(android.app.LocaleManager::class.java).applicationLocales[0].language)
        assertEquals("hi", AppLanguages.selected(activity))
        AppLanguages.select(activity, "en")
        assertEquals("en", AppLanguages.selected(activity))
        controller.pause().stop().destroy()
    }
}

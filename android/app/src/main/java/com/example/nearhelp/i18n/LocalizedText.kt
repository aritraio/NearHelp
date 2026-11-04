package com.example.nearhelp.i18n

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.example.nearhelp.R
import java.util.Locale

/**
 * Resource-backed rendering adapter for the existing Care component API. Domain values (symptom
 * IDs, profile values, status codes) remain unchanged; only their presentation is localized.
 * Unknown strings, including user messages and provider place names, are preserved verbatim.
 */
object UiTranslations {
    private data class Entry(val id: Int, val english: String, val matcher: Regex?)
    @Volatile private var entries: List<Entry>? = null
    private fun catalog(context: Context): List<Entry> = entries ?: synchronized(this) {
        entries ?: run {
            val english = context.applicationContext.createConfigurationContext(Configuration(context.resources.configuration).apply { setLocale(Locale.ENGLISH) })
            val array = english.resources.obtainTypedArray(R.array.ui_text_resources)
            try {
                (0 until array.length()).map { index ->
                    val id = array.getResourceId(index, 0)
                    val text = english.getString(id)
                    val segments = text.split(Regex("%\\d+\\\$s"))
                    val pattern = if (segments.size == 1) null else Regex(segments.joinToString("(.+?)") { Regex.escape(it) }, RegexOption.DOT_MATCHES_ALL)
                    Entry(id, text, pattern)
                }
            } finally { array.recycle() }
        }.also { entries = it }
    }

    fun text(context: Context, value: String): String {
        if (context.resources.configuration.locales[0].language == "en") return value
        val catalog = catalog(context)
        catalog.firstOrNull { it.english == value }?.let { return context.getString(it.id) }
        for (entry in catalog) {
            val match = entry.matcher?.matchEntire(value) ?: continue
            val arguments = match.groupValues.drop(1).map { token -> text(context, token) }.toTypedArray()
            return context.getString(entry.id, *arguments)
        }
        return value
    }
}

@Composable
fun uiText(value: String): String = UiTranslations.text(LocalContext.current, value)

@Composable
fun LocalizedText(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified, fontStyle: FontStyle? = null, fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null, letterSpacing: TextUnit = TextUnit.Unspecified, textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified, overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true, maxLines: Int = Int.MAX_VALUE, minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null, style: TextStyle = LocalTextStyle.current) {
    androidx.compose.material3.Text(text = uiText(text), modifier = modifier, color = color, fontSize = fontSize,
        fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow,
        softWrap = softWrap, maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style)
}

// Legacy illustrated components use annotated brand labels. They retain their span styling.
@Composable
fun LocalizedText(text: AnnotatedString, modifier: Modifier = Modifier, color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified, fontStyle: FontStyle? = null, fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null, letterSpacing: TextUnit = TextUnit.Unspecified, textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified, overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true, maxLines: Int = Int.MAX_VALUE, minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {}, style: TextStyle = LocalTextStyle.current) {
    androidx.compose.material3.Text(text = text, modifier = modifier, color = color, fontSize = fontSize,
        fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow,
        softWrap = softWrap, maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style)
}

package com.abhinav.caisarra.core.ui.components

import android.util.Patterns
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

fun String.linkify(linkColor: Color): AnnotatedString {
    val ranges = mutableListOf<Pair<IntRange, String>>()
    fun collect(pattern: java.util.regex.Pattern, toUrl: (String) -> String?) {
        val matcher = pattern.matcher(this)
        while (matcher.find()) {
            val range = matcher.start() until matcher.end()
            if (ranges.none { it.first.first <= range.last && range.first <= it.first.last }) {
                toUrl(matcher.group())?.let { ranges += range to it }
            }
        }
    }
    collect(Patterns.EMAIL_ADDRESS) { "mailto:$it" }
    collect(Patterns.WEB_URL) { if (it.contains("://")) it else "https://$it" }
    collect(Patterns.PHONE) { phone -> "tel:$phone".takeIf { phone.count(Char::isDigit) >= 7 } }
    val styles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
    return buildAnnotatedString {
        append(this@linkify)
        ranges.forEach { (range, url) ->
            addLink(LinkAnnotation.Url(url, styles), range.first, range.last + 1)
        }
    }
}

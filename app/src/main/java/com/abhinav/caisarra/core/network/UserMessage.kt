package com.abhinav.caisarra.core.network

import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import retrofit2.HttpException
import java.io.IOException

private val technicalText = Regex(
    """exception|https?\b|\bsql|stack ?trace|failed to connect|resolve host|timeout|econn|serializ|json|\bresponse\b|\bnesting\b|\bnull\b|\bjava\.|\bkotlin\.""",
    RegexOption.IGNORE_CASE
)

fun Throwable.userMessage(fallback: String = "Something went wrong. Please try again."): String {
    val text = when {
        this is IOException || this is HttpException -> ApiErrorInterpreter.toUserMessage(this, fallback)
        this::class == Exception::class || this::class == IllegalArgumentException::class || this is IllegalStateException || javaClass.name.startsWith("com.abhinav.") -> message
        else -> null
    }
    return text?.takeIf { it.isNotBlank() && !technicalText.containsMatchIn(it) } ?: fallback
}

package com.abhinav.caisarra.presentation.components

import java.util.Locale

data class CountryOption(
    val code: String,
    val name: String,
    val flag: String
)

object CountryUtils {

    private val countries: List<CountryOption> by lazy {
        Locale.getISOCountries()
            .mapNotNull { rawCode ->
                val code = rawCode.uppercase(Locale.ROOT)

                val locale = Locale("", code)
                val name = locale.getDisplayCountry(Locale.ENGLISH)

                if (name.isBlank() || name == code) {
                    null
                } else {
                    CountryOption(
                        code = code,
                        name = name,
                        flag = countryCodeToFlag(code)
                    )
                }
            }
            .distinctBy { it.code }
            .sortedBy { it.name }
    }

    fun all(): List<CountryOption> = countries

    fun find(code: String?): CountryOption? {
        val normalized = code
            ?.trim()
            ?.uppercase(Locale.ROOT)
            ?: return null

        if (normalized.length != 2) return null

        return countries.firstOrNull {
            it.code == normalized
        }
    }

    fun isValidCode(code: String): Boolean {
        return find(code) != null
    }

    fun countryName(code: String?): String {
        return find(code)?.name ?: "Not set"
    }

    fun flag(code: String?): String {
        return find(code)?.flag.orEmpty()
    }

    private fun countryCodeToFlag(code: String): String {
        if (!code.matches(Regex("[A-Z]{2}"))) {
            return ""
        }

        return buildString {
            code.forEach { character ->
                append(
                    String(Character.toChars(
                        0x1F1E6 + character.code - 'A'.code
                    ))
                )
            }
        }
    }
}
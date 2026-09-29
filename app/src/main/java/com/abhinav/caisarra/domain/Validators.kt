package com.abhinav.caisarra.domain

object Validators {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val usernameRegex = Regex("^[A-Za-z0-9_]{3,20}$")

    fun usernameError(username: String): String? = when {
        username.length < 3 -> "Username must be at least 3 characters."
        username.length > 20 -> "Username must be under 20 characters."
        !usernameRegex.matches(username) -> "Only letters, numbers and underscores allowed."
        else -> null
    }

    fun emailError(email: String): String? =
        if (!emailRegex.matches(email)) "Enter a valid email address." else null

    fun passwordError(password: String): String? = when {
        password.length < 8 -> "Password must be at least 8 characters."
        password.length > 64 -> "Password is too long."
        !password.any { it.isDigit() } -> "Include at least one number."
        !password.any { it.isLetter() } -> "Include at least one letter."
        else -> null
    }

    fun confirmPasswordError(password: String, confirm: String): String? =
        if (password != confirm) "Passwords do not match." else null
}
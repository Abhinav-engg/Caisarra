package com.abhinav.caisarra.features.puzzles.domain

fun puzzleThemeLabel(theme: String): String =
    theme.replace('_', ' ')
        .replace(Regex("(?<=[a-z])(?=[A-Z0-9])"), " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }

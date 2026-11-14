package com.abhinav.caisarra.features.mascot.domain

data class MascotReaction(
    val mood: MascotMood,
    val line: String,
    val detail: String? = null,
    val key: Any = line
)

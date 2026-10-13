package com.abhinav.caisarra.presentation.game.components.setup

import com.abhinav.caisarra.presentation.game.viewmodel.GameMode

data class TimeControlOption(
    val label: String,
    val minutes: Float,
    val incrementSeconds: Float,
    val mode: GameMode
)

package com.abhinav.caisarra.presentation.game.viewmodel

fun gameModeFor(minutes: Float): GameMode = when {
    minutes >= 1440f -> GameMode.DAILY
    minutes < 3f -> GameMode.BULLET
    minutes < 10f -> GameMode.BLITZ
    else -> GameMode.RAPID
}

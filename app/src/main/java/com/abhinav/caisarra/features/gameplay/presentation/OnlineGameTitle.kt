package com.abhinav.caisarra.features.gameplay.presentation

import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.viewmodel.gameModeFor

fun onlineGameTitle(state: GameUiState): String =
    if (state.isDailyMode) {
        "DAILY | ${state.dailyDaysPerMove}d"
    } else {
        "${gameModeFor(state.timeControlMinutes.toFloat()).label.uppercase()} | ${state.timeControlMinutes}+${state.incrementSeconds}"
    }

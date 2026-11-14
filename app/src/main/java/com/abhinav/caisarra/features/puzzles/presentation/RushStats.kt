package com.abhinav.caisarra.features.puzzles.presentation

import com.abhinav.caisarra.presentation.puzzles.RushUiState
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.StatsCyan

fun rushStats(state: RushUiState.Playing): List<PuzzleStat> {
    val seconds = state.secondsRemaining
    return listOf(
        if (seconds == null) {
            PuzzleStat("None", "Time limit")
        } else {
            PuzzleStat("%02d:%02d".format(seconds / 60, seconds % 60), "Time left", if (seconds <= 30) RedNormal else StatsCyan)
        },
        PuzzleStat("${state.score}", "Solved"),
        if (state.mode == "survival") {
            PuzzleStat("${state.strikesLeft} / 3", "Lives left", if (state.strikesLeft <= 1) RedNormal else StatsCyan)
        } else {
            PuzzleStat("${state.strikes} / 3", "Mistakes", if (state.strikes >= 2) RedNormal else StatsCyan)
        }
    )
}

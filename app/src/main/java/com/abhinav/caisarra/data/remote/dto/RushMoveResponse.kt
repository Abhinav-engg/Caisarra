package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RushMoveResponse(
    val correct: Boolean = false,
    val score: Int = 0,
    val strikes: Int = 0,
    @SerialName("strikes_left") val strikesLeft: Int = 3,
    @SerialName("game_over") val gameOver: Boolean = false,
    val reason: String? = null,
    @SerialName("next_puzzle") val nextPuzzle: RushPuzzleData? = null,
    @SerialName("next_move") val nextMove: String? = null,
    val summary: RushSummaryDto? = null
)

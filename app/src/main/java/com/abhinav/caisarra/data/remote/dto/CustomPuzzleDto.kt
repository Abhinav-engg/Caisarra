package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PuzzleThemesResponse(
    val themes: List<PuzzleThemeDto> = emptyList()
)

@Serializable
data class PuzzleThemeDto(
    val id: String,
    val name: String,
    val count: Int
)

@Serializable
data class CustomPuzzleResponse(
    @SerialName("puzzle_id")
    val puzzleId: String,

    val fen: String,

    @SerialName("initial_move")
    val initialMove: String,

    val rating: Int,

    val themes: List<String> = emptyList()
)

@Serializable
data class CustomPuzzleAttemptRequest(
    @SerialName("puzzle_id")
    val puzzleId: String,

    val move: String,

    @SerialName("time_taken_ms")
    val timeTakenMs: Long,

    @SerialName("move_index")
    val moveIndex: Int? = null,

    val rated: Boolean = false
)

@Serializable
data class CustomPuzzleAttemptResponse(
    val correct: Boolean,

    val solved: Boolean,

    @SerialName("next_move")
    val nextMove: String? = null,

    @SerialName("puzzle_id")
    val puzzleId: String,

    @SerialName("attempted_moves")
    val attemptedMoves: List<String> = emptyList(),

    @SerialName("solution_moves")
    val solutionMoves: List<String> = emptyList(),

    @SerialName("rating_change")
    val ratingChange: Int = 0,

    @SerialName("user_rating_before")
    val userRatingBefore: Int = 0,

    @SerialName("user_rating_after")
    val userRatingAfter: Int = 0,

    val rated: Boolean = false
)
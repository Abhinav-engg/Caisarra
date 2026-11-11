package com.abhinav.caisarra.features.minimates.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MiniMatesOverviewDto(
    @SerialName("current_level") val currentLevel: Int = 1,
    @SerialName("total_stars") val totalStars: Int = 0,
    @SerialName("completed_levels") val completedLevels: Int = 0,
    val chapters: List<MiniMatesChapterDto> = emptyList()
)

@Serializable
data class MiniMatesChapterDto(
    val id: Int,
    val name: String,
    val description: String = "",
    @SerialName("target_moves") val targetMoves: Int = 1,
    val levels: List<MiniMatesLevelSummaryDto> = emptyList()
)

@Serializable
data class MiniMatesLevelSummaryDto(
    @SerialName("level_number") val levelNumber: Int,
    @SerialName("target_moves") val targetMoves: Int = 1,
    val title: String = "",
    val stars: Int = 0,
    val completed: Boolean = false,
    val unlocked: Boolean = false
)

@Serializable
data class MiniMatesLevelDto(
    @SerialName("level_number") val levelNumber: Int,
    @SerialName("chapter_name") val chapterName: String = "",
    @SerialName("target_moves") val targetMoves: Int = 1,
    val title: String = "",
    val fen: String,
    @SerialName("player_color") val playerColor: String = "white",
    val viewport: MiniMatesViewportDto
)

@Serializable
data class MiniMatesViewportDto(
    @SerialName("start_file") val startFile: String,
    @SerialName("end_rank") val endRank: Int,
    val cols: Int,
    val rows: Int
)

@Serializable
data class MiniMatesHintRequest(@SerialName("move_index") val moveIndex: Int)

@Serializable
data class MiniMatesHintDto(
    @SerialName("from_square") val fromSquare: String? = null,
    val message: String? = null
)

@Serializable
data class MiniMatesMoveRequest(
    val move: String,
    @SerialName("move_index") val moveIndex: Int,
    @SerialName("time_taken_ms") val timeTakenMs: Long
)

@Serializable
data class MiniMatesMoveDto(
    val correct: Boolean = false,
    @SerialName("is_checkmate") val isCheckmate: Boolean = false,
    @SerialName("next_move") val nextMove: String? = null,
    val stars: Int = 0,
    @SerialName("next_unlocked_level") val nextUnlockedLevel: Int? = null,
    val message: String? = null
)

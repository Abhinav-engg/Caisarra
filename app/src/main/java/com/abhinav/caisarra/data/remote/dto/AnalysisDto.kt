package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisDto(
    val id: String,
    @SerialName("game_id") val gameId: String,
    val status: String,
    @SerialName("engine_version") val engineVersion: String = "",
    val depth: Int = 0
)

@Serializable
data class AnalysisSummaryStatsDto(
    @SerialName("accuracy_white") val accuracyWhite: Double = 0.0,
    @SerialName("accuracy_black") val accuracyBlack: Double = 0.0,
    @SerialName("rating_white") val ratingWhite: Int = 0,
    @SerialName("rating_black") val ratingBlack: Int = 0,
    @SerialName("brilliant_white") val brilliantWhite: Int = 0,
    @SerialName("great_white") val greatWhite: Int = 0,
    @SerialName("best_white") val bestWhite: Int = 0,
    @SerialName("good_white") val goodWhite: Int = 0,
    @SerialName("inaccuracies_white") val inaccuraciesWhite: Int = 0,
    @SerialName("mistakes_white") val mistakesWhite: Int = 0,
    @SerialName("misses_white") val missesWhite: Int = 0,
    @SerialName("blunders_white") val blundersWhite: Int = 0,
    @SerialName("brilliant_black") val brilliantBlack: Int = 0,
    @SerialName("great_black") val greatBlack: Int = 0,
    @SerialName("best_black") val bestBlack: Int = 0,
    @SerialName("good_black") val goodBlack: Int = 0,
    @SerialName("inaccuracies_black") val inaccuraciesBlack: Int = 0,
    @SerialName("mistakes_black") val mistakesBlack: Int = 0,
    @SerialName("misses_black") val missesBlack: Int = 0,
    @SerialName("blunders_black") val blundersBlack: Int = 0
)

@Serializable
data class AnalysisPhaseDto(
    val phase: String,
    @SerialName("accuracy_white") val accuracyWhite: Double = 0.0,
    @SerialName("accuracy_black") val accuracyBlack: Double = 0.0,
    @SerialName("classification_white") val classificationWhite: String = "",
    @SerialName("classification_black") val classificationBlack: String = "",
    @SerialName("symbol_white") val symbolWhite: String = "",
    @SerialName("symbol_black") val symbolBlack: String = ""
)

@Serializable
data class EvalPointDto(
    @SerialName("move_number") val moveNumber: Int,
    @SerialName("player_color") val playerColor: String,
    @SerialName("eval_white") val evalWhite: Int
)

@Serializable
data class AnalysisSummaryResponse(
    val analysis: AnalysisDto,
    @SerialName("summary_stats") val summaryStats: AnalysisSummaryStatsDto = AnalysisSummaryStatsDto(),
    val phases: List<AnalysisPhaseDto> = emptyList(),
    @SerialName("eval_history") val evalHistory: List<EvalPointDto> = emptyList()
)

@Serializable
data class MoveAnalysisDto(
    val id: String = "",
    @SerialName("move_number") val moveNumber: Int,
    @SerialName("player_color") val playerColor: String,
    @SerialName("played_move") val playedMove: String,
    @SerialName("position_fen") val positionFen: String = "",
    @SerialName("eval_before") val evalBefore: Int = 0,
    @SerialName("eval_after") val evalAfter: Int = 0,
    @SerialName("best_move") val bestMove: String = "",
    val pv: List<String> = emptyList(),
    @SerialName("centipawn_loss") val centipawnLoss: Int = 0,
    val classification: String = ""
)
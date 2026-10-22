package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RushStatsResponse(
    @SerialName("best_score_3min") val bestScore3min: Int = 0,
    @SerialName("best_score_5min") val bestScore5min: Int = 0,
    @SerialName("best_score_survival") val bestScoreSurvival: Int = 0,
    @SerialName("total_runs") val totalRuns: Int = 0
)

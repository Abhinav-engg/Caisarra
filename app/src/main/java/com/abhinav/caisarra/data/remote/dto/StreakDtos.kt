package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreakDayDto(
    val date: String = "",
    val weekday: String = "",
    val status: String = "empty"
)

@Serializable
data class DailyStreakResponse(
    @SerialName("current_streak") val currentStreak: Int = 0,
    @SerialName("max_streak") val maxStreak: Int = 0,
    @SerialName("best_streak") val bestStreak: Int = 0,
    @SerialName("today_completed") val todayCompleted: Boolean = false,
    @SerialName("streak_status") val streakStatus: String = "active",
    val timezone: String = "",
    @SerialName("freezes_available") val freezesAvailable: Int = 0,
    val week: List<StreakDayDto> = emptyList()
)

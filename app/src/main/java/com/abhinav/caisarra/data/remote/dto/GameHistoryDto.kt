
package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameHistoryDto(
    val id: String,

    @SerialName("white_player_id")
    val whitePlayerId: Long,

    @SerialName("black_player_id")
    val blackPlayerId: Long,

    @SerialName("time_control_minutes")
    val timeControlMinutes: Int,
    val rated: Boolean,

    @SerialName("time_control_mode")
    val timeControlMode: String,

    val position: String,
    val status: String,
    val result: String? = null,

    @SerialName("end_reason")
    val endReason: String? = null,

    @SerialName("initial_time_ms")
    val initialTimeMs: Long,

    @SerialName("increment_ms")
    val incrementMs: Long,

    @SerialName("white_time_ms")
    val whiteTimeMs: Long,

    @SerialName("black_time_ms")
    val blackTimeMs: Long,

    @SerialName("current_turn")
    val currentTurn: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("started_at")
    val startedAt: String? = null,
    @SerialName("ended_at")
    val endedAt: String? = null
)

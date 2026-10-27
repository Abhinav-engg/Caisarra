package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WatchInviteResponse(
    @SerialName("watch_code") val watchCode: String = "",
    @SerialName("game_id") val gameId: String = "",
    val link: String = ""
)

@Serializable
data class ActiveGameDto(
    @SerialName("id") val id: String = "",
    @SerialName("game_id") val gameId: String? = null,
    @SerialName("watch_code") val watchCode: String? = null,
    @SerialName("link") val link: String? = null,
    @SerialName("white_player_id") val whitePlayerId: Long = 0L,
    @SerialName("black_player_id") val blackPlayerId: Long = 0L,
    @SerialName("white_username") val whiteUsername: String? = null,
    @SerialName("black_username") val blackUsername: String? = null,
    @SerialName("white_rating") val whiteRating: Int? = null,
    @SerialName("black_rating") val blackRating: Int? = null,
    @SerialName("time_control_minutes") val timeControlMinutes: Int = 10,
    @SerialName("increment_seconds") val incrementSeconds: Int = 0,
    @SerialName("time_control_mode") val timeControlMode: String = "realtime",
    val rated: Boolean = true,
    val position: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    val status: String = "active",
    @SerialName("current_turn") val currentTurn: String = "white",
    @SerialName("spectator_count") val spectatorCount: Int = 0,
    @SerialName("allow_public_spectators") val allowPublicSpectators: Boolean = true
) {
    val effectiveGameId: String get() = gameId?.takeIf { it.isNotBlank() } ?: id
    val effectiveWatchCode: String? get() = watchCode?.takeIf { it.isNotBlank() }
}

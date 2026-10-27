package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class SocketMove(
    val move: String = ""
)

@Serializable
sealed interface SocketIncoming {

    @Serializable
    @SerialName("game_start")
    data class GameStart(
        @SerialName("game_id") val gameId: String = ""
    ) : SocketIncoming

    @Serializable
    @SerialName("game_state")
    data class GameState(
        @SerialName("game_id") val gameId: String = "",
        val position: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        val status: String = "waiting",
        @SerialName("white_time_ms") val whiteTimeMs: Long = 0L,
        @SerialName("black_time_ms") val blackTimeMs: Long = 0L,
        @SerialName("current_turn") val currentTurn: String = "white",
        @SerialName("turn_started_at") val turnStartedAt: String = "",
        @SerialName("white_player_id") val whitePlayerId: Long = 0L,
        @SerialName("black_player_id") val blackPlayerId: Long = 0L,
        @SerialName("white_username") val whiteUsername: String? = null,
        @SerialName("black_username") val blackUsername: String? = null,
        @SerialName("last_move") val lastMove: String? = null,
        val moves: List<SocketMove>? = null,
        val result: String? = null,
        @SerialName("end_reason") val endReason: String? = null,
        @SerialName("draw_offered_by") val drawOfferedBy: Long? = null,
        @SerialName("time_control_minutes") val timeControlMinutes: Int = 10,
        val rated: Boolean? = true
    ) : SocketIncoming

    @Serializable
    @SerialName("waiting")
    data class Waiting(
        @SerialName("game_id") val gameId: String = ""
    ) : SocketIncoming

    @Serializable
    @SerialName("draw_offer")
    data class DrawOffer(
        @SerialName("offered_by") val offeredBy: Long = 0L
    ) : SocketIncoming

    @Serializable
    @SerialName("draw_declined")
    data class DrawDeclined(
        @SerialName("declined_by") val declinedBy: Long = 0L
    ) : SocketIncoming

    @Serializable
    @SerialName("error")
    data class Error(
        val message: String = ""
    ) : SocketIncoming

    @Serializable
    @SerialName("spectator_count")
    data class SpectatorCount(
        @SerialName("game_id") val gameId: String = "",
        val count: Int = 0
    ) : SocketIncoming

    @Serializable
    @SerialName("watch_ended")
    data class WatchEnded(
        @SerialName("game_id") val gameId: String = ""
    ) : SocketIncoming

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @SerialName("chat")
    data class ChatMessage(
        val id: String,
        @SerialName("game_id") val gameId: String,
        @SerialName("sender_id") @JsonNames("user_id") val senderId: Long,
        val message: String,
        @SerialName("created_at") val createdAt: String,
        @JsonNames("sender_username", "user_name") val username: String? = null,
        @SerialName("avatar_url") @JsonNames("sender_avatar_url") val avatarUrl: String? = null
    ) : SocketIncoming
}
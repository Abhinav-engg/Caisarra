package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
sealed interface SocketIncoming {

    @Serializable
    @SerialName("game_start")
    data class GameStart(
        @SerialName("game_id") val gameId: String
    ) : SocketIncoming
    @Serializable
    @SerialName("game_state")
    data class GameState(
        @SerialName("game_id") val gameId: String,
        val position: String,
        val status: String,
        @SerialName("white_time_ms") val whiteTimeMs: Long,
        @SerialName("black_time_ms") val blackTimeMs: Long,
        @SerialName("current_turn") val currentTurn: String,
        @SerialName("turn_started_at") val turnStartedAt: String,
        @SerialName("white_username") val whiteUsername: String? = null,
        @SerialName("black_username") val blackUsername: String? = null,
        @SerialName("last_move") val lastMove: String? = null,
        val moves: List<String>? = null,
        val result: String? = null,
        @SerialName("end_reason") val endReason: String? = null,
        @SerialName("draw_offered_by") val drawOfferedBy: Long? = null
    ) : SocketIncoming

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @SerialName("chat_message")
    data class ChatMessage(
        val id: String,
        @SerialName("game_id") val gameId: String,
        @SerialName("sender_id") @JsonNames("user_id") val senderId: Long,
        val message: String,
        @SerialName("created_at") val createdAt: String
    ) : SocketIncoming
}
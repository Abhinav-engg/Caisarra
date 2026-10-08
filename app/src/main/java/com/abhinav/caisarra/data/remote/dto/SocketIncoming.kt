package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
        @SerialName("turn_started_at") val turnStartedAt: String
    ) : SocketIncoming

    @Serializable
    @SerialName("chat_message")
    data class ChatMessage(
        val id: String,
        @SerialName("game_id") val gameId: String,
        @SerialName("user_id") val userId: Long,
        val message: String,
        @SerialName("created_at") val createdAt: String
    ) : SocketIncoming
}
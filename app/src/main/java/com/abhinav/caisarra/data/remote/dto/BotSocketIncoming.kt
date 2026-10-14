package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BotSocketIncoming {

    @Serializable
    @SerialName("game_state")
    data class GameState(val game: BotGameDto) : BotSocketIncoming()

    @Serializable
    @SerialName("bot_thinking")
    data object BotThinking : BotSocketIncoming()

    @Serializable
    @SerialName("error")
    data class Error(val message: String) : BotSocketIncoming()
}
package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BotSocketOutgoing {

    @Serializable
    @SerialName("move")
    data class Move(val move: String) : BotSocketOutgoing()

    @Serializable
    @SerialName("resign")
    data object Resign : BotSocketOutgoing()
}
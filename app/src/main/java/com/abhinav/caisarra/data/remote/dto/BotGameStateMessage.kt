package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("game_state")
data class BotGameStateMessage(
    val game: BotGame
) : BotSocketIncoming

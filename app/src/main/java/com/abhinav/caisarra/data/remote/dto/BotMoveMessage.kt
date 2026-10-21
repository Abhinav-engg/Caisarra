package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("move")
data class BotMoveMessage(
    val move: String
) : BotSocketOutgoing

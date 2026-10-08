package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface SocketOutgoing {

    @Serializable
    @SerialName("move")
    data class Move(val move: String) : SocketOutgoing

    @Serializable
    @SerialName("chat")
    data class Chat(val message: String) : SocketOutgoing

    @Serializable
    @SerialName("resign")
    data object Resign : SocketOutgoing

    @Serializable
    @SerialName("offer_draw")
    data object OfferDraw : SocketOutgoing

    @Serializable
    @SerialName("accept_draw")
    data object AcceptDraw : SocketOutgoing
}
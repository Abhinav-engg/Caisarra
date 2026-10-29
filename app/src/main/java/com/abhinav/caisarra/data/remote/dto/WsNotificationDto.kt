package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class WsNotificationEnvelope(
    val type: String = "notification",
    val notification: NotificationItemDto? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WsNotificationPayload(
    val type: String = "",
    val event: String? = null,
    val title: String? = null,
    val body: String? = null,
    val data: WsNotificationData? = null,
    val code: String? = null,
    @SerialName("game_id") @JsonNames("gameId") val gameId: String? = null,
    val link: String? = null,
    @SerialName("user_id") @JsonNames("userId", "opponent_id", "challenger_id") val userId: Long? = null,
    val username: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WsNotificationData(
    val code: String? = null,
    @SerialName("game_id") @JsonNames("gameId") val gameId: String? = null,
    val link: String? = null,
    @SerialName("user_id") @JsonNames("userId", "opponent_id", "challenger_id") val userId: Long? = null,
    val username: String? = null
)

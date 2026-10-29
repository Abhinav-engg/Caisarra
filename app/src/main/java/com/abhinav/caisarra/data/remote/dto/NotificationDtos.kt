package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@Serializable
data class RegisterDeviceRequest(
    val token: String,
    val platform: String = "android"
)

@Serializable
data class DeleteDeviceRequest(
    val token: String
)

@Serializable
data class SimpleApiResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class NotificationItemDto(
    val id: Long,
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val data: JsonObject? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class NotificationsData(
    val notifications: List<NotificationItemDto> = emptyList()
)

@Serializable
data class UnreadCountData(
    val count: Int = 0
)

fun NotificationItemDto.getStringData(key: String): String? =
    data?.get(key)?.jsonPrimitive?.contentOrNull

fun NotificationItemDto.getLongData(key: String): Long? =
    data?.get(key)?.jsonPrimitive?.longOrNull ?: getStringData(key)?.toLongOrNull()

fun NotificationItemDto.getIntData(key: String): Int? =
    data?.get(key)?.jsonPrimitive?.intOrNull ?: getStringData(key)?.toIntOrNull()

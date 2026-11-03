package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PuzzleHistoryResponse(
    val total: Int = 0,
    @SerialName("items") val items: List<PuzzleHistoryItemDto> = emptyList(),
    @SerialName("attempts") val attempts: List<PuzzleHistoryItemDto> = emptyList()
) {
    val allAttempts: List<PuzzleHistoryItemDto>
        get() = items.ifEmpty { attempts }
}

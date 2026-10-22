package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PuzzleHistoryResponse(
    val total: Int = 0,
    val attempts: List<PuzzleHistoryItemDto> = emptyList()
)

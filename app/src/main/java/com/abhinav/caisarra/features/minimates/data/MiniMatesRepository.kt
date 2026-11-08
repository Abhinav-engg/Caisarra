package com.abhinav.caisarra.features.minimates.data

import com.abhinav.caisarra.data.repository.apiCall

class MiniMatesRepository(private val service: MiniMatesService) {
    suspend fun overview() = apiCall { service.overview() }
    suspend fun level(level: Int) = apiCall { service.level(level) }
    suspend fun hint(level: Int, moveIndex: Int) = apiCall { service.hint(level, MiniMatesHintRequest(moveIndex)) }
    suspend fun move(level: Int, request: MiniMatesMoveRequest) = apiCall { service.move(level, request) }
}

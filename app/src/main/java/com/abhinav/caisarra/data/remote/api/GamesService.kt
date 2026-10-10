
package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.GameHistoryDto
import retrofit2.http.GET

interface GamesService {

    @GET("api/games/history")
    suspend fun getGameHistory(): List<GameHistoryDto>
}

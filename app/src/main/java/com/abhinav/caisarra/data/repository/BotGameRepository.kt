package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.BotGameDto
import com.abhinav.caisarra.data.remote.dto.CreateBotGameRequest

class BotGameRepository private constructor(context: Context) {

    private val service = RetrofitInstance.createBotGameService(TokenManager.get(context))

    suspend fun createGame(level: String, rating: Int?, color: String): Result<BotGameDto> =
        call { service.createBotGame(CreateBotGameRequest(level, rating, color)) }

    suspend fun getGame(gameId: String): Result<BotGameDto> =
        call { service.getBotGame(gameId) }

    private suspend fun <T> call(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorInterpreter.toUserMessage(e)))
        }
    }

    companion object {
        @Volatile
        private var instance: BotGameRepository? = null

        fun get(context: Context): BotGameRepository =
            instance ?: synchronized(this) {
                instance ?: BotGameRepository(context.applicationContext).also { instance = it }
            }
    }
}
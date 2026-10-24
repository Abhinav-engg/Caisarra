package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.remote.api.BotGameApiService
import com.abhinav.caisarra.data.remote.dto.Bot
import com.abhinav.caisarra.data.remote.dto.BotGame
import com.abhinav.caisarra.data.remote.dto.CreateBotGameRequest
import com.abhinav.caisarra.data.remote.dto.ErrorResponse
import kotlinx.serialization.json.Json
import retrofit2.Response

class BotGameRepository(private val service: BotGameApiService) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun getBots(): Result<List<Bot>> = safeCall {
        val response = service.getBots()
        if (response.isSuccessful) {
            response.body()?.bots ?: emptyList()
        } else {
            throw parseException(response)
        }
    }

    suspend fun createGame(botId: String?, rating: Int?, color: String?): Result<BotGame> = safeCall {
        val request = CreateBotGameRequest(botId = botId, rating = rating, color = color)
        val response = service.createGame(request)
        if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            throw parseException(response)
        }
    }

    suspend fun getGames(): Result<List<BotGame>> = safeCall {
        val response = service.getGames()
        if (response.isSuccessful) {
            response.body()?.games ?: emptyList()
        } else {
            throw parseException(response)
        }
    }

    suspend fun getGame(gameId: String): Result<BotGame> = safeCall {
        val response = service.getGame(gameId)
        if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            throw parseException(response)
        }
    }

    private fun <T> parseException(response: Response<T>): Exception {
        val errorBody = response.errorBody()?.string()
        val parsedMessage = if (!errorBody.isNullOrBlank()) {
            try {
                val error = json.decodeFromString<ErrorResponse>(errorBody)
                error.error?.takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        } else null

        val message = parsedMessage ?: when (response.code()) {
            400 -> "Invalid request"
            401 -> "Unauthorized"
            403 -> "Action not allowed"
            404 -> "Bot game not found"
            409 -> "You can play at most 5 bot games at a time"
            else -> "Server error (${response.code()})"
        }

        return if (response.code() == 409) {
            ActiveGamesLimitException(message)
        } else {
            Exception(message)
        }
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }

    companion object {
        @Volatile
        private var instance: BotGameRepository? = null

        fun get(context: Context): BotGameRepository =
            instance ?: synchronized(this) {
                instance ?: BotGameRepository(
                    AuthRepository.get(context).createBotGameService()
                ).also { instance = it }
            }
    }
}

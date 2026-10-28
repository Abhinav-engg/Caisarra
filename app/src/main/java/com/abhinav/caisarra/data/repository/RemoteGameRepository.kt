package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.local.entity.RecordStatus
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.api.GamesService
import com.abhinav.caisarra.data.remote.dto.ActiveGameDto
import com.abhinav.caisarra.data.remote.dto.GameHistoryDto
import com.abhinav.caisarra.data.remote.dto.GameMoveDto
import com.abhinav.caisarra.data.remote.dto.WatchInviteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.time.Instant

class RemoteGameRepository(
    private val gamesService: GamesService
) {

    suspend fun getActiveGames(): Result<List<ActiveGameDto>> = withContext(Dispatchers.IO) {
        runCatching { gamesService.getActiveGames() }
    }

    suspend fun createWatchInvite(gameId: String): Result<WatchInviteResponse> = withContext(Dispatchers.IO) {
        runCatching { gamesService.createWatchInvite(gameId) }
    }

    suspend fun getGameHistory(
        username: String
    ): List<GameEntity> {
        return gamesService.getGameHistory().map { remote ->
            remote.toGameEntity(username)
        }
    }

    suspend fun getGame(
        gameId: String
    ): GameHistoryDto {
        return gamesService.getGame(gameId)
    }

    suspend fun getGameMoves(
        gameId: String
    ): List<GameMoveDto> {
        return gamesService.getGameMoves(gameId)
            .sortedBy { it.moveNumber }
    }

    suspend fun getRawGameHistory(): List<GameHistoryDto> {
        return gamesService.getGameHistory()
    }

    suspend fun getPlayerGames(
        username: String,
        mode: String? = null,
        limit: Int? = null
    ): List<GameEntity> = withContext(Dispatchers.IO) {
        val dtos = runCatching {
            val responseBody = gamesService.getPlayerGames(
                username = username.trim(),
                mode = mode?.trim()?.ifEmpty { null },
                limit = limit
            )
            val bodyString = responseBody.string()
            val element = RetrofitInstance.json.parseToJsonElement(bodyString)
            when (element) {
                is JsonArray -> {
                    RetrofitInstance.json.decodeFromJsonElement<List<GameHistoryDto>>(element)
                }
                is JsonObject -> {
                    val dataElement = element["data"]
                    when {
                        dataElement is JsonArray -> {
                            RetrofitInstance.json.decodeFromJsonElement<List<GameHistoryDto>>(dataElement)
                        }
                        dataElement is JsonObject && dataElement["games"] is JsonArray -> {
                            RetrofitInstance.json.decodeFromJsonElement<List<GameHistoryDto>>(dataElement["games"]!!)
                        }
                        element["games"] is JsonArray -> {
                            RetrofitInstance.json.decodeFromJsonElement<List<GameHistoryDto>>(element["games"]!!)
                        }
                        else -> emptyList()
                    }
                }
                else -> emptyList()
            }
        }.getOrElse {
            emptyList()
        }

        dtos.map { it.toGameEntity(username) }
    }

    fun GameHistoryDto.toGameEntity(
        username: String
    ): GameEntity {
        val isFinished = status.equals(
            "finished",
            ignoreCase = true
        )

        return GameEntity(
            id = id,
            ownerId = username,
            whiteName = whiteUsername?.takeIf { it.isNotBlank() } ?: "White #$whitePlayerId",
            blackName = blackUsername?.takeIf { it.isNotBlank() } ?: "Black #$blackPlayerId",
            timeControlMinutes = timeControlMinutes,
            incrementSeconds = (incrementMs / 1000L).toInt(),
            moves = "",
            moveTimes = "",
            whiteTimeMs = whiteTimeMs,
            blackTimeMs = blackTimeMs,

            result = when (result?.lowercase()) {
                "white_win" -> "WHITE_WINS"
                "black_win" -> "BLACK_WINS"
                "draw" -> "DRAW"
                else -> null
            },

            endReason = endReason,

            startedAt = parseTimestamp(
                startedAt ?: createdAt
            ),

            endedAt = endedAt?.let(::parseTimestamp),

            status = if (isFinished) {
                RecordStatus.Synced
            } else {
                RecordStatus.InProgress
            }
        )
    }

    private fun parseTimestamp(value: String): Long {
        return runCatching {
            Instant.parse(value).toEpochMilli()
        }.getOrElse {
            System.currentTimeMillis()
        }
    }
}
package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.ChessDatabase
import com.abhinav.caisarra.data.local.dao.GameDao
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.local.entity.RecordStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GameRepository(private val dao: GameDao) {

    suspend fun startGame(
        ownerId: String,
        whiteName: String,
        blackName: String,
        timeControlMinutes: Int?,
        incrementSeconds: Int
    ): GameEntity {
        val initialMs = (timeControlMinutes ?: 0) * 60_000L
        val game = GameEntity(
            id = UUID.randomUUID().toString(),
            ownerId = ownerId,
            whiteName = whiteName,
            blackName = blackName,
            timeControlMinutes = timeControlMinutes,
            incrementSeconds = incrementSeconds,
            moves = "",
            whiteTimeMs = initialMs,
            blackTimeMs = initialMs,
            result = null,
            endReason = null,
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            status = RecordStatus.InProgress
        )
        dao.upsert(game)
        return game
    }

    suspend fun saveProgress(
        id: String,
        moves: List<String>,
        whiteTimeMs: Long,
        blackTimeMs: Long
    ) {
        val game = dao.getById(id) ?: return
        dao.upsert(
            game.copy(
                moves = moves.joinToString(" "),
                whiteTimeMs = whiteTimeMs,
                blackTimeMs = blackTimeMs
            )
        )
    }

    suspend fun finishGame(
        id: String,
        moves: List<String>,
        whiteTimeMs: Long,
        blackTimeMs: Long,
        result: String,
        endReason: String,
        canSync: Boolean
    ) {
        val game = dao.getById(id) ?: return
        dao.upsert(
            game.copy(
                moves = moves.joinToString(" "),
                whiteTimeMs = whiteTimeMs,
                blackTimeMs = blackTimeMs,
                result = result,
                endReason = endReason,
                endedAt = System.currentTimeMillis(),
                status = if (canSync) RecordStatus.Pending else RecordStatus.LocalOnly
            )
        )
    }

    suspend fun getUnfinished(ownerId: String): GameEntity? = dao.getUnfinished(ownerId)

    fun observeFinished(ownerId: String): Flow<List<GameEntity>> = dao.observeFinished(ownerId)

    suspend fun discard(id: String) = dao.deleteById(id)

    suspend fun getPending(ownerId: String): List<GameEntity> =
        dao.getByStatus(ownerId, RecordStatus.Pending)

    suspend fun markSynced(id: String) = dao.updateStatus(id, RecordStatus.Synced)

    suspend fun markFailed(id: String) = dao.updateStatus(id, RecordStatus.Failed)

    fun movesOf(game: GameEntity): List<String> = game.moves.split(" ").filter { it.isNotEmpty() }

    companion object {
        @Volatile
        private var instance: GameRepository? = null

        fun get(context: Context): GameRepository =
            instance ?: synchronized(this) {
                instance ?: GameRepository(ChessDatabase.get(context).gameDao()).also { instance = it }
            }
    }
}
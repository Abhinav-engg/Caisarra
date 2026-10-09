package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.ChessDatabase
import com.abhinav.caisarra.data.local.dao.GameDao
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.local.entity.RecordStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GameRepository(
    private val dao: GameDao
) {

    suspend fun startGame(
        ownerId: String,
        whiteName: String,
        blackName: String,
        timeControlMinutes: Int?,
        incrementSeconds: Int
    ): GameEntity {

        val initialMs =
            (timeControlMinutes ?: 0) * 60_000L

        val game = GameEntity(
            id = UUID.randomUUID().toString(),

            ownerId = ownerId,

            whiteName = whiteName,
            blackName = blackName,

            timeControlMinutes = timeControlMinutes,

            incrementSeconds = incrementSeconds,

            moves = "",

            moveTimes = "",

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
        moveTimes: List<Pair<Long, Long>>,
        whiteTimeMs: Long,
        blackTimeMs: Long
    ) {

        val game =
            dao.getById(id)
                ?: return

        dao.upsert(
            game.copy(

                moves = moves.joinToString(" "),

                moveTimes = encodeMoveTimes(moveTimes),
                whiteTimeMs = whiteTimeMs,

                blackTimeMs = blackTimeMs
            )
        )
    }

    suspend fun finishGame(
        id: String,
        moves: List<String>,
        moveTimes: List<Pair<Long, Long>>,
        whiteTimeMs: Long,
        blackTimeMs: Long,
        result: String,
        endReason: String,
        canSync: Boolean
    ) {

        val game =
            dao.getById(id)
                ?: return

        dao.upsert(
            game.copy(

                moves = moves.joinToString(" "),
                moveTimes = encodeMoveTimes(moveTimes),
                whiteTimeMs = whiteTimeMs,
                blackTimeMs = blackTimeMs,

                result = result,

                endReason = endReason,
                endedAt = System.currentTimeMillis(),

                status =
                    if (canSync) {
                        RecordStatus.Pending
                    } else {
                        RecordStatus.LocalOnly
                    }
            )
        )
    }

    suspend fun getById(
        id: String
    ): GameEntity? =
        dao.getById(id)

    suspend fun getUnfinished(
        ownerId: String
    ): GameEntity? =
        dao.getUnfinished(ownerId)

    fun observeFinished(
        ownerId: String
    ): Flow<List<GameEntity>> =
        dao.observeFinished(ownerId)

    suspend fun discard(
        id: String
    ) =
        dao.deleteById(id)

    suspend fun getPending(
        ownerId: String
    ): List<GameEntity> =
        dao.getByStatus(
            ownerId,
            RecordStatus.Pending
        )

    suspend fun markSynced(
        id: String
    ) =
        dao.updateStatus(
            id,
            RecordStatus.Synced
        )

    suspend fun markFailed(
        id: String
    ) =
        dao.updateStatus(
            id,
            RecordStatus.Failed
        )

    fun movesOf(
        game: GameEntity
    ): List<String> {

        return game.moves
            .split(" ")
            .filter {
                it.isNotEmpty()
            }
    }

    fun moveTimesOf(
        game: GameEntity
    ): List<Pair<Long, Long>> {

        return game.moveTimes
            .split("|")
            .filter {
                it.isNotBlank()
            }
            .mapNotNull { item ->

                val values = item.split(",")

                if (values.size != 2) {
                    return@mapNotNull null
                }

                val white = values[0].toLongOrNull()
                        ?: return@mapNotNull null

                val black = values[1].toLongOrNull()
                        ?: return@mapNotNull null

                white to black
            }
    }

    private fun encodeMoveTimes(
        moveTimes: List<Pair<Long, Long>>
    ): String {

        return moveTimes.joinToString("|") {

            val white = it.first
            val black = it.second

            "$white,$black"
        }
    }

    companion object {
        @Volatile
        private var instance: GameRepository? = null

        fun get(
            context: Context
        ): GameRepository =

            instance ?: synchronized(this) {

                instance ?: GameRepository(
                    ChessDatabase
                        .get(context)
                        .gameDao()
                ).also {
                    instance = it
                }
            }
    }
}
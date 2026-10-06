package com.abhinav.caisarra.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.local.entity.RecordStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Upsert
    suspend fun upsert(game: GameEntity)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: String): GameEntity?

    @Query("SELECT * FROM games WHERE ownerId = :ownerId AND endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getUnfinished(ownerId: String): GameEntity?

    @Query("SELECT * FROM games WHERE ownerId = :ownerId AND endedAt IS NOT NULL ORDER BY endedAt DESC")
    fun observeFinished(ownerId: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE ownerId = :ownerId AND status = :status")
    suspend fun getByStatus(ownerId: String, status: RecordStatus): List<GameEntity>

    @Query("UPDATE games SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: RecordStatus)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: String)
}
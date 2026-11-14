package com.abhinav.caisarra.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CachedResponseDao {

    @Upsert
    suspend fun upsert(entry: CachedResponseEntity)

    @Query("SELECT * FROM cached_responses WHERE `key` = :key")
    suspend fun get(key: String): CachedResponseEntity?

    @Query("DELETE FROM cached_responses")
    suspend fun clear()
}

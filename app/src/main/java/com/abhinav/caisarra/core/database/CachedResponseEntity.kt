package com.abhinav.caisarra.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_responses")
data class CachedResponseEntity(
    @PrimaryKey val key: String,
    val json: String,
    val updatedAt: Long
)

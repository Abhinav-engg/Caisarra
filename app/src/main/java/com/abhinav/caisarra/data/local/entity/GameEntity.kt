package com.abhinav.caisarra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val whiteName: String,
    val blackName: String,
    val timeControlMinutes: Int?,
    val incrementSeconds: Int,
    val moves: String,
    val moveTimes: String = "",
    val whiteTimeMs: Long,
    val blackTimeMs: Long,
    val result: String?,
    val endReason: String?,
    val startedAt: Long,
    val endedAt: Long?,
    val status: RecordStatus
)
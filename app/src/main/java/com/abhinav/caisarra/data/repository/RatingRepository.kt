package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.api.RatingService
import com.abhinav.caisarra.data.remote.dto.ClubLeaderboardEntry
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.abhinav.caisarra.data.remote.dto.PlayerRatingProfile

class RatingRepository(
    private val service: RatingService
) {

    suspend fun getPublicProfile(
        username: String
    ): PlayerRatingProfile {
        val response = service.getPublicProfile(
            username.trim()
        )

        if (!response.success) {
            error(response.error ?: response.message ?: "Unable to load player profile"
            )
        }
        return response.data ?: error("Player profile response was empty")
    }

    suspend fun getModeStats(
        username: String,
        mode: String,
        range: String? = null
    ): ModeStatsData {
        val normalizedUsername = username.trim()
        require(normalizedUsername.isNotBlank()) {
            "Username cannot be empty"
        }

        val normalizedMode = mode.trim().lowercase()

        require(
            normalizedMode in setOf("bullet", "blitz", "rapid", "daily")
        ) {
            "Mode must be one of bullet, blitz, rapid, daily"
        }

        val normalizedRange = range?.trim()?.lowercase()?.ifEmpty { null }
        if (normalizedRange != null) {
            require(normalizedRange in setOf("7d", "30d", "90d", "1y", "all")) {
                "Range must be one of 7d, 30d, 90d, 1y, all"
            }
        }

        val response = service.getModeStats(
            username = normalizedUsername,
            mode = normalizedMode,
            range = normalizedRange
        )

        if (!response.success) {
            error(response.error ?: response.message ?: "Unable to load rating statistics"
            )
        }

        return response.data ?: error("Rating statistics response was empty")
    }

    suspend fun getClubLeaderboard(
        clubId: String
    ): List<ClubLeaderboardEntry> {
        require(clubId.isNotBlank()) {
            "Club ID is required"
        }

        return try {
            service.getClubLeaderboard(clubId.trim())
                .sortedWith(compareBy<ClubLeaderboardEntry> { it.rank }
                        .thenBy { it.username.lowercase() }
                )
        } catch (e: Exception) {
            throw Exception(
                ApiErrorInterpreter.toUserMessage(e),e
            )
        }
    }
}

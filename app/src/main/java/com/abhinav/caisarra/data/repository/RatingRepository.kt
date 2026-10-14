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
        mode: String
    ): ModeStatsData {
        val normalizedMode = mode.trim().lowercase()

        require(
            normalizedMode in setOf("bullet", "blitz", "rapid", "daily")
        ) {
            "Mode must be bullet, blitz, rapid, or daily"
        }

        val response = service.getModeStats(
            username.trim(),
            normalizedMode
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

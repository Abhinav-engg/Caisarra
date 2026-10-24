
package com.abhinav.caisarra.data.remote.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LeaderboardService {

    // 1. Global daily-rating leaderboard
    @GET("api/leaderboard")
    suspend fun getGlobalLeaderboard(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): GlobalLeaderboardResponse

    // 2. Leaderboard for a particular club
    @GET("api/clubs/{clubID}/leaderboard")
    suspend fun getClubLeaderboard(
        @Path("clubID") clubId: String,
        @Query("mode") mode: String = "blitz"
    ): List<LeaderboardEntry>

    // 3. Puzzle Rush leaderboard
    // This endpoint intentionally has no /api prefix.
    @GET("puzzles/rush/leaderboard")
    suspend fun getPuzzleRushLeaderboard(
        @Query("mode") mode: String = "3min",
        @Query("limit") limit: Int = 50
    ): PuzzleRushLeaderboardResponse
}

@Serializable
data class GlobalLeaderboardResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: List<LeaderboardEntry> = emptyList()
)

@Serializable
data class LeaderboardEntry(
    val rank: Int = 0,
    val user_id: Long = 0L,
    val username: String = "",
    val rating: Int = 0
)

@Serializable
data class PuzzleRushLeaderboardResponse(
    val mode: String = "3min",
    val entries: List<PuzzleRushLeaderboardEntry> = emptyList()
)

@Serializable
data class PuzzleRushLeaderboardEntry(
    val user_id: Long = 0L,
    val username: String = "",
    val score: Int = 0,
    val mode: String = "3min",
    val played_at: String = ""
)


package com.abhinav.caisarra.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LeaderboardService {

    @GET("api/leaderboard")
    suspend fun getGlobalLeaderboard(
        @Query("mode") mode: String = "rapid",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): GlobalLeaderboardResponse

    @GET("api/clubs/{clubID}/leaderboard")
    suspend fun getClubLeaderboard(
        @Path("clubID") clubId: String,
        @Query("mode") mode: String = "rapid"
    ): List<LeaderboardEntry>

    @GET("puzzles/rush/leaderboard")
    suspend fun getPuzzleRushLeaderboard(
        @Query("mode") mode: String = "3min",
        @Query("limit") limit: Int = 50
    ): PuzzleRushLeaderboardResponse
}

@Serializable
data class GlobalLeaderboardResponse(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val data: List<LeaderboardEntry> = emptyList()
)

@Serializable
data class LeaderboardEntry(
    val rank: Int = 0,
    @SerialName("user_id")
    val userId: Long = 0L,
    val username: String = "",
    val rating: Int = 0
) {
    val user_id: Long get() = userId
}

@Serializable
data class PuzzleRushLeaderboardResponse(
    val mode: String = "3min",
    val entries: List<PuzzleRushLeaderboardEntry> = emptyList()
)

@Serializable
data class PuzzleRushLeaderboardEntry(
    @SerialName("user_id")
    val userId: Long = 0L,
    val username: String = "",
    val score: Int = 0,
    val mode: String = "3min",
    @SerialName("played_at")
    val playedAt: String = ""
) {
    val user_id: Long get() = userId
    val played_at: String get() = playedAt
}

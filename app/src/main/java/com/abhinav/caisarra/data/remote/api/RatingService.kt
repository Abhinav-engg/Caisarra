
package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.abhinav.caisarra.data.remote.dto.PlayerRatingProfile
import com.abhinav.caisarra.data.remote.dto.ClubLeaderboardEntry
import retrofit2.http.GET
import retrofit2.http.Path

interface RatingService {
    @GET("players/{username}")
    suspend fun getPublicProfile(
        @Path("username") username: String
    ): ApiResponse<PlayerRatingProfile>
    @GET("players/{username}/stats/{mode}")
    suspend fun getModeStats(
        @Path("username") username: String,
        @Path("mode") mode: String
    ): ApiResponse<ModeStatsData>
    @GET("api/clubs/{clubID}/leaderboard")
    suspend fun getClubLeaderboard(
        @Path("clubID") clubId: String
    ): List<ClubLeaderboardEntry>
}

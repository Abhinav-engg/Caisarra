
package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerRatingProfile(
    @SerialName("user_id")
    val userId: Int,

    val username: String,

    @SerialName("display_name")
    val displayName: String? = null,

    val country: String? = null,
    val bio: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    val rating: Int? = null,
    val provisional: Boolean = true,

    val ratings: Map<String, ModeRating>? = null,

    @SerialName("joined_at")
    val joinedAt: String? = null
)

@Serializable
data class ModeStatsData(
    val mode: String,
    val range: String = "30d",
    val rating: Int = 1200,
    val provisional: Boolean = true,

    @SerialName("best_rating")
    val bestRating: Int? = null,

    @SerialName("best_rating_at")
    val bestRatingAt: String? = null,

    @SerialName("lowest_rating")
    val lowestRating: Int? = null,

    @SerialName("lowest_rating_at")
    val lowestRatingAt: String? = null,

    @SerialName("start_rating")
    val startRating: Int = 1200,

    @SerialName("games_played")
    val gamesPlayed: Int = 0,

    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,

    @SerialName("win_percent")
    val winPercent: Int = 0,

    @SerialName("as_white")
    val asWhite: ColorStatsDto? = null,

    @SerialName("as_black")
    val asBlack: ColorStatsDto? = null,

    @SerialName("current_streak")
    val currentStreak: Int = 0,

    @SerialName("best_streak")
    val bestStreak: Int = 0,

    @SerialName("rating_history")
    val ratingHistory: List<RatingHistoryPoint> = emptyList(),

    @SerialName("recent_games")
    val recentGames: List<RatingRecentGame> = emptyList()
)

@Serializable
data class ColorStatsDto(
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0
)

@Serializable
data class RatingHistoryPoint(
    val date: String,
    val rating: Int
)

@Serializable
data class RatingRecentGame(
    @SerialName("game_id")
    val gameId: String,

    @SerialName("opponent_username")
    val opponentUsername: String,

    val color: String,
    val outcome: String,

    @SerialName("end_reason")
    val endReason: String? = null,

    @SerialName("ended_at")
    val endedAt: String? = null
)

@Serializable
data class ClubLeaderboardEntry(
    val rank: Int,

    @SerialName("user_id")
    val userId: Int,

    val username: String,
    val rating: Int
)

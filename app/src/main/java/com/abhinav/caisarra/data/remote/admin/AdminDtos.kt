
package com.abhinav.caisarra.data.remote.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminMe(
    @SerialName("is_admin")
    val isAdmin: Boolean = false
)

@Serializable
data class AdminStats(
    @SerialName("total_users") val totalUsers: Int = 0,
    @SerialName("new_users_today") val newUsersToday: Int = 0,
    @SerialName("banned_users") val bannedUsers: Int = 0,
    @SerialName("active_games") val activeGames: Int = 0,
    @SerialName("pending_reports") val pendingReports: Int = 0,
    @SerialName("ongoing_tournaments") val ongoingTournaments: Int = 0
)

@Serializable
data class AdminHealth(
    val database: String = "unknown",
    val redis: String = "unknown"
)

@Serializable
data class AdminMatchmakingQueue(
    val queue: String,
    val players: Long
)

@Serializable
data class AdminUser(
    val id: Long,
    val username: String,
    val email: String,
    val role: String = "user",
    val rating: Int = 0,
    @SerialName("is_banned") val isBanned: Boolean = false,
    @SerialName("ban_reason") val banReason: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class AdminUserDetails(
    val id: Long,
    val username: String,
    val email: String,
    val role: String = "user",
    val rating: Int = 0,
    @SerialName("email_verified") val emailVerified: Boolean = false,
    @SerialName("is_banned") val isBanned: Boolean = false,
    @SerialName("is_permanent_ban") val isPermanentBan: Boolean = false,
    @SerialName("banned_until") val bannedUntil: String? = null,
    @SerialName("ban_reason") val banReason: String = "",
    @SerialName("games_played") val gamesPlayed: Int = 0,
    @SerialName("reports_against") val reportsAgainst: Int = 0,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class AdminReport(
    val id: Long,
    @SerialName("reporter_id") val reporterId: Long,
    @SerialName("reporter_username") val reporterUsername: String,
    @SerialName("reported_id") val reportedId: Long,
    @SerialName("reported_username") val reportedUsername: String,
    val reason: String,
    val details: String = "",
    val status: String,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class AdminAction(
    val id: Long,
    @SerialName("admin_id") val adminId: Long? = null,
    @SerialName("admin_username") val adminUsername: String,
    val action: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    val details: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class AdminGame(
    val id: String,
    @SerialName("white_player_id") val whitePlayerId: Long,
    @SerialName("white_username") val whiteUsername: String = "",
    @SerialName("black_player_id") val blackPlayerId: Long,
    @SerialName("black_username") val blackUsername: String = "",
    @SerialName("time_control_mode") val timeControlMode: String = "",
    @SerialName("time_control_minutes") val timeControlMinutes: Int = 0,
    val rated: Boolean = false,
    @SerialName("move_count") val moveCount: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("started_at") val startedAt: String? = null
)

@Serializable
data class AdminGameMove(
    val id: String = "",
    @SerialName("game_id") val gameId: String = "",
    @SerialName("move_number") val moveNumber: Int = 0,
    @SerialName("player_id") val playerId: String = "",
    val move: String = "",
    @SerialName("position_after") val positionAfter: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class AdminGameDetails(
    val id: String,
    @SerialName("white_player_id") val whitePlayerId: Long,
    @SerialName("black_player_id") val blackPlayerId: Long,
    @SerialName("white_username") val whiteUsername: String? = null,
    @SerialName("black_username") val blackUsername: String? = null,
    @SerialName("time_control_minutes") val timeControlMinutes: Int = 0,
    val rated: Boolean = false,
    @SerialName("time_control_mode") val timeControlMode: String = "",
    val position: String = "",
    val status: String = "",
    @SerialName("initial_time_ms") val initialTimeMs: Long = 0,
    @SerialName("increment_ms") val incrementMs: Long = 0,
    @SerialName("white_time_ms") val whiteTimeMs: Long = 0,
    @SerialName("black_time_ms") val blackTimeMs: Long = 0,
    @SerialName("current_turn") val currentTurn: String? = null,
    @SerialName("turn_started_at") val turnStartedAt: String? = null,
    @SerialName("last_move") val lastMove: String? = null,
    val result: String? = null,
    @SerialName("end_reason") val endReason: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null
)

@Serializable
data class AdminGameDetailsResponse(
    val game: AdminGameDetails,
    val moves: List<AdminGameMove>? = null
)

@Serializable
data class AdminBanRequest(
    val reason: String,
    val days: Int
)

@Serializable
data class CloseReportRequest(
    val status: String,
    val note: String = ""
)

@Serializable
data class AbortGameRequest(
    val reason: String
)

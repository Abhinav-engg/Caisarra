package com.abhinav.caisarra.data.remote.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminStats(
    @SerialName("total_users")
    val totalUsers: Int = 0,

    @SerialName("new_users_today")
    val newUsersToday: Int = 0,

    @SerialName("banned_users")
    val bannedUsers: Int = 0,

    @SerialName("active_games")
    val activeGames: Int = 0,

    @SerialName("pending_reports")
    val pendingReports: Int = 0,

    @SerialName("ongoing_tournaments")
    val ongoingTournaments: Int = 0
)

@Serializable
data class AdminUser(
    val id: Long,
    val username: String,
    val email: String,
    val role: String = "user",
    val rating: Int = 0,

    @SerialName("is_banned")
    val isBanned: Boolean = false,

    @SerialName("ban_reason")
    val banReason: String = "",

    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class AdminUserDetails(
    val id: Long,
    val username: String,
    val email: String,
    val role: String = "user",
    val rating: Int = 0,

    @SerialName("email_verified")
    val emailVerified: Boolean = false,

    @SerialName("is_banned")
    val isBanned: Boolean = false,

    @SerialName("is_permanent_ban")
    val isPermanentBan: Boolean = false,

    @SerialName("banned_until")
    val bannedUntil: String? = null,

    @SerialName("ban_reason")
    val banReason: String = "",

    @SerialName("games_played")
    val gamesPlayed: Int = 0,

    @SerialName("reports_against")
    val reportsAgainst: Int = 0,

    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class AdminReport(
    val id: Long,

    @SerialName("reporter_id")
    val reporterId: Long,

    @SerialName("reporter_username")
    val reporterUsername: String,

    @SerialName("reported_id")
    val reportedId: Long,

    @SerialName("reported_username")
    val reportedUsername: String,

    val reason: String,
    val details: String = "",
    val status: String,

    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class AdminAction(
    val id: Long,

    @SerialName("admin_id")
    val adminId: Long? = null,

    @SerialName("admin_username")
    val adminUsername: String,

    val action: String,

    @SerialName("target_type")
    val targetType: String,

    @SerialName("target_id")
    val targetId: String,

    val details: String = "",

    @SerialName("created_at")
    val createdAt: String = ""
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

package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.admin.*
import com.abhinav.caisarra.data.remote.dto.ApiResponse
import retrofit2.HttpException
import retrofit2.Response

class AdminRepository(
    private val service: AdminService
) {

    private fun <T> unwrap(
        response: Response<ApiResponse<T>>
    ): T? {
        val body = response.body()

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        if (body?.success != true) {
            error(body?.error ?: body?.message ?: "Admin request failed")
        }

        return body.data
    }

    suspend fun verifyAdmin(): Boolean =
        unwrap(service.getMe())?.isAdmin == true

    suspend fun getStats(): AdminStats =
        unwrap(service.getStats()) ?: error("Stats unavailable")

    suspend fun getHealth(): AdminHealth =
        unwrap(service.getHealth()) ?: error("Health unavailable")

    suspend fun getUsers(
        query: String = "",
        limit: Int = 20,
        offset: Int = 0
    ): List<AdminUser> =
        unwrap(service.getUsers(query, limit, offset)).orEmpty()

    suspend fun getUserDetails(id: Long): AdminUserDetails =
        unwrap(service.getUserDetails(id))
            ?: error("User details unavailable")

    suspend fun banUser(id: Long, reason: String, days: Int) {
        require(reason.isNotBlank()) { "Ban reason is required" }
        require(reason.length <= 500) { "Reason must be at most 500 characters" }
        require(days in 0..365) { "Days must be between 0 and 365" }

        unwrap(service.banUser(id, AdminBanRequest(reason.trim(), days)))
    }

    suspend fun unbanUser(id: Long) {
        unwrap(service.unbanUser(id))
    }

    suspend fun forceLogout(id: Long) {
        unwrap(service.forceLogout(id))
    }

    suspend fun getReports(
        status: String = "pending",
        limit: Int = 20,
        offset: Int = 0
    ): List<AdminReport> =
        unwrap(service.getReports(status, limit, offset)).orEmpty()

    suspend fun closeReport(
        id: Long,
        status: String,
        note: String = ""
    ) {
        require(status == "resolved" || status == "dismissed")
        require(note.length <= 500)

        unwrap(service.closeReport(id, CloseReportRequest(status, note)))
    }

    suspend fun getGames(
        limit: Int = 20,
        offset: Int = 0
    ): List<AdminGame> =
        unwrap(service.getGames(limit, offset)).orEmpty()

    suspend fun getGameDetails(id: String): AdminGameDetailsResponse =
        unwrap(service.getGameDetails(id))
            ?: error("Game details unavailable")

    suspend fun abortGame(id: String, reason: String) {
        require(reason.isNotBlank()) { "Abort reason is required" }
        require(reason.length <= 500)

        unwrap(service.abortGame(id, AbortGameRequest(reason.trim())))
    }

    suspend fun getMatchmaking(): List<AdminMatchmakingQueue> =
        unwrap(service.getMatchmaking()).orEmpty()
            .groupBy { it.queue }
            .map { (queue, entries) ->
                AdminMatchmakingQueue(
                    queue = queue,
                    players = entries.sumOf { it.players }
                )
            }

    suspend fun deletePost(id: Long) {
        unwrap(service.deletePost(id))
    }

    suspend fun deleteComment(id: Long) {
        unwrap(service.deleteComment(id))
    }

    suspend fun deleteClub(id: String) {
        require(id.isNotBlank()) { "Club ID is required" }
        unwrap(service.deleteClub(id.trim()))
    }

    suspend fun cancelTournament(id: Long) {
        unwrap(service.cancelTournament(id))
    }

    suspend fun getActions(
        limit: Int = 20,
        offset: Int = 0
    ): List<AdminAction> =
        unwrap(service.getActions(limit, offset)).orEmpty()
}

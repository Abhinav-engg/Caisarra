package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.admin.*

class AdminRepository(
    private val service: AdminService
) {

    private fun <T> unwrap(
        response: retrofit2.Response<com.abhinav.caisarra.data.remote.dto.ApiResponse<T>>
    ): T? {
        val body = response.body()

        if (!response.isSuccessful) {
            throw retrofit2.HttpException(response)
        }

        if (body?.success != true) {
            error(body?.error ?: body?.message ?: "Admin request failed")
        }

        return body.data
    }

    suspend fun getStats(): AdminStats =
        unwrap(service.getStats()) ?: error("Stats unavailable")

    suspend fun getUsers(
        query: String = "",
        limit: Int = 20,
        offset: Int = 0
    ): List<AdminUser> =
        unwrap(service.getUsers(query, limit, offset)).orEmpty()

    suspend fun getUserDetails(id: Long): AdminUserDetails =
        unwrap(service.getUserDetails(id))
            ?: error("User details unavailable")

    suspend fun banUser(
        id: Long,
        reason: String,
        days: Int
    ) {
        require(reason.isNotBlank()) { "Ban reason is required" }
        require(reason.length <= 500) { "Reason must be at most 500 characters" }
        require(days in 0..365) { "Days must be between 0 and 365" }

        unwrap(
            service.banUser(
                id,
                AdminBanRequest(reason.trim(), days)
            )
        )
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

        unwrap(
            service.closeReport(
                id,
                CloseReportRequest(status, note)
            )
        )
    }

    suspend fun deletePost(id: Long) {
        unwrap(service.deletePost(id))
    }

    suspend fun deleteComment(id: Long) {
        unwrap(service.deleteComment(id))
    }

    suspend fun deleteClub(id: String) {
        require(id.isNotBlank())
        unwrap(service.deleteClub(id))
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
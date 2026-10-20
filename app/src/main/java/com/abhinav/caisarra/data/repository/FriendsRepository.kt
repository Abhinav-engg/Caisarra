
package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.api.FriendsService
import com.abhinav.caisarra.data.remote.dto.FriendDto
import com.abhinav.caisarra.data.remote.dto.FriendRequestDto
import com.abhinav.caisarra.data.remote.dto.FriendshipStatusData
import com.abhinav.caisarra.data.remote.dto.PlayerSearchDto
import com.abhinav.caisarra.data.remote.dto.SendFriendRequest

class FriendsRepository(context: Context) {

    private val service: FriendsService =
        RetrofitInstance.createFriendsService(
            TokenManager(context.applicationContext)
        )

    suspend fun searchPlayers(
        query: String
    ): Result<List<PlayerSearchDto>> = execute {
        val response = service.searchPlayers(query.trim())
        checkSuccess(response.success, response.error, response.message)
        response.data?.players.orEmpty()
    }

    suspend fun getFriendshipStatus(
        username: String
    ): Result<FriendshipStatusData> = execute {
        val response = service.getFriendshipStatus(username)
        checkSuccess(response.success, response.error, response.message)
        response.data ?: error("Friendship status was empty.")
    }

    suspend fun getFriends(): Result<List<FriendDto>> = execute {
        val response = service.getFriends()
        checkSuccess(response.success, response.error, response.message)
        response.data?.friends.orEmpty()
    }

    suspend fun getIncomingRequests(): Result<List<FriendRequestDto>> =
        execute {
            val response = service.getIncomingRequests()
            checkSuccess(response.success, response.error, response.message)
            response.data?.requests.orEmpty()
        }

    suspend fun getSentRequests(): Result<List<FriendRequestDto>> =
        execute {
            val response = service.getSentRequests()
            checkSuccess(response.success, response.error, response.message)
            response.data?.requests.orEmpty()
        }

    suspend fun sendFriendRequest(
        username: String
    ): Result<Unit> = execute {
        val response = service.sendFriendRequest(
            SendFriendRequest(username = username)
        )
        checkSuccess(response.success, response.error, response.message)
    }

    suspend fun acceptRequest(requestId: Long): Result<Unit> =
        execute {
            val response = service.acceptRequest(requestId)
            checkSuccess(response.success, response.error, response.message)
        }

    suspend fun rejectRequest(requestId: Long): Result<Unit> =
        execute {
            val response = service.rejectRequest(requestId)
            checkSuccess(response.success, response.error, response.message)
        }

    suspend fun cancelRequest(requestId: Long): Result<Unit> =
        execute {
            val response = service.cancelRequest(requestId)
            checkSuccess(response.success, response.error, response.message)
        }

    suspend fun removeFriend(userId: Long): Result<Unit> =
        execute {
            val response = service.removeFriend(userId)
            checkSuccess(response.success, response.error, response.message)
        }

    private fun checkSuccess(
        success: Boolean,
        error: String?,
        message: String?
    ) {
        if (!success) {
            throw IllegalStateException(
                error ?: message ?: "The request could not be completed."
            )
        }
    }

    private suspend fun <T> execute(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.message?.takeIf { it.isNotBlank() }
                        ?: ApiErrorInterpreter.toUserMessage(e)
                )
            )
        }
    }
}

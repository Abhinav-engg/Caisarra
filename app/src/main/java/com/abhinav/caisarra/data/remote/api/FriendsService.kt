
package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.FriendRequestsData
import com.abhinav.caisarra.data.remote.dto.FriendshipStatusData
import com.abhinav.caisarra.data.remote.dto.FriendsListData
import com.abhinav.caisarra.data.remote.dto.PlayerSearchData
import com.abhinav.caisarra.data.remote.dto.SendFriendRequest
import com.abhinav.caisarra.data.remote.dto.SendFriendRequestData
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendsService {

    @GET("api/players/search")
    suspend fun searchPlayers(
        @Query("q") query: String
    ): ApiResponse<PlayerSearchData>

    @GET("api/friends/status/{username}")
    suspend fun getFriendshipStatus(
        @Path("username") username: String
    ): ApiResponse<FriendshipStatusData>

    @GET("api/friends")
    suspend fun getFriends(): ApiResponse<FriendsListData>

    @GET("api/friends/requests")
    suspend fun getIncomingRequests(): ApiResponse<FriendRequestsData>

    @GET("api/friends/requests/sent")
    suspend fun getSentRequests(): ApiResponse<FriendRequestsData>

    @POST("api/friends/requests")
    suspend fun sendFriendRequest(
        @Body request: SendFriendRequest
    ): ApiResponse<SendFriendRequestData>

    @POST("api/friends/requests/{id}/accept")
    suspend fun acceptRequest(
        @Path("id") requestId: Long
    ): ApiResponse<Unit>

    @POST("api/friends/requests/{id}/reject")
    suspend fun rejectRequest(
        @Path("id") requestId: Long
    ): ApiResponse<Unit>

    @DELETE("api/friends/requests/{id}")
    suspend fun cancelRequest(
        @Path("id") requestId: Long
    ): ApiResponse<Unit>

    @DELETE("api/friends/{userId}")
    suspend fun removeFriend(
        @Path("userId") userId: Long
    ): ApiResponse<Unit>
}

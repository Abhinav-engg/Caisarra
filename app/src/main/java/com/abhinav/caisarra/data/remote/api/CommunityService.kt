
package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.CommunityCommentDto
import com.abhinav.caisarra.data.remote.dto.CommunityCommentsData
import com.abhinav.caisarra.data.remote.dto.CommunityFeedData
import com.abhinav.caisarra.data.remote.dto.CommunityPostDto
import com.abhinav.caisarra.data.remote.dto.CommunityReactionData
import com.abhinav.caisarra.data.remote.dto.CreateCommunityCommentRequest
import com.abhinav.caisarra.data.remote.dto.CreateCommunityPostRequest
import com.abhinav.caisarra.data.remote.dto.CommunityReactionRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityService {

    @GET("posts")
    suspend fun getFeed(
        @Query("before_id") beforeId: Long? = null,
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityFeedData>

    @GET("posts/{id}")
    suspend fun getPost(
        @Path("id") postId: Long
    ): ApiResponse<CommunityPostDto>

    @GET("posts/{id}/comments")
    suspend fun getComments(
        @Path("id") postId: Long,
        @Query("after_id") afterId: Long? = null,
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityCommentsData>

    @POST("api/posts")
    suspend fun createPost(
        @Body request: CreateCommunityPostRequest
    ): ApiResponse<CommunityPostDto>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(
        @Path("id") postId: Long
    ): ApiResponse<Unit>

    @PUT("api/posts/{id}/reaction")
    suspend fun setReaction(
        @Path("id") postId: Long,
        @Body request: CommunityReactionRequest
    ): ApiResponse<CommunityReactionData>

    @DELETE("api/posts/{id}/reaction")
    suspend fun removeReaction(
        @Path("id") postId: Long
    ): ApiResponse<CommunityReactionData>

    @POST("api/posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: Long,
        @Body request: CreateCommunityCommentRequest
    ): ApiResponse<CommunityCommentDto>

    @DELETE("api/comments/{id}")
    suspend fun deleteComment(
        @Path("id") commentId: Long
    ): ApiResponse<Unit>
}

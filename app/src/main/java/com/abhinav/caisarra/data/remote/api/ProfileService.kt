
package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.remote.dto.UpdateProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE

interface ProfileService {

    @GET("api/profile")
    suspend fun getMyProfile(): ApiResponse<ProfileData>

    @PATCH("api/profile")
    suspend fun updateMyProfile(
        @Body request: UpdateProfileRequest
    ): ApiResponse<ProfileData>

    @GET("avatars")
    suspend fun getDefaultAvatars(): ApiResponse<DefaultAvatarsData>

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(@Part photo: MultipartBody.Part): ApiResponse<AvatarUrlData>

    @PUT("api/profile/avatar/default")
    suspend fun setDefaultAvatar(@Body request: DefaultAvatarRequest): ApiResponse<AvatarUrlData>

    @DELETE("api/profile/avatar")
    suspend fun removeAvatar(): ApiResponse<Unit>

    @GET("players/{username}")
    suspend fun getPublicProfile(
        @Path("username") username: String
    ): ApiResponse<ProfileData>
}


@kotlinx.serialization.Serializable
data class DefaultAvatarsData(
    val avatars: List<DefaultAvatarItem> = emptyList()
)

@kotlinx.serialization.Serializable
data class DefaultAvatarItem(val id: String, val url: String)

@kotlinx.serialization.Serializable
data class AvatarUrlData(
    @kotlinx.serialization.SerialName("avatar_url") val avatarUrl: String? = null
)

@kotlinx.serialization.Serializable
data class DefaultAvatarRequest(
    @kotlinx.serialization.SerialName("avatar_id") val avatarId: String
)

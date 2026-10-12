
package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.remote.dto.UpdateProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ProfileService {

    @GET("api/profile")
    suspend fun getMyProfile(): ApiResponse<ProfileData>

    @PATCH("api/profile")
    suspend fun updateMyProfile(
        @Body request: UpdateProfileRequest
    ): ApiResponse<ProfileData>

    @GET("players/{username}")
    suspend fun getPublicProfile(
        @Path("username") username: String
    ): ApiResponse<ProfileData>
}

package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.CreateInviteRequest
import com.abhinav.caisarra.data.remote.dto.CreateInviteResponse
import com.abhinav.caisarra.data.remote.dto.InvitePreviewResponse
import com.abhinav.caisarra.data.remote.dto.JoinGameResponse

class InviteRepository private constructor(context: Context) {

    private val service = RetrofitInstance.createInviteService(TokenManager(context))

    suspend fun createInvite(timeControlMinutes: Int, color: String): Result<CreateInviteResponse> =
        call { service.createInvite(CreateInviteRequest(timeControlMinutes, color)) }

    suspend fun previewInvite(code: String): Result<InvitePreviewResponse> =
        call { service.previewInvite(code) }

    suspend fun joinInvite(code: String): Result<JoinGameResponse> =
        call { service.joinInvite(code) }

    private suspend fun <T> call(block: suspend () -> ApiResponse<T>): Result<T> {
        return try {
            val data = block().data
                ?: return Result.failure(Exception("Something went wrong. Please try again."))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorInterpreter.toUserMessage(e)))
        }
    }

    companion object {
        @Volatile
        private var instance: InviteRepository? = null

        fun get(context: Context): InviteRepository =
            instance ?: synchronized(this) {
                instance ?: InviteRepository(context.applicationContext).also { instance = it }
            }
    }
}
package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.AcceptChallengeResponse
import com.abhinav.caisarra.data.remote.dto.ChallengeDto
import com.abhinav.caisarra.data.remote.dto.CreateInviteRequest
import com.abhinav.caisarra.data.remote.dto.CreateInviteResponse
import com.abhinav.caisarra.data.remote.dto.InvitePreviewResponse
import com.abhinav.caisarra.data.remote.dto.JoinGameResponse
import com.abhinav.caisarra.data.remote.dto.MessageResponse
import retrofit2.HttpException

class InviteRepository private constructor(context: Context) {

    private val service = RetrofitInstance.createInviteService(TokenManager.get(context))

    suspend fun createInvite(
        timeControlMinutes: Int,
        incrementSeconds: Int,
        color: String,
        rated: Boolean,
        opponentId: Long? = null
    ): Result<CreateInviteResponse> =
        call {
            val req = CreateInviteRequest(
                timeControlMinutes = timeControlMinutes,
                incrementSeconds = incrementSeconds,
                color = color,
                opponentId = if (opponentId != null && opponentId > 0) opponentId else null,
                rated = rated
            )
            try {
                service.createInvite(req)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    service.createInviteLegacy(req)
                } else throw e
            }
        }

    suspend fun getReceivedChallenges(): Result<List<ChallengeDto>> =
        call { service.getReceivedChallenges() }

    suspend fun getSentChallenges(): Result<List<ChallengeDto>> =
        call { service.getSentChallenges() }

    suspend fun acceptChallenge(code: String): Result<AcceptChallengeResponse> =
        call {
            try {
                service.acceptChallenge(code)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    try {
                        service.joinInvite(code)
                    } catch (e2: HttpException) {
                        if (e2.code() == 404) {
                            val res = service.joinInviteLegacy(code)
                            AcceptChallengeResponse(gameId = res.gameId, status = res.status)
                        } else throw e2
                    }
                } else throw e
            }
        }

    suspend fun declineChallenge(code: String): Result<MessageResponse> =
        call { service.declineChallenge(code) }

    suspend fun cancelChallenge(code: String): Result<MessageResponse> =
        call { service.cancelChallenge(code) }

    suspend fun previewInvite(code: String): Result<InvitePreviewResponse> =
        call {
            try {
                service.previewInvite(code)
            } catch (e1: HttpException) {
                if (e1.code() == 404) {
                    try {
                        service.previewInviteV1Api(code)
                    } catch (e2: HttpException) {
                        if (e2.code() == 404) {
                            service.previewInviteLegacy(code)
                        } else throw e2
                    }
                } else throw e1
            }
        }

    suspend fun joinInvite(code: String): Result<JoinGameResponse> =
        call {
            try {
                val acc = service.acceptChallenge(code)
                JoinGameResponse(gameId = acc.gameId, status = acc.status ?: "joined")
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    service.joinInviteLegacy(code)
                } else throw e
            }
        }

    private suspend fun <T> call(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
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

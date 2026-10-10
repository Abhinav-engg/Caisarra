package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance

class DrawRepository private constructor(context: Context) {

    private val service = RetrofitInstance.createDrawService(TokenManager(context))

    suspend fun offerDraw(gameId: String): Result<Unit> = call { service.offerDraw(gameId) }

    suspend fun acceptDraw(gameId: String): Result<Unit> = call { service.acceptDraw(gameId) }

    suspend fun declineDraw(gameId: String): Result<Unit> = call { service.declineDraw(gameId) }

    private suspend fun call(block: suspend () -> Unit): Result<Unit> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorInterpreter.toUserMessage(e)))
        }
    }

    companion object {
        @Volatile
        private var instance: DrawRepository? = null

        fun get(context: Context): DrawRepository =
            instance ?: synchronized(this) {
                instance ?: DrawRepository(context.applicationContext).also { instance = it }
            }
    }
}
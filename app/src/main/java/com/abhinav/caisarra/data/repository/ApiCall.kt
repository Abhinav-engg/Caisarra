package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.dto.ApiResponse
import kotlinx.coroutines.CancellationException

suspend fun <T> apiCall(block: suspend () -> ApiResponse<T>): Result<T> = try {
    val response = block()
    response.data?.let { Result.success(it) } ?: Result.failure(Exception(response.error ?: response.message))
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(Exception(ApiErrorInterpreter.toUserMessage(e), e))
}

package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.remote.dto.ApiError
import retrofit2.HttpException
import java.io.IOException

object ApiErrorInterpreter {

    fun toUserMessage(error: Throwable, fallback: String? = null): String {

        if (error is java.net.SocketTimeoutException) {
            return "Server timed out. The server is taking too long to respond."
        }
        if (error is java.net.ConnectException) {
            return "Could not connect to the server. The server might be down."
        }
        if (error is java.net.UnknownHostException) {
            return "Unable to resolve server address. Check your internet connection."
        }
        if (error is IOException) {
            return "Can't reach the server. Check your connection or server status."
        }
        if (error is HttpException) {

            val body = runCatching { error.response()?.errorBody()?.string() }.getOrNull()
            if (body != null) {
                try {
                    val apiError = RetrofitInstance.json.decodeFromString(ApiError.serializer(), body)
                    return apiError.error
                } catch (e: Exception) {
                }
            }
            val code = error.code()
            if (code == 429) return "Too many attempts. Please try again in a bit."
            if (code >= 500) return "Server error. Please try again in a moment."
            if (fallback != null) return fallback
            if (code == 400) return "That request wasn't valid. Please check your input."
            if (code == 401) return "Incorrect username or password."
            if (code == 409) return "That username or email is already taken."
            return "Something went wrong. Please try again."
        }
        return "Something went wrong. Please try again."
    }
}
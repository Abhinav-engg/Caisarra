package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.remote.dto.ApiError
import retrofit2.HttpException
import java.io.IOException


object ApiErrorInterpreter {

    fun toUserMessage(error: Throwable): String {

        if (error is IOException) {
            return "Can't reach the server. Check your internet connection."
        }
        if (error is HttpException) {

            val body = error.response()?.errorBody()?.string()
            if (body != null) {
                try {
                    val apiError = RetrofitInstance.json.decodeFromString(ApiError.serializer(), body)
                    return apiError.error
                } catch (e: Exception) {
                }
            }


            val code = error.code()
            if (code == 400) return "That request wasn't valid. Please check your input."
            if (code == 401) return "Incorrect username or password."
            if (code == 409) return "That username or email is already taken."
            if (code == 429) return "Too many attempts. Please try again in a bit."
            if (code == 500) return "Server error. Please try again in a moment."
            return "Something went wrong (code $code)."
        }


        return "Something went wrong. Please try again."
    }
}
package com.abhinav.caisarra.data.repository

import android.content.Context
import android.util.Log
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.api.NotificationService
import com.abhinav.caisarra.data.remote.dto.DeleteDeviceRequest
import com.abhinav.caisarra.data.remote.dto.NotificationItemDto
import com.abhinav.caisarra.data.remote.dto.RegisterDeviceRequest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepository private constructor(context: Context) {

    private val tokenManager = TokenManager.get(context.applicationContext)
    private val service: NotificationService =
        RetrofitInstance.createNotificationService(tokenManager)

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _incomingNotifications = MutableSharedFlow<NotificationItemDto>(extraBufferCapacity = 64)
    val incomingNotifications: SharedFlow<NotificationItemDto> = _incomingNotifications.asSharedFlow()

    suspend fun registerDevice(token: String): Result<Unit> = execute {
        val candidates: List<Pair<String, JsonObject>> = listOf(
            "token + platform" to buildJsonObject {
                put("token", token)
                put("platform", "android")
            },
            "fcm_token + platform" to buildJsonObject {
                put("fcm_token", token)
                put("platform", "android")
            },
            "device_token + platform" to buildJsonObject {
                put("device_token", token)
                put("platform", "android")
            },
            "token only" to buildJsonObject {
                put("token", token)
            },
            "fcm_token only" to buildJsonObject {
                put("fcm_token", token)
            },
            "device_token only" to buildJsonObject {
                put("device_token", token)
            },
            "token + ANDROID uppercase" to buildJsonObject {
                put("token", token)
                put("platform", "ANDROID")
            },
            "all combined" to buildJsonObject {
                put("token", token)
                put("fcm_token", token)
                put("device_token", token)
                put("platform", "android")
            }
        )

        var lastException: Exception? = null
        for ((name, body) in candidates) {
            try {
                Log.d(TAG, "Attempting registerDevice with payload shape: $name")
                val response = service.registerDeviceRaw(body)
                checkSuccess(response.success, response.error, response.message)
                Log.i(TAG, "Device registered successfully with shape '$name': ${response.message}")
                android.util.Log.i("FCM_DEBUG", "Device registered successfully with shape '$name': ${response.message}")
                return@execute
            } catch (e: Exception) {
                lastException = e
                val errorBody = if (e is HttpException) {
                    try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                } else null
                Log.w(TAG, "registerDevice shape '$name' failed (${e.message}), server body: $errorBody")
                android.util.Log.w("FCM_DEBUG", "registerDevice shape '$name' failed (${e.message}), server body: $errorBody")
                if (e is HttpException && e.code() != 400) {
                    break
                }
            }
        }
        throw lastException ?: IllegalStateException("Failed to register device")
    }

    suspend fun unregisterDevice(token: String): Result<Unit> = execute {
        val candidates = listOf(
            buildJsonObject { put("token", token) },
            buildJsonObject { put("fcm_token", token) },
            buildJsonObject { put("device_token", token) }
        )
        for (body in candidates) {
            try {
                val response = service.deleteDeviceRaw(body)
                checkSuccess(response.success, response.error, response.message)
                Log.i(TAG, "Device unregistered successfully: ${response.message}")
                return@execute
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 404) return@execute
            }
        }
    }

    suspend fun fetchUnreadCount(): Result<Int> = execute {
        val response = service.getUnreadCount()
        checkSuccess(response.success, response.error, response.message)
        val count = response.data?.count ?: 0
        _unreadCount.value = count
        count
    }

    suspend fun getNotifications(
        limit: Int = 30,
        beforeId: Long? = null
    ): Result<List<NotificationItemDto>> = execute {
        val response = service.getNotifications(limit = limit, beforeId = beforeId)
        checkSuccess(response.success, response.error, response.message)
        response.data?.notifications.orEmpty()
    }

    suspend fun markAsRead(id: Long): Result<Unit> = execute {
        if (id <= 0) return@execute
        val response = service.markAsRead(id)
        checkSuccess(response.success, response.error, response.message)
        _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
    }

    suspend fun markAllAsRead(): Result<Unit> = execute {
        val response = service.markAllAsRead()
        checkSuccess(response.success, response.error, response.message)
        _unreadCount.value = 0
    }

    fun onNotificationReceived(notification: NotificationItemDto) {
        _incomingNotifications.tryEmit(notification)
        if (!notification.isRead) {
            _unreadCount.value += 1
        }
    }

    fun clearLocalState() {
        _unreadCount.value = 0
    }

    private fun checkSuccess(
        success: Boolean,
        error: String?,
        message: String?
    ) {
        if (!error.isNullOrBlank()) {
            throw IllegalStateException(error)
        }
        if (!success && message?.contains("fail", ignoreCase = true) == true) {
            throw IllegalStateException(message)
        }
    }

    private suspend fun <T> execute(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Log.e(TAG, "Notification operation failed", e)
            Result.failure(
                Exception(
                    e.message?.takeIf { it.isNotBlank() }
                        ?: ApiErrorInterpreter.toUserMessage(e)
                )
            )
        }
    }

    companion object {
        private const val TAG = "NotificationRepository"

        @Volatile
        private var instance: NotificationRepository? = null

        fun get(context: Context): NotificationRepository =
            instance ?: synchronized(this) {
                instance ?: NotificationRepository(context.applicationContext).also {
                    instance = it
                }
            }
    }
}

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
        Log.d(TAG, "Registering FCM device token: $token")
        val response = service.registerDevice(
            RegisterDeviceRequest(token = token, platform = "android")
        )
        checkSuccess(response.success, response.error, response.message)
        Log.i(TAG, "Device registered successfully for push")
    }

    suspend fun unregisterDevice(token: String): Result<Unit> = execute {
        Log.d(TAG, "Unregistering FCM device token: $token")
        val response = service.deleteDevice(
            DeleteDeviceRequest(token = token)
        )
        checkSuccess(response.success, response.error, response.message)
        Log.i(TAG, "Device unregistered successfully from push")
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
        if (!success) {
            throw IllegalStateException(
                error ?: message ?: "The request could not be completed."
            )
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

package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.DeleteDeviceRequest
import com.abhinav.caisarra.data.remote.dto.NotificationsData
import com.abhinav.caisarra.data.remote.dto.RegisterDeviceRequest
import com.abhinav.caisarra.data.remote.dto.SimpleApiResponse
import com.abhinav.caisarra.data.remote.dto.UnreadCountData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationService {

    @POST("api/notifications/devices")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest): SimpleApiResponse

    @HTTP(method = "DELETE", path = "api/notifications/devices", hasBody = true)
    suspend fun deleteDevice(@Body body: DeleteDeviceRequest): SimpleApiResponse

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int = 30,
        @Query("before_id") beforeId: Long? = null
    ): ApiResponse<NotificationsData>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): ApiResponse<UnreadCountData>

    @POST("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Long): SimpleApiResponse

    @POST("api/notifications/read-all")
    suspend fun markAllAsRead(): SimpleApiResponse
}

package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing the payload required to permanently
 * delete an authenticated user's account.
 *
 * Requirements:
 * - [password]: The user's current password, typed again to confirm deletion.
 *   Must be non-empty and up to 72 characters maximum.
 */
@Serializable
data class DeleteAccountRequest(
    @SerialName("password")
    val password: String
)

/**
 * Data transfer object representing the standard response from the backend
 * after an account deletion request.
 *
 * Status 200 OK:
 * {
 *   "success": true,
 *   "message": "account deleted"
 * }
 *
 * Status 4xx / 5xx:
 * {
 *   "success": false,
 *   "error": "password is incorrect"
 * }
 */
@Serializable
data class DeleteAccountResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("error")
    val error: String? = null
)

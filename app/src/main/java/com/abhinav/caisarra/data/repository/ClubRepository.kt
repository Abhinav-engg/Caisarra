
package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.api.ClubService
import com.abhinav.caisarra.data.remote.dto.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ResponseBody
import retrofit2.Response

class ClubRepository(
    private val service: ClubService
) {
    private val clubJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun <T> requireData(response: ApiResponse<T>): T {
        check(response.success) {
            response.error ?: response.message ?: "Club request failed."
        }
        return response.data
            ?: error(response.message ?: "Empty server response.")
    }

    suspend fun getClubs(
        search: String = "",
        offset: Int = 0
    ): List<ClubDto> = service.getClubs(
        limit = 20,
        offset = offset,
        search = search.trim().ifBlank { null }
    )

    suspend fun getMyClubs(): List<ClubDto> = service.getMyClubs()

    suspend fun createClub(name: String, description: String): ClubDto {
        require(name.isNotBlank()) { "Enter a club name." }
        require(name.length <= 60) {
            "Club name must be 60 characters or fewer."
        }

        val response = service.createClub(
            CreateClubRequest(
                name = name.trim(),
                description = description.trim()
            )
        )
        return decodeCreatedClub(response)
    }

    private fun decodeCreatedClub(response: JsonElement): ClubDto {
        var current = response

        repeat(5) {
            val obj = current as? JsonObject
                ?: throw IllegalStateException(
                    "Unexpected response while creating club."
                )

            val success = (obj["success"] as? JsonPrimitive)?.booleanOrNull
            if (success == false) {
                throw IllegalStateException(
                    obj["error"]?.jsonPrimitive?.contentOrNull
                        ?: obj["message"]?.jsonPrimitive?.contentOrNull
                        ?: "The server rejected the club creation request."
                )
            }

            val nested = obj["data"] ?: obj["club"] ?: obj["result"]
            if (nested == null) {
                return clubJson.decodeFromJsonElement(
                    ClubDto.serializer(),
                    obj
                )
            }

            current = nested
        }

        val finalObject = current as? JsonObject
            ?: throw IllegalStateException(
                "Unexpected response while creating club."
            )

        return clubJson.decodeFromJsonElement(
            ClubDto.serializer(),
            finalObject
        )
    }

    suspend fun getClub(id: String): ClubDto = service.getClub(id)

    suspend fun joinClub(clubId: String): ClubDto {
        require(clubId.isNotBlank()) { "Invalid club ID." }
        return decodeJoinedClub(service.joinClub(clubId))
    }

    suspend fun joinByCode(code: String): ClubDto {
        require(code.isNotBlank()) { "Enter a club share code." }
        return decodeJoinedClub(service.joinClubByCode(code.trim()))
    }

    /** Accept direct club JSON and common success/data or club/result envelopes. */
    private fun decodeJoinedClub(response: JsonElement): ClubDto {
        var current = response
        repeat(6) {
            val obj = current as? JsonObject
                ?: throw IllegalStateException(
                    "Unexpected response format while joining club."
                )

            val success = (obj["success"] as? JsonPrimitive)?.booleanOrNull
            if (success == false) {
                throw IllegalStateException(
                    obj["error"]?.jsonPrimitive?.contentOrNull
                        ?: obj["message"]?.jsonPrimitive?.contentOrNull
                        ?: "The server rejected the join request."
                )
            }

            val nested = obj["data"] ?: obj["club"] ?: obj["result"]
            if (nested != null && nested != current) {
                current = nested
            } else {
                return try {
                    clubJson.decodeFromJsonElement(ClubDto.serializer(), obj)
                } catch (e: Exception) {
                    throw IllegalStateException(
                        obj["message"]?.jsonPrimitive?.contentOrNull
                            ?: "The server returned an unexpected club format.",
                        e
                    )
                }
            }
        }
        throw IllegalStateException("Club response nesting is too deep.")
    }

    private fun checkActionResponse(
        response: Response<ResponseBody>,
        action: String
    ) {
        val bodyText = runCatching {
            response.errorBody()?.string()
        }.getOrNull() ?: runCatching {
            response.body()?.string()
        }.getOrNull()

        if (!response.isSuccessful) {
            val serverMessage = bodyText?.let { raw ->
                runCatching {
                    val obj = clubJson.parseToJsonElement(raw) as? JsonObject
                    obj?.get("error")?.jsonPrimitive?.contentOrNull
                        ?: obj?.get("message")?.jsonPrimitive?.contentOrNull
                }.getOrNull()
            }

            throw IllegalStateException(
                serverMessage ?: "$action failed (HTTP ${response.code()})."
            )
        }

        if (!bodyText.isNullOrBlank()) {
            val serverFailure = runCatching {
                val obj = clubJson.parseToJsonElement(bodyText) as? JsonObject
                val success = (obj?.get("success") as? JsonPrimitive)
                    ?.booleanOrNull

                if (success == false) {
                    obj["error"]?.jsonPrimitive?.contentOrNull
                        ?: obj["message"]?.jsonPrimitive?.contentOrNull
                        ?: "$action failed."
                } else {
                    null
                }
            }.getOrNull()

            if (serverFailure != null) {
                throw IllegalStateException(serverFailure)
            }
        }
    }

    suspend fun getMembers(id: String): List<ClubMemberDto> {
        val response = service.getMembers(id)

        fun decodeMembers(element: JsonElement): List<ClubMemberDto> =
            clubJson.decodeFromJsonElement(
                ListSerializer(ClubMemberDto.serializer()),
                element
            )

        if (response is JsonArray) return decodeMembers(response)

        if (response is JsonObject) {
            val success = (response["success"] as? JsonPrimitive)
                ?.booleanOrNull

            if (success == false) {
                throw IllegalStateException(
                    response["error"]?.jsonPrimitive?.contentOrNull
                        ?: response["message"]?.jsonPrimitive?.contentOrNull
                        ?: "Unable to load club members."
                )
            }

            val payload = response["data"] ?: response
            if (payload is JsonArray) return decodeMembers(payload)

            if (payload is JsonObject) {
                val members = payload["members"] as? JsonArray
                if (members != null) return decodeMembers(members)
            }
        }

        throw IllegalStateException(
            "Unexpected response format from club members endpoint."
        )
    }

    suspend fun getLeaderboard(
        id: String,
        mode: String? = null
    ): List<ClubLeaderboardPlayerDto> {
        val response = service.getLeaderboard(id, mode)
        return decodeList(
            response,
            "leaderboard",
            ClubLeaderboardPlayerDto.serializer()
        )
    }
    private fun <T> decodeList(
        element: JsonElement,
        key: String,
        serializer: kotlinx.serialization.KSerializer<T>
    ): List<T> {
        var current: JsonElement = element

        repeat(6) {
            when (current) {
                is JsonArray -> return clubJson.decodeFromJsonElement(
                    ListSerializer(serializer), current
                )

                is JsonObject -> {
                    val success = (current["success"] as? JsonPrimitive)?.booleanOrNull
                    if (success == false) {
                        throw IllegalStateException(
                            current["error"]?.jsonPrimitive?.contentOrNull
                                ?: current["message"]?.jsonPrimitive?.contentOrNull
                                ?: "Unable to load $key."
                        )
                    }

                    val next = current[key]
                        ?: current["data"]
                        ?: current["entries"]
                        ?: current["results"]
                        ?: current["items"]

                    if (next == null || next == current) {
                        throw IllegalStateException(
                            "Unexpected response format for $key."
                        )
                    }
                    current = next
                }

                else -> throw IllegalStateException(
                    "Unexpected response format for $key."
                )
            }
        }

        throw IllegalStateException("Response nesting is too deep for $key.")
    }


    suspend fun getMessages(id: String): List<ClubMessageDto> {
        return service.getMessages(id)
    }
    suspend fun sendMessage(
        id: String,
        message: String
    ): ClubMessageDto {
        require(message.isNotBlank()) { "Message cannot be empty." }

        val response = service.sendMessage(
            id,
            SendClubMessageRequest(message.trim())
        )

        // The current backend returns the message object directly:
        // {"id":"UUID", "user_id":1, "username":"...", "message":"..."}.
        // Also tolerate an optional {"success":true,"data":{...}} envelope.
        var payload = response
        if (response is JsonObject) {
            val success = (response["success"] as? JsonPrimitive)?.booleanOrNull
            if (success == false) {
                throw IllegalStateException(
                    response["error"]?.jsonPrimitive?.contentOrNull
                        ?: response["message"]?.jsonPrimitive?.contentOrNull
                        ?: "The server rejected your message."
                )
            }
            payload = response["data"] ?: response["message_data"] ?: response
        }

        val messageObject = payload as? JsonObject
            ?: throw IllegalStateException("Unexpected response from the chat server.")

        return clubJson.decodeFromJsonElement(
            ClubMessageDto.serializer(),
            messageObject
        )
    }

    suspend fun leaveClub(id: String) {
        checkActionResponse(
            service.leaveClub(id),
            "Unable to leave club"
        )
    }

    suspend fun deleteClub(id: String) {
        checkActionResponse(
            service.deleteClub(id),
            "Unable to delete club"
        )
    }

    suspend fun getBans(id: String): List<ClubMemberDto> =
        requireData(service.getBans(id)).members

    suspend fun kickMember(clubId: String, userId: Long) {
        val response = service.kickMember(clubId, userId)
        check(response.success) {
            response.error ?: response.message ?: "Unable to remove member."
        }
    }

    suspend fun promoteAdmin(clubId: String, userId: Long) {
        val response = service.promoteAdmin(clubId, userId)
        check(response.success) {
            response.error ?: response.message ?: "Unable to promote admin."
        }
    }

    suspend fun removeAdmin(clubId: String, userId: Long) {
        val response = service.removeAdmin(clubId, userId)
        check(response.success) {
            response.error ?: response.message ?: "Unable to remove admin."
        }
    }

    suspend fun transferLeadership(clubId: String, userId: Long) {
        val response = service.transferLeadership(clubId, userId)
        check(response.success) {
            response.error ?: response.message ?: "Unable to transfer leadership."
        }
    }

    suspend fun banMember(clubId: String, userId: Long) {
        val response = service.banMember(clubId, userId)
        check(response.success) {
            response.error ?: response.message ?: "Unable to ban member."
        }
    }

    suspend fun unblockMember(clubId: String, userId: Long) {
        val response = service.unblockMember(clubId, userId)
        check(response.success) {
            response.error ?: response.message ?: "Unable to unblock member."
        }
    }
}

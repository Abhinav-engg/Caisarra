
package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//@Serializable
//data class ClubDto(
//    val id: Long = 0L,
//    @SerialName("club_id")
//    val clubId: Long? = null,
//    val name: String = "",
//    val description: String? = null,
//    @SerialName("share_code")
//    val shareCode: String? = null,
//    @SerialName("member_count")
//    val memberCount: Int = 0,
//    @SerialName("owner_id")
//    val ownerId: Long? = null,
//    val role: String? = null
//) {
//    val effectiveId: Long
//        get() = clubId ?: id
//}

@Serializable
data class ClubDto(
    val id: String = "",
    @SerialName("club_id")
    val clubId: String? = null,
    val name: String = "",
    val description: String? = null,
    @SerialName("share_code")
    val shareCode: String? = null,
    @SerialName("member_count")
    val memberCount: Int = 0,
    @SerialName("owner_id")
    val ownerId: Long? = null,
    val role: String? = null
) {
    val effectiveId: String
        get() = clubId ?: id
}


@Serializable
data class ClubListData(
    val clubs: List<ClubDto> = emptyList()
)

@Serializable
data class CreateClubRequest(
    val name: String,
    val description: String
)

@Serializable
data class JoinClubRequest(
    @SerialName("club_id")
    val clubId: Long
)

@Serializable
data class ClubMemberDto(
    @SerialName("user_id")
    val userId: Long = 0L,
    val username: String = "Player",
    val role: String = "member",
    val rating: Int? = null
)

@Serializable
data class ClubMembersData(
    val members: List<ClubMemberDto> = emptyList()
)

@Serializable
data class ClubLeaderboardPlayerDto(
    @SerialName("user_id")
    val userId: Long = 0L,
    val username: String = "Player",
    val rating: Int = 0,
    val rank: Int = 0
)


@Serializable
data class ClubLeaderboardData(
    val leaderboard: List<ClubLeaderboardPlayerDto> = emptyList()
)

@Serializable
data class ClubMessageDto(
    // Backend message IDs are UUID strings, not numeric IDs.
    val id: String = "",
    @SerialName("user_id")
    val userId: Long = 0L,
    val username: String = "Player",
    val message: String = "",
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class ClubMessagesData(
    val messages: List<ClubMessageDto> = emptyList()
)

@Serializable
data class SendClubMessageRequest(
    val message: String
)

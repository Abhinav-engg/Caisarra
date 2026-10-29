package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

enum class PrivacyOption(val value: String, val label: String, val description: String) {
    EVERYONE("everyone", "Everyone", "Anyone can interact with you"),
    ONLY_FRIENDS("only_friends", "Friends Only", "Only accepted friends can interact with you"),
    NOBODY("nobody", "Nobody", "Interaction is completely disabled");

    companion object {
        fun fromValue(value: String?): PrivacyOption =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: EVERYONE
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PrivacySettingsDto(
    @SerialName("direct_message")
    @JsonNames("direct_messages", "directMessage", "directMessages")
    val directMessage: String? = "everyone",

    @SerialName("club")
    @JsonNames("clubs", "clubOption")
    val club: String? = "everyone",

    @SerialName("tournament")
    @JsonNames("tournaments", "tournamentOption")
    val tournament: String? = "everyone",

    @SerialName("invite_friend")
    @JsonNames("invite_friends", "inviteFriend", "inviteFriends", "friend_invite", "friend_invites")
    val inviteFriend: String? = "everyone"
) {
    val directMessageOption: PrivacyOption get() = PrivacyOption.fromValue(directMessage)
    val clubOption: PrivacyOption get() = PrivacyOption.fromValue(club)
    val tournamentOption: PrivacyOption get() = PrivacyOption.fromValue(tournament)
    val inviteFriendOption: PrivacyOption get() = PrivacyOption.fromValue(inviteFriend)
}

@Serializable
data class UpdatePrivacySettingsRequest(
    @SerialName("direct_message")
    val directMessage: String? = null,
    @SerialName("club")
    val club: String? = null,
    @SerialName("tournament")
    val tournament: String? = null,
    @SerialName("invite_friend")
    val inviteFriend: String? = null
)

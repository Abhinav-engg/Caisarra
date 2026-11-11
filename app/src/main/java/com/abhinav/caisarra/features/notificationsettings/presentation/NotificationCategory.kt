package com.abhinav.caisarra.features.notificationsettings.presentation

enum class NotificationCategory(
    val section: String,
    val title: String,
    val subtitle: String,
    val types: List<String>
) {
    CHALLENGES(
        "GAMES",
        "Game Challenges",
        "Invites and challenge responses",
        listOf("game_challenge", "challenge_accepted", "challenge_declined", "challenge_cancelled")
    ),
    TOURNAMENTS(
        "GAMES",
        "Tournaments",
        "Round starts, results and cancellations",
        listOf("tournament_round_started", "tournament_finished", "tournament_cancelled")
    ),
    FRIENDS(
        "SOCIAL",
        "Friend Requests",
        "New requests and accepted requests",
        listOf("friend_request", "friend_accepted")
    ),
    DIRECT_MESSAGES(
        "SOCIAL",
        "Direct Messages",
        "Push alerts for new private messages",
        listOf("direct_message")
    ),
    COMMUNITY(
        "SOCIAL",
        "Community",
        "Comments and reactions on your posts",
        listOf("post_comment", "post_reaction")
    ),
    CLUBS(
        "SOCIAL",
        "Clubs",
        "Role changes and membership updates",
        listOf("club_promoted", "club_demoted", "club_new_owner", "club_kicked", "club_banned")
    )
}

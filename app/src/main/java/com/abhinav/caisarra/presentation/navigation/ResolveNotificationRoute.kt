package com.abhinav.caisarra.presentation.navigation

import com.abhinav.caisarra.presentation.invite.InviteCodeParser

fun resolveNotificationRoute(
    type: String?,
    getParam: (String) -> String?
): String {
    val normalized = type?.trim()?.lowercase().orEmpty()
    return when (normalized) {
        "post_comment",
        "comment",
        "post_reaction",
        "reaction" -> {
            val postId = (getParam("post_id") ?: getParam("postId"))?.toLongOrNull()
            if (postId != null && postId > 0L) {
                AppRoutes.communityPost(postId)
            } else {
                AppRoutes.COMMUNITY
            }
        }

        "direct_message",
        "message",
        "dm",
        "chat",
        "chat_message" -> {
            val userId = getParam("user_id")?.toLongOrNull()
                ?: getParam("userId")?.toLongOrNull()
                ?: getParam("sender_id")?.toLongOrNull()
                ?: getParam("senderId")?.toLongOrNull()
            val username = getParam("username")
                ?: getParam("sender_username")
                ?: getParam("senderUsername")
                ?: getParam("sender")
                ?: getParam("user_name")
            val avatarUrl = getParam("avatar_url")
                ?: getParam("avatarUrl")
                ?: getParam("avatar")
            if (userId != null && userId > 0L && !username.isNullOrBlank()) {
                AppRoutes.directMessage(userId, username, avatarUrl)
            } else {
                AppRoutes.DIRECT_MESSAGES_LIST
            }
        }

        "club_message",
        "club_chat_message",
        "club_chat" -> {
            val clubId = getParam("club_id") ?: getParam("clubId")
            if (!clubId.isNullOrBlank()) {
                AppRoutes.clubChat(clubId)
            } else {
                AppRoutes.CLUBS
            }
        }

        "friend_request" -> AppRoutes.PLAY_WITH_FRIEND

        "friend_accepted",
        "user_follow",
        "follow" -> {
            val username = getParam("username")
                ?: getParam("sender_username")
                ?: getParam("senderUsername")
            if (!username.isNullOrBlank()) {
                AppRoutes.profile(username)
            } else {
                AppRoutes.NOTIFICATIONS
            }
        }

        "game_challenge",
        "game_invite",
        "game_invitation",
        "challenge",
        "invite",
        "challenge_received",
        "invite_received" -> {
            val raw = getParam("code")
                ?: getParam("invite_code")
                ?: getParam("inviteCode")
                ?: getParam("challenge_code")
                ?: getParam("challengeCode")
                ?: getParam("deep_link")
                ?: getParam("link")
            val code = raw?.let { InviteCodeParser.extractCode(it) }?.takeIf { it.isNotBlank() }
            if (!code.isNullOrBlank()) {
                AppRoutes.joinInvite(code)
            } else {
                AppRoutes.PLAY_WITH_FRIEND
            }
        }

        "challenge_accepted",
        "game_challenge_accepted",
        "invite_accepted",
        "game_invite_accepted" -> {
            val gameId = getParam("game_id") ?: getParam("gameId")
            if (!gameId.isNullOrBlank()) {
                AppRoutes.onlineGame(gameId)
            } else {
                AppRoutes.PLAY_WITH_FRIEND
            }
        }

        "challenge_declined",
        "challenge_cancelled" -> {
            val username = getParam("username")
                ?: getParam("challenger_username")
                ?: getParam("challengerUsername")
            if (!username.isNullOrBlank()) {
                AppRoutes.profile(username)
            } else {
                AppRoutes.PLAY_WITH_FRIEND
            }
        }

        "tournament_round_started" -> {
            val gameId = getParam("game_id") ?: getParam("gameId")
            val tournamentId = getParam("tournament_id") ?: getParam("tournamentId")
            if (!gameId.isNullOrBlank()) {
                AppRoutes.onlineGame(gameId, tournamentId)
            } else if (!tournamentId.isNullOrBlank()) {
                AppRoutes.tournamentDetail(tournamentId)
            } else {
                AppRoutes.TOURNAMENTS
            }
        }

        "tournament_finished",
        "tournament_completed" -> {
            val tournamentId = getParam("tournament_id") ?: getParam("tournamentId")
            if (!tournamentId.isNullOrBlank()) {
                AppRoutes.tournamentDetail(tournamentId)
            } else {
                AppRoutes.TOURNAMENTS
            }
        }

        "tournament_cancelled" -> AppRoutes.TOURNAMENTS

        "club_promoted",
        "club_demoted",
        "club_new_owner" -> {
            val clubId = getParam("club_id") ?: getParam("clubId")
            if (!clubId.isNullOrBlank()) {
                AppRoutes.clubDetail(clubId)
            } else {
                AppRoutes.CLUBS
            }
        }

        "club_kicked",
        "club_banned" -> AppRoutes.CLUBS

        "game_turn",
        "daily_turn",
        "turn",
        "move",
        "game_move" -> {
            val gameId = getParam("game_id") ?: getParam("gameId")
            if (!gameId.isNullOrBlank()) {
                AppRoutes.onlineGame(gameId)
            } else {
                AppRoutes.HOME
            }
        }

        "daily_puzzle",
        "puzzle",
        "streak" -> AppRoutes.DAILY_PUZZLE

        else -> AppRoutes.NOTIFICATIONS
    }
}

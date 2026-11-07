package com.abhinav.caisarra.presentation.navigation

import android.content.Intent
import androidx.navigation.NavController
import com.abhinav.caisarra.data.remote.dto.NotificationItemDto
import com.abhinav.caisarra.data.remote.dto.getLongData
import com.abhinav.caisarra.data.remote.dto.getStringData
import com.abhinav.caisarra.presentation.invite.InviteCodeParser

fun handleNotificationItemClick(
    notification: NotificationItemDto,
    navController: NavController
) {
    val type = notification.type.lowercase()
    when (type) {
        "friend_request" -> {
            navController.navigateOnce(AppRoutes.PLAY_WITH_FRIEND)
        }
        "friend_accepted" -> {
            val username = notification.getStringData("username")
            navController.navigateOnce(AppRoutes.profile(username))
        }
        "game_challenge",
        "game_invite",
        "game_invitation",
        "challenge",
        "invite",
        "challenge_received",
        "invite_received" -> {
            val raw = notification.getStringData("code")
                ?: notification.getStringData("invite_code")
                ?: notification.getStringData("challenge_code")
                ?: notification.getStringData("deep_link")
                ?: notification.getStringData("link")
            val code = raw?.let { InviteCodeParser.extractCode(it) }?.takeIf { it.isNotBlank() }
            if (!code.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.joinInvite(code))
            } else {
                navController.navigateOnce(AppRoutes.PLAY_WITH_FRIEND)
            }
        }
        "challenge_declined" -> {
            val username = notification.getStringData("username")
            navController.navigateOnce(AppRoutes.profile(username))
        }
        "tournament_round_started" -> {
            val gameId = notification.getStringData("game_id")
            val tournamentId = notification.getStringData("tournament_id")
            if (!gameId.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.onlineGame(gameId))
            } else if (!tournamentId.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.tournamentDetail(tournamentId))
            } else {
                navController.navigateOnce(AppRoutes.TOURNAMENTS)
            }
        }
        "tournament_finished",
        "tournament_completed" -> {
            val tournamentId = notification.getStringData("tournament_id")
            if (!tournamentId.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.tournamentDetail(tournamentId))
            } else {
                navController.navigateOnce(AppRoutes.TOURNAMENTS)
            }
        }
        "tournament_cancelled" -> {
            navController.navigateOnce(AppRoutes.TOURNAMENTS)
        }
        "post_comment",
        "post_reaction" -> {
            navController.navigateOnce(AppRoutes.COMMUNITY)
        }
        "club_promoted",
        "club_demoted",
        "club_new_owner" -> {
            val clubId = notification.getStringData("club_id")
            if (!clubId.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.clubDetail(clubId))
            } else {
                navController.navigateOnce(AppRoutes.CLUBS)
            }
        }
        "club_kicked",
        "club_banned" -> {
            navController.navigateOnce(AppRoutes.CLUBS)
        }
        "direct_message" -> {
            val userId = notification.getLongData("user_id")
            val username = notification.getStringData("username")
            if (userId != null && !username.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.directMessage(userId, username))
            } else {
                navController.navigateOnce(AppRoutes.DIRECT_MESSAGES_LIST)
            }
        }
        else -> {
        }
    }
}

fun handleIntentNotificationNavigation(
    intent: Intent,
    notifType: String?,
    navController: NavController
) {
    val type = notifType?.lowercase().orEmpty()
    when (type) {
        "friend_request" -> {
            navController.navigateOnce(AppRoutes.PLAY_WITH_FRIEND)
        }
        "friend_accepted" -> {
            val username = intent.getStringExtra("username")
            navController.navigateOnce(AppRoutes.profile(username))
        }
        "game_challenge",
        "game_invite",
        "game_invitation",
        "challenge",
        "invite",
        "challenge_received",
        "invite_received" -> {
            val raw = intent.getStringExtra("code")
                ?: intent.getStringExtra("invite_code")
                ?: intent.getStringExtra("challenge_code")
                ?: intent.getStringExtra("deep_link")
                ?: intent.getStringExtra("link")
                ?: intent.dataString
            val code = raw?.let { InviteCodeParser.extractCode(it) }?.takeIf { it.isNotBlank() }
            if (!code.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.joinInvite(code))
            } else {
                navController.navigateOnce(AppRoutes.PLAY_WITH_FRIEND)
            }
        }
        "challenge_accepted",
        "game_challenge_accepted",
        "invite_accepted",
        "game_invite_accepted" -> {
            val gameId = intent.getStringExtra("game_id") ?: intent.getStringExtra("gameId")
            if (!gameId.isNullOrBlank()) {
                navController.navigate(AppRoutes.onlineGame(gameId)) {
                    launchSingleTop = true
                }
            }
        }
        "challenge_declined" -> {
            val username = intent.getStringExtra("username")
            navController.navigateOnce(AppRoutes.profile(username))
        }
        "tournament_round_started" -> {
            val gameId = intent.getStringExtra("game_id")
            val tournamentId = intent.getStringExtra("tournament_id")
            if (!gameId.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.onlineGame(gameId))
            } else if (!tournamentId.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.tournamentDetail(tournamentId))
            } else {
                navController.navigateOnce(AppRoutes.TOURNAMENTS)
            }
        }
        "tournament_finished",
        "tournament_completed" -> {
            val tournamentId = intent.getStringExtra("tournament_id")
            if (!tournamentId.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.tournamentDetail(tournamentId))
            } else {
                navController.navigateOnce(AppRoutes.TOURNAMENTS)
            }
        }
        "tournament_cancelled" -> {
            navController.navigateOnce(AppRoutes.TOURNAMENTS)
        }
        "post_comment",
        "post_reaction" -> {
            navController.navigateOnce(AppRoutes.COMMUNITY)
        }
        "club_promoted",
        "club_demoted",
        "club_new_owner" -> {
            val clubId = intent.getStringExtra("club_id")
            if (!clubId.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.clubDetail(clubId))
            } else {
                navController.navigateOnce(AppRoutes.CLUBS)
            }
        }
        "club_kicked",
        "club_banned" -> {
            navController.navigateOnce(AppRoutes.CLUBS)
        }
        "direct_message" -> {
            val userId = intent.getStringExtra("user_id")?.toLongOrNull()
            val username = intent.getStringExtra("username")
            if (userId != null && !username.isNullOrBlank()) {
                navController.navigateOnce(AppRoutes.directMessage(userId, username))
            } else {
                navController.navigateOnce(AppRoutes.DIRECT_MESSAGES_LIST)
            }
        }
        else -> {
            val dataUri = intent.data
            if (dataUri == null) {
                navController.navigateOnce(AppRoutes.NOTIFICATIONS)
            }
        }
    }
}

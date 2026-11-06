package com.abhinav.caisarra.presentation.navigation

import android.net.Uri
import com.abhinav.caisarra.presentation.viewmodel.VerifyPurpose

/**
 * Central routing contract for Caisarra application navigation.
 * Defines all route constants, path templates, and type-safe URI builder functions
 * used across navigation graphs and deep links.
 */
object AppRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"
    const val VERIFY = "verify/{purpose}/{email}"
    const val NEW_PASSWORD = "new_password"
    const val RATING = "rating"

    fun verify(email: String, purpose: VerifyPurpose): String {
        return "verify/${purpose.name}/${Uri.encode(email)}"
    }
    const val HOME = "home"
    const val GUEST_HOME = "guest_options"
    const val PROFILE = "com/abhinav/caisarra/presentation/screens/components/profile"
    const val EDIT_PROFILE = "edit_profile"
    const val ALL_GAME_HISTORY = "all_game_history"
    const val STATS = "com/abhinav/caisarra/presentation/screens/components/stats"

    fun profile(username: String? = null): String {
        return if (username.isNullOrBlank()) {
            "com/abhinav/caisarra/presentation/screens/components/profile"
        } else {
            "profile?username=${Uri.encode(username.trim())}"
        }
    }

    fun stats(username: String? = null, mode: String? = null): String {
        val queryParts = mutableListOf<String>()
        if (!username.isNullOrBlank()) {
            queryParts.add("username=${Uri.encode(username.trim())}")
        }
        if (!mode.isNullOrBlank()) {
            queryParts.add("mode=${Uri.encode(mode.trim())}")
        }
        return if (queryParts.isEmpty()) {
            "com/abhinav/caisarra/presentation/screens/components/stats"
        } else {
            "stats?${queryParts.joinToString("&")}"
        }
    }
    const val GAME_SETUP = "game_setup"
    const val PASS_AND_PLAY_SETUP = "pass_and_play_setup"
    const val TIME_CONTROLS = "time_controls"
    const val MATCHMAKING = "matchmaking/{minutes}/{incrementSeconds}"
    const val ONLINE_GAME = "online_game/{gameId}"
    const val GAME_ANALYSIS = "game_analysis/{gameId}"
    const val GAME_HISTORY = "game_history/{gameId}"
    const val GAME = "game?resume={resume}" +
            "&white={white}" +
            "&black={black}" +
            "&minutes={minutes}" +
            "&flip={flip}" +
            "&undo={undo}" +
            "&clock={clock}"
    fun newGame(
        white: String,
        black: String,
        minutes: Int,
        flip: Boolean,
        undo: Boolean,
        clockEnabled: Boolean
    ): String {
        return "game" + "?resume=false" +
                "&white=${Uri.encode(white)}" +
                "&black=${Uri.encode(black)}" +
                "&minutes=$minutes" +
                "&flip=$flip" +
                "&undo=$undo" +
                "&clock=$clockEnabled"
    }

    fun gameHistory(gameId: String): String {
        return "game_history/${Uri.encode(gameId)}"
    }
    fun onlineGame(gameId: String): String {
        return "online_game/${Uri.encode(gameId)}"
    }
    fun gameAnalysis(gameId: String): String {
        return "game_analysis/${Uri.encode(gameId)}"
    }

    fun matchmaking(minutes: Int, incrementSeconds: Int): String {
        return "matchmaking/$minutes/$incrementSeconds"
    }
    const val BOT_SETUP = "bot_setup"
    const val BOT_GAME = "bot_game/{gameId}"
    const val BOT_HISTORY = "bot_history"

    fun botGame(gameId: String): String = "bot_game/${Uri.encode(gameId)}"

    fun botHistory(): String = BOT_HISTORY
    const val PLAY_WITH_FRIEND = "play_with_friend"
    const val PLAY_WITH_FRIEND_HOME = "play_with_friend_home"
    const val CREATE_INVITE = "create_invite"
    const val JOIN_INVITE = "play/{code}"
    const val DIRECT_MESSAGES_LIST = "direct_messages_list"
    const val DIRECT_MESSAGE = "direct_message/{userId}/{username}?avatarUrl={avatarUrl}"
    const val NEARBY = "nearby"
    const val SOCIAL = "social"

    fun joinInvite(code: String): String = "play/${Uri.encode(code)}"

    fun directMessage(userId: Long, username: String, avatarUrl: String? = null): String {
        return if (avatarUrl.isNullOrBlank()) {
            "direct_message/$userId/${Uri.encode(username)}"
        } else {
            "direct_message/$userId/${Uri.encode(username)}?avatarUrl=${Uri.encode(avatarUrl.trim())}"
        }
    }
    const val TOURNAMENTS = "tournaments"
    const val CREATE_TOURNAMENT = "create_tournament"
    const val TOURNAMENT_DETAIL = "tournament/{id}?code={code}"

    fun tournamentDetail(id: String, inviteCode: String? = null): String {
        val queryParam = inviteCode?.let { "?code=${Uri.encode(it)}" } ?: ""
        return "tournament/${Uri.encode(id)}$queryParam"
    }
    const val PUZZLES = "puzzles"
    const val DAILY_PUZZLE = "daily_puzzle"
    const val PUZZLE_RUSH = "puzzle_rush?mode={mode}"
    const val CUSTOM_TACTICS = "custom_tactics"
    const val MINI_MATES = "mini_mates"

    fun puzzleRush(mode: String = "3min"): String = "puzzle_rush?mode=$mode"

    const val COMMUNITY = "community"
    const val CLUBS = "clubs"
    const val CLUB_DETAIL = "club_detail/{clubId}"

    fun clubDetail(clubId: String): String =
        "club_detail/${Uri.encode(clubId)}"

    const val MENU = "menu"
    const val THEME = "theme"
    const val LEADERBOARD = "leaderboard"
    const val NEWS = "news"
    const val SETTINGS_DETAIL = "settings_detail"
    const val PRIVACY_SETTINGS = "privacy_settings"
    const val DELETE_ACCOUNT = "delete_account"
    const val WATCH = "watch"
    const val SPECTATE_GAME = "spectate/{watchCode}"
    const val NOTIFICATIONS = "notifications"

    fun spectateGame(watchCode: String): String =
        "spectate/${Uri.encode(watchCode.trim())}"
}

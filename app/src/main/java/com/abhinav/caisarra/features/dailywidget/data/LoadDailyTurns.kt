package com.abhinav.caisarra.features.dailywidget.data

import android.content.Context
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.features.dailywidget.domain.DailyTurn

private val finishedStatuses = setOf("finished", "completed", "aborted", "expired", "resigned", "timeout")

suspend fun loadDailyTurns(context: Context): List<DailyTurn> {
    val tokens = TokenManager.get(context)
    val myId = tokens.getAccessToken()?.let { TokenExpiryChecker.getUserId(it) }
    val me = tokens.getUsername()?.takeUnless { it.contains("@") }
    val games = runCatching { RetrofitInstance.createGamesService(tokens).getGameHistory() }.getOrDefault(emptyList())
    return games.filter {
        it.endedAt == null && it.status.lowercase() !in finishedStatuses &&
            (it.timeControlMode.equals("daily", ignoreCase = true) || it.timeControlMinutes >= 1440)
    }.mapNotNull { game ->
        val isWhite = when {
            (myId != null && game.whitePlayerId == myId) || (me != null && game.whiteUsername.equals(me, ignoreCase = true)) -> true
            (myId != null && game.blackPlayerId == myId) || (me != null && game.blackUsername.equals(me, ignoreCase = true)) -> false
            else -> return@mapNotNull null
        }
        val turn = game.position.split(" ").getOrNull(1) ?: game.currentTurn.lowercase()
        DailyTurn(
            gameId = game.id,
            opponent = (if (isWhite) game.blackUsername else game.whiteUsername)?.takeIf { it.isNotBlank() } ?: "Opponent",
            isMyTurn = if (isWhite) turn == "white" || turn == "w" else turn == "black" || turn == "b"
        )
    }.distinctBy { it.gameId }.sortedByDescending { it.isMyTurn }
}

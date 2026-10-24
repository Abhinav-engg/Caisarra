package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.clearFocusOnTap
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.BotGame
import com.abhinav.caisarra.presentation.common.ClickGuard
import com.abhinav.caisarra.presentation.common.singleClick
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.viewmodel.BotHistoryViewModel
import com.caisaara.ui.theme.BotActiveBg
import com.caisaara.ui.theme.BotCardBg
import com.caisaara.ui.theme.BotCardBorder
import com.caisaara.ui.theme.BotDrawBlue
import com.caisaara.ui.theme.BotFinishedBg
import com.caisaara.ui.theme.BotLossRed
import com.caisaara.ui.theme.BotSelectedBorder
import com.caisaara.ui.theme.BotTextMuted
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.TextSizeTiny

@Composable
fun BotHistoryScreen(
    viewModel: BotHistoryViewModel,
    onOpenGame: (String) -> Unit,
    onNewGame: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .clearFocusOnTap()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = singleClick(onBack)) {
                Text(
                    text = "← BACK",
                    color = Color.White,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = singleClick(onNewGame)) {
                Text(
                    text = "+ NEW GAME",
                    color = SetupColors.Accent,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "BOT GAMES",
            color = Color.White,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = TextSizeHeading
        )

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading && state.games.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BotSelectedBorder)
                }
            }

            state.errorMessage != null && state.games.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = BotLossRed,
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeBody
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = singleClick(viewModel::loadGames)) {
                            Text("RETRY", color = BotSelectedBorder, fontFamily = JetBrainsMono)
                        }
                    }
                }
            }

            state.games.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No bot games played yet.",
                        color = BotTextMuted,
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeBody
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.games) { game ->
                        BotGameHistoryItem(
                            game = game,
                            onClick = {
                                if (ClickGuard.canClick()) {
                                    onOpenGame(game.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotGameHistoryItem(
    game: BotGame,
    onClick: () -> Unit
) {
    val isActive = game.status == "active"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BotCardBg)
            .border(1.dp, if (isActive) BotSelectedBorder else BotCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (game.botId != null) "Bot (${game.botRating})" else "Custom Bot (${game.botRating})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextSizeSubhead,
                    fontFamily = JetBrainsMono
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) BotActiveBg else BotFinishedBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isActive) "ACTIVE" else "FINISHED",
                        color = if (isActive) BotSelectedBorder else BotTextMuted,
                        fontSize = TextSizeTiny,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playing as ${game.playerColor.replaceFirstChar { it.uppercase() }} · ${game.moves.size} moves",
                    color = BotTextMuted,
                    fontSize = TextSizeCaption
                )

                if (!isActive) {
                    val resultText = when (game.result) {
                        "white_win" -> if (game.playerColor == "white") "Won" else "Lost"
                        "black_win" -> if (game.playerColor == "black") "Won" else "Lost"
                        "draw" -> "Draw"
                        else -> "Ended"
                    }
                    val resultColor = when (resultText) {
                        "Won" -> BotSelectedBorder
                        "Lost" -> BotLossRed
                        else -> BotDrawBlue
                    }
                    Text(
                        text = resultText,
                        color = resultColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextSizeBadge,
                        fontFamily = JetBrainsMono
                    )
                }
            }
        }
    }
}

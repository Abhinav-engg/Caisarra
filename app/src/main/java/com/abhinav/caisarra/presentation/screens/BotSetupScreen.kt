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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.components.clearFocusOnTap
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.Bot
import com.abhinav.caisarra.presentation.common.ClickGuard
import com.abhinav.caisarra.presentation.common.singleClick
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage
import com.abhinav.caisarra.presentation.game.viewmodel.BotSetupViewModel
import com.caisaara.ui.theme.BotCardBg
import com.caisaara.ui.theme.BotCardBorder
import com.caisaara.ui.theme.BotLockedBg
import com.caisaara.ui.theme.BotLockedBorder
import com.caisaara.ui.theme.BotLockedName
import com.caisaara.ui.theme.BotLockedRating
import com.caisaara.ui.theme.BotLockedStatus
import com.caisaara.ui.theme.BotLossRed
import com.caisaara.ui.theme.BotSelectedBorder
import com.caisaara.ui.theme.BotSliderTrack
import com.caisaara.ui.theme.BotWarningBg
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeMicro
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.TextSizeTiny
@Composable
fun BotSetupScreen(
    viewModel: BotSetupViewModel,
    onGameCreated: (String) -> Unit,
    onViewHistory: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.createdGameId) {
        state.createdGameId?.let { id ->
            onGameCreated(id)
            viewModel.onGameOpened()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap()
    ) {
        SetupPage {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SetupHeader("Play Bots", onBack)
                TextButton(
                    onClick = singleClick(onViewHistory)
                ) {
                    Text(
                        text = "GAMES",
                        color = SetupColors.Accent,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextSizeBadge
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.isLoadingBots && state.bots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SetupColors.Accent)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SectionTitle("CHOOSE BOT")
                    Spacer(Modifier.height(8.dp))

                    val levels = listOf("easy", "medium", "hard", "expert")
                    levels.forEach { level ->
                        val botsInLevel = state.bots.filter { it.level.equals(level, ignoreCase = true) }
                        if (botsInLevel.isNotEmpty()) {
                            Text(
                                text = level.uppercase(),
                                color = SetupColors.Secondary,
                                fontSize = TextSizeTiny,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMono,
                                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(botsInLevel) { bot ->
                                    BotCard(
                                        bot = bot,
                                        isSelected = !state.isCustomRating && state.selectedBotId == bot.id,
                                        onSelect = {
                                            if (ClickGuard.canClick()) {
                                                viewModel.onBotSelected(bot)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (state.isCustomRating) SetupColors.Card else BotCardBg)
                            .border(
                                1.dp,
                                if (state.isCustomRating) BotSelectedBorder else BotCardBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(onClick = singleClick { viewModel.onCustomRatingSelected() })
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Custom Rating Bot",
                                    color = if (state.isCustomRating) SetupColors.Accent else SetupColors.Text,
                                    fontSize = TextSizeSubhead,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${state.customRating}",
                                    color = SetupColors.Accent,
                                    fontSize = TextSizeSubhead,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMono
                                )
                            }

                            if (state.isCustomRating) {
                                Spacer(Modifier.height(12.dp))
                                Slider(
                                    value = state.customRating.toFloat(),
                                    onValueChange = { viewModel.onCustomRatingChanged(it.toInt()) },
                                    valueRange = 400f..3000f,
                                    steps = 25,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = SetupColors.Accent,
                                        inactiveTrackColor = BotSliderTrack
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("400", color = SetupColors.Secondary, fontSize = TextSizeTiny)
                                    Text("1500", color = SetupColors.Secondary, fontSize = TextSizeTiny)
                                    Text("3000", color = SetupColors.Secondary, fontSize = TextSizeTiny)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    SectionTitle("PLAY AS")
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("white", "random", "black").forEach { color ->
                            val isSelected = state.selectedColor.equals(color, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BotCardBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) BotSelectedBorder else BotCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable(onClick = singleClick { viewModel.onColorSelected(color) }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = color.replaceFirstChar { it.uppercase() },
                                    color = if (isSelected) SetupColors.Accent else SetupColors.Text,
                                    fontSize = TextSizeSubhead,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (state.hasActiveLimitReached) {
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BotWarningBg)
                                .border(1.dp, BotLossRed, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.errorMessage ?: "You can play at most 5 bot games at a time.",
                                color = Color.White,
                                fontSize = TextSizeBadge
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = singleClick(onViewHistory)) {
                                Text("VIEW ACTIVE GAMES", color = SetupColors.Accent, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (state.errorMessage != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = BotLossRed,
                            fontSize = TextSizeBadge
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    GeneralButton(
                        text = if (state.isStartingGame) "STARTING..." else "START GAME",
                        onClick = { viewModel.startGame() },
                        enabled = !state.isStartingGame
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BotCard(
    bot: Bot,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isLocked = !bot.unlocked

    Box(
        modifier = Modifier
            .width(115.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isLocked) BotLockedBg else BotCardBg)
            .border(
                1.dp,
                when {
                    isSelected -> BotSelectedBorder
                    isLocked -> BotLockedBorder
                    else -> BotCardBorder
                },
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = !isLocked, onClick = onSelect)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bot.rating}",
                    color = if (isLocked) BotLockedRating else SetupColors.Accent,
                    fontSize = TextSizeCaption,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                )

                when {
                    isLocked -> {
                        Text("🔒", fontSize = TextSizeCaption)
                    }
                    bot.beaten -> {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(BotSelectedBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.Black, fontSize = TextSizeMicro, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column {
                Text(
                    text = bot.name,
                    color = if (isLocked) BotLockedName else SetupColors.Text,
                    fontSize = TextSizeBody,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = if (isLocked) "Locked" else if (bot.beaten) "Beaten" else "Unlocked",
                    color = if (isLocked) BotLockedStatus else SetupColors.Secondary,
                    fontSize = TextSizeTiny
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = SetupColors.Secondary,
        fontSize = TextSizeCaption,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        fontFamily = JetBrainsMono
    )
}

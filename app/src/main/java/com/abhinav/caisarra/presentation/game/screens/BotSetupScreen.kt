package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage
import com.abhinav.caisarra.presentation.game.viewmodel.BotSetupViewModel

private data class BotChoice(val value: String, val label: String)

private val Levels = listOf(
    BotChoice("easy", "Easy · 1000"),
    BotChoice("medium", "Medium · 1500"),
    BotChoice("hard", "Hard · 2000"),
    BotChoice("expert", "Expert · 2500"),
    BotChoice("custom", "Custom")
)

private val Colors = listOf(
    BotChoice("white", "White"),
    BotChoice("black", "Black"),
    BotChoice("random", "Random")
)

@Composable
fun BotSetupScreen(
    viewModel: BotSetupViewModel,
    onGameCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.createdGameId) {
        state.createdGameId?.let {
            onGameCreated(it)
            viewModel.onGameOpened()
        }
    }

    SetupPage {
        SetupHeader("Play Bots", onBack)
        Spacer(Modifier.height(24.dp))
        SectionTitle("BOT LEVEL")
        Spacer(Modifier.height(12.dp))
        ChoiceGrid(Levels, state.level, 2, viewModel::onLevelSelected)
        if (state.level == "custom") {
            Spacer(Modifier.height(12.dp))
            RatingCard(state.customRating, viewModel::onCustomRatingChanged)
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("PLAY AS")
        Spacer(Modifier.height(12.dp))
        ChoiceGrid(Colors, state.color, 3, viewModel::onColorSelected)
        state.errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = Color(0xFFFF626B), fontSize = 14.sp)
        }
        Spacer(Modifier.height(24.dp))
        GeneralButton(
            text = if (state.isLoading) "STARTING..." else "START GAME",
            onClick = viewModel::startGame,
            enabled = !state.isLoading
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = SetupColors.Secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
private fun ChoiceGrid(choices: List<BotChoice>, selected: String, columns: Int, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        choices.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { choice ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(SetupColors.Card)
                            .clickable { onSelected(choice.value) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = choice.label,
                            color = if (choice.value == selected) SetupColors.Accent else SetupColors.Text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RatingCard(rating: Int, onRatingChange: (Int) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SetupColors.Card)
            .padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Bot Rating", color = SetupColors.Secondary, fontSize = 14.sp)
            Text("$rating", color = SetupColors.Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = rating.toFloat(),
            onValueChange = { onRatingChange(it.toInt()) },
            valueRange = 1000f..3000f,
            steps = 39,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = SetupColors.Accent,
                inactiveTrackColor = Color(0xFF253241)
            )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("1000", "1500", "2000", "2500", "3000").forEach {
                Text(it, color = Color(0xFF71839B), fontSize = 12.sp)
            }
        }
    }
}
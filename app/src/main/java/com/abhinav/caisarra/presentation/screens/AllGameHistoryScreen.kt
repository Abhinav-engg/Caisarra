
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.components.GameHistorySection
import com.abhinav.caisarra.presentation.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.viewmodel.HomeViewModel

private val HistoryBackground = Color(0xFF080D13)
private val HistoryWhite = Color(0xFFF5F5F5)

@Composable
fun AllGameHistoryScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onReviewGame: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HistoryBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = 24.dp
            )
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = "← BACK TO HOME",
                color = HistoryWhite,
                fontFamily = JetBrainsMono
            )
        }
        Spacer(Modifier.height(12.dp))

        Text(
            text = "ALL GAME HISTORY",
            color = HistoryWhite,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(Modifier.height(20.dp))
        if (state.error != null && state.gameHistory.isEmpty()) {
            Text(
                text = state.error ?: "Unable to load game history.",
                color = HistoryWhite,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp
            )
        } else {
            GameHistorySection(
                games = state.gameHistory,
                onReviewGame = onReviewGame,
                modifier = Modifier.fillMaxWidth(),
                showViewAll = false
            )
        }
    }
}

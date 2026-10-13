package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage

@Composable
fun BotSetupScreen(onStart: () -> Unit, onBack: () -> Unit) {
    SetupPage {
        SetupHeader("Play Bots", onBack)
        Spacer(Modifier.height(24.dp))
        Text("Bot setup coming soon", color = SetupColors.Secondary)
        Spacer(Modifier.height(20.dp))
        GeneralButton("START GAME", onStart)
    }
}
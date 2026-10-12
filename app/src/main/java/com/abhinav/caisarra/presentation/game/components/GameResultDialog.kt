package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.game.model.GameResult

@Composable
fun GameResultDialog(result: GameResult, onNewGame: () -> Unit, onHome: () -> Unit) {
    val title = when (result) {
        GameResult.WHITE_WINS -> "White Wins"
        GameResult.BLACK_WINS -> "Black Wins"
        GameResult.DRAW -> "Draw Accepted"
    }
    val subtitle = when (result) {
        GameResult.DRAW -> "Both players agreed to a draw."
        else -> "The game has ended."
    }
    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFF18212A)).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (result == GameResult.DRAW) {
                Icon(Icons.Default.Handshake, null, tint = Color(0xFF44E2A8))
            }
            Text(title, color = Color(0xFFF3F6FA), fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(subtitle, color = Color(0xFFB5C0CD), textAlign = TextAlign.Center)
            GeneralButton("New Game", onNewGame)
            Text("Home", color = Color(0xFFD5DCE6), fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onHome).padding(12.dp))
        }
    }
}

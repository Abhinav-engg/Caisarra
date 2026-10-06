package com.abhinav.caisarra.presentation.game.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.abhinav.caisarra.presentation.game.model.GameResult

@Composable
fun GameResultDialog(
    result: GameResult,
    onNewGame: () -> Unit,
    onHome: () -> Unit
) {
    val title = when (result) {
        GameResult.WHITE_WINS -> "White Wins!"
        GameResult.BLACK_WINS -> "Black Wins!"
        GameResult.DRAW -> "Draw"
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(title)
        },
        text = {
            Text("The game has ended.")
        },
        confirmButton = {
            TextButton(
                onClick = onNewGame
            ) {
                Text("New Game")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onHome
            ) {
                Text("Home")
            }
        }
    )
}
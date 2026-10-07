package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupViewModel
import androidx.compose.material3.Switch

@Composable
fun GameSetupScreen(
    viewModel: GameSetupViewModel,
    onStartGame: (
        whitePlayer: String,
        blackPlayer: String,
        minutes: Int,
        flipBoard: Boolean,
        undoEnabled: Boolean
    ) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080D13))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "NEW GAME",
            color = Color.White,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = state.whitePlayerName,
            onValueChange = viewModel::setWhitePlayerName,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("White Player")
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = Color.White
            )

        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = state.blackPlayerName,
            onValueChange = viewModel::setBlackPlayerName,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Black Player")
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = Color.White
            )

        )
        Spacer(modifier = Modifier.height(22.dp))

        Text(text = "Game Time",
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            listOf(3, 5, 10, 15).forEach { minutes ->

                val selected =
                    state.timeMinutes == minutes

                if (selected) {
                    Button(
                        onClick = {
                            viewModel.setTime(minutes)
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF10B981)
                            )
                    ) {
                        Text("${minutes}m")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            viewModel.setTime(minutes)
                        }
                    ) {
                        Text("${minutes}m")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Flip Board",
                color = Color.White
            )

            Switch(
                checked = state.flipBoard,
                onCheckedChange = {
                    viewModel.setFlipBoard(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Enable Undo",
                color = Color.White
            )

            Switch(
                checked = state.undoEnabled,
                onCheckedChange = {
                    viewModel.setUndoEnabled(it)
                }
            )
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = Color(0xFFEF4444),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (viewModel.validate()) {
                    onStartGame(
                        state.whitePlayerName,
                        state.blackPlayerName,
                        state.timeMinutes,
                        state.flipBoard,
                        state.undoEnabled
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF10B981)
                )
        ) {
            Text(
                text = "START GAME",
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BACK")
        }
    }
}
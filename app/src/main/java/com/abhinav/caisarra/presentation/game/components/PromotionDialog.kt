package com.abhinav.caisarra.presentation.game.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.abhinav.caisarra.presentation.game.model.PieceType

@Composable
fun PromotionDialog(
    onPieceSelected: (PieceType) -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {Text("Choose promotion")
        },
        text = {
            Text("Select the piece you want your pawn to become.")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onPieceSelected(PieceType.QUEEN)
                }
            ) {
                Text("Queen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onPieceSelected(PieceType.ROOK)
                }
            ) {
                Text("Rook")
            }
        }
    )
}

package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.abhinav.caisarra.presentation.game.model.PieceType

private val PromotionBackground = Color(0xFF0B1118)
private val PromotionCard = Color(0xFF141F2A)
private val PromotionBorder = Color(0xFF293744)
private val PromotionAccent = Color(0xFF10B981)
private val PromotionText = Color(0xFFF1F5F9)
private val PromotionMuted = Color(0xFF94A3B8)

@Composable
fun PromotionDialog(
    onPieceSelected: (PieceType) -> Unit
) {
    val pieces = listOf(
        Triple(PieceType.QUEEN, "♕", "Queen"),
        Triple(PieceType.ROOK, "♖", "Rook"),
        Triple(PieceType.BISHOP, "♗", "Bishop"),
        Triple(PieceType.KNIGHT, "♘", "Knight")
    )

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PromotionBackground)
                .border(
                    width = 1.dp,
                    color = PromotionBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(22.dp)
        ) {
            Text(
                text = "PAWN PROMOTION",
                color = PromotionAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Choose your piece",
                color = PromotionText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your pawn reached the final rank. " +
                        "Select the piece you want it to become.",
                color = PromotionMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pieces.take(2).forEach { (type, symbol, name) ->
                    PromotionPieceCard(
                        symbol = symbol,
                        name = name,
                        modifier = Modifier.weight(1f),
                        onClick = { onPieceSelected(type) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pieces.drop(2).forEach { (type, symbol, name) ->
                    PromotionPieceCard(
                        symbol = symbol,
                        name = name,
                        modifier = Modifier.weight(1f),
                        onClick = { onPieceSelected(type) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "QUEEN IS THE MOST COMMON CHOICE",
                color = PromotionMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun PromotionPieceCard(
    symbol: String,
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PromotionCard)
            .border(
                width = 1.dp,
                color = PromotionBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = symbol,
            color = PromotionText,
            fontSize = 42.sp,
            lineHeight = 48.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = name,
            color = PromotionText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (name == "Queen") "Most powerful" else "Select piece",
            color = if (name == "Queen") PromotionAccent else PromotionMuted,
            fontSize = 10.sp
        )
    }
}

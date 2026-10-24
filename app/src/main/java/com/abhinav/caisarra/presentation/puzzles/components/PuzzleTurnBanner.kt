package com.abhinav.caisarra.presentation.puzzles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.caisaara.ui.theme.SlateLight
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.ThemeSlateAccent
import com.caisaara.ui.theme.White

@Composable
fun PuzzleTurnBanner(
    playerColor: PieceColor,
    isCheckingMove: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateLight)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isCheckingMove) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = StatsBarSlate,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Checking move...",
                    color = StatsBarSlate,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            color = if (playerColor == PieceColor.WHITE) White else StatsBarSlate,
                            shape = RoundedCornerShape(3.dp)
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (playerColor == PieceColor.WHITE) ThemeSlateAccent else StatsBarSlate,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
                Text(
                    text = if (playerColor == PieceColor.WHITE) "White to Move" else "Black to Move",
                    color = StatsBarSlate,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

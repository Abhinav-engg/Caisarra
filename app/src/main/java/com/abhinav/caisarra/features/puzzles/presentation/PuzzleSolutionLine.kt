package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.ui.res.stringResource
import com.abhinav.caisarra.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.PuzzleCardBorder
import com.caisaara.ui.theme.PuzzleHeaderBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted

@Composable
fun PuzzleSolutionLine(moves: List<String>, currentPly: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PuzzleHeaderBg, shape)
            .border(1.dp, PuzzleCardBorder, shape)
            .padding(14.dp)
    ) {
        Text(text = stringResource(R.string.solution), color = StatsTextMuted, fontSize = 12.sp, letterSpacing = 1.sp)
        Row(
            modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            moves.forEachIndexed { index, move ->
                Text(
                    text = "${if (index % 2 == 0) "${index / 2 + 1}. " else ""}$move",
                    color = when {
                        index == currentPly - 1 -> StatsCyan
                        index < currentPly -> Color.White
                        else -> StatsTextMuted
                    },
                    fontSize = 15.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

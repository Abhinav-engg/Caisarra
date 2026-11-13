package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.data.remote.dto.MoveAnalysisDto
import com.abhinav.caisarra.features.mascot.domain.AnalysisMascotLines

@Composable
fun AnalysisMoveMascot(
    currentPly: Int,
    moves: List<MoveAnalysisDto>,
    modifier: Modifier = Modifier
) {
    val move = moves.getOrNull(currentPly - 1)
    val reaction = if (currentPly == 0 || move == null) {
        AnalysisMascotLines.start()
    } else {
        AnalysisMascotLines.forMove(move.classification, move.explanation, currentPly)
    }
    MascotSays(reaction = reaction, modifier = modifier, mascotSize = 48.dp)
}

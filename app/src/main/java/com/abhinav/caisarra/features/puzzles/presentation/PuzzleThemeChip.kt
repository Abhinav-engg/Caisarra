package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.features.puzzles.domain.puzzleThemeLabel

@Composable
fun PuzzleThemeChip(theme: String, modifier: Modifier = Modifier) {
    val accent = puzzleThemeAccent(theme)
    Row(
        modifier = modifier
            .background(accent.copy(alpha = 0.12f), CircleShape)
            .border(1.dp, accent.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(accent, CircleShape))
        Text(text = puzzleThemeLabel(theme), color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

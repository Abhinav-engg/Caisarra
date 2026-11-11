package com.abhinav.caisarra.features.minimates.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.features.minimates.data.MiniMatesOverviewDto
import com.abhinav.caisarra.presentation.screens.ModeItem
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.White

@Composable
fun MiniMatesLevelList(
    overview: MiniMatesOverviewDto,
    onBack: () -> Unit,
    onLevelClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
    ) {
        CaisaaraTopBar(title = "MINI MATES", onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text(text = "★ ${overview.totalStars} • ${overview.completedLevels} solved", color = White, fontSize = TextSizeSubhead) }
            overview.chapters.forEach { chapter ->
                item { Text(text = chapter.name, color = White, fontSize = TextSizeSubhead, fontWeight = FontWeight.SemiBold) }
                items(chapter.levels) { level ->
                    ModeItem(
                        title = level.title,
                        subtitle = "Mate in ${level.targetMoves} • ${starsText(level.stars)}",
                        iconRes = R.drawable.ic_puzzle_piece,
                        enabled = level.unlocked,
                        onClick = { if (level.unlocked) onLevelClick(level.levelNumber) }
                    )
                }
            }
        }
    }
}

fun starsText(stars: Int) = "★".repeat(stars) + "☆".repeat(3 - stars.coerceIn(0, 3))

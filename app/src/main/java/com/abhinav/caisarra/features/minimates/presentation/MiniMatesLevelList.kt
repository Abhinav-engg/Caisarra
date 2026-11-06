package com.abhinav.caisarra.features.minimates.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.features.minimates.domain.MiniMatesLevel
import com.abhinav.caisarra.features.minimates.domain.miniMatesLevels
import com.abhinav.caisarra.features.minimates.domain.parseMiniMatesLayout
import com.abhinav.caisarra.presentation.screens.ModeItem
import com.caisaara.ui.theme.StatsBg

@Composable
fun MiniMatesLevelList(
    onBack: () -> Unit,
    onLevelClick: (MiniMatesLevel) -> Unit
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
            items(miniMatesLevels) { level ->
                val size = parseMiniMatesLayout(level.layout).size
                ModeItem(
                    title = "Level ${level.number}",
                    subtitle = "$size×$size • Mate in ${level.mateIn}",
                    iconRes = R.drawable.ic_puzzle_piece,
                    enabled = true,
                    onClick = { onLevelClick(level) }
                )
            }
        }
    }
}

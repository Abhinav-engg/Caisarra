package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.StatsBg

@Composable
fun PuzzleLayout(
    header: @Composable ColumnScope.() -> Unit,
    board: @Composable () -> Unit,
    panel: @Composable ColumnScope.() -> Unit,
    footer: @Composable () -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(StatsBg).systemBarsPadding()) {
        val boardSize = minOf(maxHeight - 24.dp, maxWidth * 0.55f)
        if (maxWidth > maxHeight && maxWidth >= 600.dp) {
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(boardSize)) { board() }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    header()
                    panel()
                    footer()
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = header)
                BoxWithConstraints(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(minOf(maxWidth, maxHeight))) { board() }
                }
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = panel
                )
                footer()
            }
        }
    }
}

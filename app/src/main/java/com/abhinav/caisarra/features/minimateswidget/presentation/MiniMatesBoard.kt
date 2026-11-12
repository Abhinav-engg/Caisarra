package com.abhinav.caisarra.features.minimateswidget.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.size
import com.abhinav.caisarra.features.minimates.data.MiniMatesLevelDto
import com.abhinav.caisarra.features.minimateswidget.domain.fenRows
import com.abhinav.caisarra.presentation.game.model.BoardThemes

@Composable
fun MiniMatesBoard(level: MiniMatesLevelDto, square: Dp) {
    val pieces = fenRows(level.fen)
    val flipped = level.playerColor == "black"
    val viewport = level.viewport
    val ranks = (viewport.endRank downTo viewport.endRank - viewport.rows + 1).toList().let { if (flipped) it.reversed() else it }
    val files = List(viewport.cols) { viewport.startFile.first() - 'a' + it }.let { if (flipped) it.reversed() else it }
    Column(GlanceModifier.cornerRadius(10.dp)) {
        ranks.forEach { rank ->
            Row {
                files.forEach { file ->
                    val dark = (rank + file) % 2 == 1
                    Box(GlanceModifier.size(square).background(if (dark) BoardThemes.MidnightCyan.darkSquare else BoardThemes.MidnightCyan.lightSquare), contentAlignment = Alignment.Center) {
                        pieces.getOrNull(8 - rank)?.getOrNull(file)?.let {
                            Image(provider = ImageProvider(fenPieceDrawable(it)), contentDescription = null, modifier = GlanceModifier.size(square * 0.86f))
                        }
                    }
                }
            }
        }
    }
}

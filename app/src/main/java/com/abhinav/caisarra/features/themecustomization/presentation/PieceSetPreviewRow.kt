package com.abhinav.caisarra.features.themecustomization.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceTheme
import com.abhinav.caisarra.presentation.game.model.PieceType

@Composable
fun PieceSetPreviewRow(theme: PieceTheme) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(PieceType.KING, PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP).forEach {
            Image(painterResource(theme.getPieceDrawable(PieceColor.WHITE, it)), contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
}

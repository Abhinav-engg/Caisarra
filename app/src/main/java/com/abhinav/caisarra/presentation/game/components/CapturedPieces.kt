package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.local.PIECE_THEME_KEY
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceTheme
import com.abhinav.caisarra.presentation.game.model.PieceThemeId
import com.abhinav.caisarra.presentation.game.model.PieceThemes
import com.abhinav.caisarra.presentation.game.model.pointValue
import kotlinx.coroutines.flow.map

@Composable
fun CapturedPieces(
    pieces: List<Piece>,
    modifier: Modifier = Modifier,
    advantage: Int = 0,
    pieceTheme: PieceTheme? = null
) {
    val context = LocalContext.current
    val currentPieceThemeKey by remember(context) {
        context.settingsDataStore.data.map {
            it[PIECE_THEME_KEY] ?: PieceThemeId.CLASSIC.storageKey
        }
    }.collectAsState(initial = PieceThemeId.CLASSIC.storageKey)

    val activeTheme = pieceTheme ?: remember(currentPieceThemeKey) {
        PieceThemes.getByStorageKey(currentPieceThemeKey)
    }

    val sortedPieces = remember(pieces) {
        pieces.sortedWith(
            compareBy<Piece> { it.type.pointValue }
                .thenBy { it.type.ordinal }
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        sortedPieces.forEach { piece ->
            val resource = activeTheme.getPieceDrawable(piece.color, piece.type)

            Image(
                painter = painterResource(id = resource),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.Fit
            )
        }

        if (advantage > 0) {
            Text(
                text = "+$advantage",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

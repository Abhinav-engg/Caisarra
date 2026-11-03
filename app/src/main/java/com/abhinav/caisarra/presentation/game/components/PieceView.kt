package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.abhinav.caisarra.data.local.PIECE_THEME_KEY
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceTheme
import com.abhinav.caisarra.presentation.game.model.PieceThemeId
import com.abhinav.caisarra.presentation.game.model.PieceThemes
import kotlinx.coroutines.flow.map

@Composable
fun PieceView(
    piece: Piece,
    modifier: Modifier = Modifier,
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

    val pieceResource = activeTheme.getPieceDrawable(piece.color, piece.type)

    Image(
        painter = painterResource(id = pieceResource),
        contentDescription = "${piece.color} ${piece.type}",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

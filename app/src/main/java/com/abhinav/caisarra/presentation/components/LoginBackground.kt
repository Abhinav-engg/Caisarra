package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars

@Composable
fun LoginBackground(
    modifier: Modifier = Modifier,
    cardTop: Dp,
    cardRight: Dp,
    cardLeft:Dp,
    cardBottom: Dp,
    cardWidth: Dp,
    cardHeight:Dp,
) {

    val density = LocalDensity.current
    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.50f)
            )
    ) {
        val screenHeight = maxHeight

        ChessNotation(modifier = Modifier.fillMaxSize())

        val knightWidth = cardWidth * 0.38f
        val knightHeight = knightWidth * 1.75f
        val minimumKnightY = statusBarHeight + 10.dp
        val calculatedKnightY = cardTop - knightHeight * 0.55f
        val knightY =
            maxOf(calculatedKnightY,
                minimumKnightY
            )
        val knightX = cardRight - knightWidth * 0.65f

        Image(
            painter = painterResource(
                id = R.drawable.white_knight_image
            ),
            contentDescription = null,
            modifier = Modifier
                .size(
                    width = knightWidth,
                    height = knightHeight
                )
                .offset(
                    x = knightX,
                    y = knightY
                ),
            contentScale = ContentScale.FillBounds,
            alpha = 0.80f
        )

        val pieceWidth = cardWidth * 0.34f
        val pieceHeight = pieceWidth * 2.35f
        val pieceY = screenHeight - pieceHeight - 30.dp

        val pieceX = -pieceWidth * 0.15f

        Image(painter = painterResource(
                id = R.drawable.dark_chess_piece
            ),
            contentDescription = null,
            modifier = Modifier
                .size(
                    width = pieceWidth,
                    height = pieceHeight
                )
                .offset(x = pieceX,
                    y = pieceY
                ),
            contentScale = ContentScale.FillBounds,
            alpha = 0.45f
        )
        val gridWidth = cardWidth * 1.15f
        val gridHeight = gridWidth / 1.55f
        val gridX = cardRight - gridWidth * 0.75f
        val gridY = cardBottom - gridHeight * 0.50f

        Image(painter = painterResource(
                id = R.drawable.login_grid
            ),
            contentDescription = null,
            modifier = Modifier
                .size(
                    width = gridWidth,
                    height = gridHeight
                )
                .offset(x = gridX,
                    y = gridY
                ),
            contentScale = ContentScale.FillBounds,
            alpha = 0.32f
        )
    }
}
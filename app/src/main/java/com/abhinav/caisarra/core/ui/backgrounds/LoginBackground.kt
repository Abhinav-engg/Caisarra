package com.abhinav.caisarra.core.ui.backgrounds

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
import com.abhinav.caisarra.core.ui.components.ChessNotation

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

        val knightWidth = cardWidth * 0.46f
        val knightHeight = knightWidth * (207f / 110f)
        val minimumKnightY = statusBarHeight + 10.dp
        val calculatedKnightY = cardTop - knightHeight * 0.70f
        val knightY = maxOf(calculatedKnightY, minimumKnightY)
        val knightX = cardRight - knightWidth * 0.45f

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
            alpha = 0.85f
        )

        val pieceWidth = cardWidth * 0.44f
        val pieceHeight = pieceWidth * (296f / 126f)
        val pieceY = screenHeight - pieceHeight
        val pieceX = -pieceWidth * 0.18f

        Image(
            painter = painterResource(
                id = R.drawable.dark_chess_piece
            ),
            contentDescription = null,
            modifier = Modifier
                .size(
                    width = pieceWidth,
                    height = pieceHeight
                )
                .offset(
                    x = pieceX,
                    y = pieceY
                ),
            contentScale = ContentScale.FillBounds,
            alpha = 0.50f
        )
        val gridWidth = cardWidth * 1.15f
        val gridHeight = gridWidth * (189f / 328f)
        val gridX = (maxWidth - gridWidth) / 2f
        val gridY = cardBottom - gridHeight * 0.30f

        Image(
            painter = painterResource(
                id = R.drawable.decorative_grid
            ),
            contentDescription = null,
            modifier = Modifier
                .size(
                    width = gridWidth,
                    height = gridHeight
                )
                .offset(
                    x = gridX,
                    y = gridY
                ),
            contentScale = ContentScale.FillBounds,
            alpha = 0.60f
        )
    }
}
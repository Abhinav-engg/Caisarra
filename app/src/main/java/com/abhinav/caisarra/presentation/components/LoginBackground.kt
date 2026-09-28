package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.abhinav.caisarra.R

@Composable
fun LoginBackground(
    modifier: Modifier = Modifier,
    cardLeft: Dp,
    cardTop: Dp,
    cardRight: Dp,
    cardBottom: Dp,
    cardWidth: Dp,
    cardHeight: Dp
) {

    Box(modifier = modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.50f)
            )
    ) {
        ChessNotation(modifier = Modifier.fillMaxSize())

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
                .offset(
                    x = gridX,
                    y = gridY
                ),

            contentScale = ContentScale.FillBounds,
            alpha = 0.32f
        )
    }
}
package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.R

@Composable
fun ChallengeCardBackground(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                color = Color(0xFF101922)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF008FA4),
                shape = shape
            )
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.challenge_pieces
            ),
            contentDescription = null,
            modifier = Modifier
                .width(105.dp)
                .height(125.dp)
                .align(Alignment.BottomStart),
            contentScale = ContentScale.Fit,
            alpha = 0.42f
        )
    }
}
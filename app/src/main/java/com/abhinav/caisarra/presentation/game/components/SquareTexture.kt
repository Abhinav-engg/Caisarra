package com.abhinav.caisarra.presentation.game.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize

fun Modifier.squareTexture(image: ImageBitmap?, row: Int, column: Int, gridSize: Int): Modifier = image?.let {
    val slice = it.width / gridSize
    drawBehind {
        drawImage(it, srcOffset = IntOffset(column * slice, row * slice), srcSize = IntSize(slice, slice), dstSize = size.toIntSize())
    }
} ?: this

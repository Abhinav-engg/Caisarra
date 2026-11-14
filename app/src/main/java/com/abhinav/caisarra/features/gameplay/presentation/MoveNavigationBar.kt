package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.PuzzleCardBorder
import com.caisaara.ui.theme.PuzzleHeaderBg

@Composable
fun MoveNavigationBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PuzzleHeaderBg)
            .border(1.dp, PuzzleCardBorder, RectangleShape)
    ) {
        MoveNavigationItem(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back", canGoBack, onBack, Modifier.weight(1f))
        MoveNavigationItem(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Forward", canGoForward, onForward, Modifier.weight(1f))
    }
}

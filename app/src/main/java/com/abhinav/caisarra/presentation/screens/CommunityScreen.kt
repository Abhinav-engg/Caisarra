package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.TextSizeStat

@Composable
fun CommunityScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Community",
            color = Color.White,
            fontFamily = JetBrainsMono,
            fontSize = TextSizeStat,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

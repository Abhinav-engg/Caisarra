package com.abhinav.caisarra.presentation.game.components.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SetupHeader(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        SetupIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack, Modifier.align(Alignment.CenterStart))
        Text(title, color = SetupColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.viewmodel.ExperienceLevel
import com.abhinav.caisarra.presentation.viewmodel.RatingViewModel
import com.abhinav.caisarra.ui.theme.RatingCardHeight

private val ErrorRed = Color(0xFFFF3344)

@Composable
fun RatingScreen(
    viewModel: RatingViewModel,
    onRated: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    AuthCard(cardHeight = RatingCardHeight) {
        AuthHeader(
            "HOW MUCH WOULD YOU RATE YOURSELF",
            "You will be given a provisional rating based on your choice"
        )

        ExperienceLevel.entries.forEach { level ->
            GeneralButton(
                text = level.label,
                onClick = { viewModel.submit(level, onRated) },
                enabled = !state.isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = state.error.orEmpty(),
            color = ErrorRed,
            fontSize = 11.sp,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 28.dp)
        )
    }
}
package com.abhinav.caisarra.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.presentation.tournament.TournamentColors
import kotlinx.coroutines.isActive

@Composable
fun LinkResolverScreen(
    resolveRoute: suspend () -> String,
    onResolved: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        val route = resolveRoute()
        if (isActive) onResolved(route)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TournamentColors.Background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = TournamentColors.Accent)
    }
}

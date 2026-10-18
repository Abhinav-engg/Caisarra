package com.abhinav.caisarra.presentation.tournament

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object TournamentColors {
    val Background = Color(0xFF071017)
    val Card = Color(0xFF0E1B26)
    val Accent = Color(0xFF81B64C)
    val Danger = Color(0xFFE5645A)
    val Text = Color.White
    val Muted = Color(0xFF8FA3B3)
}

@Composable
fun tournamentFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TournamentColors.Text,
    unfocusedTextColor = TournamentColors.Text,
    focusedBorderColor = TournamentColors.Accent,
    unfocusedBorderColor = TournamentColors.Muted,
    focusedLabelColor = TournamentColors.Accent,
    unfocusedLabelColor = TournamentColors.Muted,
    cursorColor = TournamentColors.Accent
)

fun formatLabel(format: String): String = when (format) {
    "swiss" -> "Swiss"
    "round_robin" -> "Round Robin"
    "knockout" -> "Knockout"
    else -> format
}

fun statusLabel(status: String): String = when (status) {
    "registration" -> "Registration open"
    "ongoing" -> "In progress"
    "completed" -> "Completed"
    "cancelled" -> "Cancelled"
    else -> status
}

fun resultLabel(result: String?): String = when (result) {
    null, "" -> ""
    "white_win" -> "1-0"
    "black_win" -> "0-1"
    "draw" -> "½-½"
    else -> result
}

fun scoreLabel(score: Double): String =
    if (score % 1.0 == 0.0) score.toInt().toString() else score.toString()
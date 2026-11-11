package com.abhinav.caisarra.presentation.tournament

fun tournamentSpeedOf(timeControl: String): TournamentSpeed? {
    val parts = timeControl.trim().split("+")
    val minutes = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
    val increment = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    val estimatedSeconds = minutes * 60 + increment * 40
    return when {
        estimatedSeconds < 180 -> TournamentSpeed.Bullet
        estimatedSeconds < 600 -> TournamentSpeed.Blitz
        estimatedSeconds < 1800 -> TournamentSpeed.Rapid
        else -> TournamentSpeed.Classical
    }
}

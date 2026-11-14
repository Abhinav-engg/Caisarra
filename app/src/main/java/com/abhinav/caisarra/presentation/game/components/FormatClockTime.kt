package com.abhinav.caisarra.presentation.game.components

private const val DAY_MILLIS = 86_400_000L

fun formatClockTime(timeMillis: Long, isDailyMode: Boolean): String {
    if (isDailyMode || timeMillis >= DAY_MILLIS) return formatDailyTime(timeMillis)
    val totalSeconds = timeMillis.coerceAtLeast(0L) / 1000L
    return "%02d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

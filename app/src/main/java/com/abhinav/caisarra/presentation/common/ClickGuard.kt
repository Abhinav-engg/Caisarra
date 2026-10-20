package com.abhinav.caisarra.presentation.common

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

object ClickGuard {
    private const val INTERVAL_MS = 500L
    private var lastClickTime = 0L

    fun canClick(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime < INTERVAL_MS) return false
        lastClickTime = now
        return true
    }
}

fun singleClick(onClick: () -> Unit): () -> Unit = {
    if (ClickGuard.canClick()) onClick()
}

fun Modifier.singleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = clickable(enabled = enabled) {
    if (ClickGuard.canClick()) onClick()
}
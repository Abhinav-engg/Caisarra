package com.abhinav.caisarra.core.ui.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import java.util.WeakHashMap

@Composable
fun SecureScreen(enabled: Boolean = true) {
    if (!enabled) return
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.findHostActivity()?.window
        if (window != null) SecureWindowCounter.acquire(window)
        onDispose {
            if (window != null) SecureWindowCounter.release(window)
        }
    }
}

private object SecureWindowCounter {
    private val counts = WeakHashMap<Window, Int>()

    fun acquire(window: Window) {
        val next = (counts[window] ?: 0) + 1
        counts[window] = next
        if (next == 1) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun release(window: Window) {
        val next = (counts[window] ?: 0) - 1
        if (next <= 0) {
            counts.remove(window)
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            counts[window] = next
        }
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

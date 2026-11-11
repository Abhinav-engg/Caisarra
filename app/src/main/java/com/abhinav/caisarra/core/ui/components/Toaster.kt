package com.abhinav.caisarra.core.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Stable
class Toaster(context: Context) {
    private val appContext = context.applicationContext
    private var current: Toast? = null

    fun show(message: String?) {
        val text = message?.takeIf { it.isNotBlank() } ?: return
        current?.cancel()
        current = Toast.makeText(appContext, text, Toast.LENGTH_SHORT).also { it.show() }
    }

    fun cancel() {
        current?.cancel()
        current = null
    }
}

@Composable
fun rememberToaster(): Toaster {
    val context = LocalContext.current
    val toaster = remember(context) { Toaster(context) }
    DisposableEffect(toaster) {
        onDispose { toaster.cancel() }
    }
    return toaster
}

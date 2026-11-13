package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.abhinav.caisarra.features.mascot.domain.MascotReaction
import kotlinx.coroutines.delay

@Stable
class TransientMascotReaction {
    var current by mutableStateOf<MascotReaction?>(null)
        private set
    internal var stamp by mutableIntStateOf(0)
        private set

    fun show(reaction: MascotReaction) {
        current = reaction
        stamp++
    }

    internal fun clear() {
        current = null
    }
}

@Composable
fun rememberTransientMascotReaction(holdMillis: Long = 4500L): TransientMascotReaction {
    val holder = remember { TransientMascotReaction() }
    LaunchedEffect(holder.stamp) {
        if (holder.current != null) {
            delay(holdMillis)
            holder.clear()
        }
    }
    return holder
}

package com.abhinav.caisarra.domain.chess.game

import com.abhinav.caisarra.domain.chess.model.PieceColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameClock(
    private val scope: CoroutineScope,
    private val initialMs: Long,
    private val incrementMs: Long,
    private val onTimeout: (PieceColor) -> Unit
) {

    private val _times = MutableStateFlow(initialMs to initialMs)
    val times: StateFlow<Pair<Long, Long>> = _times.asStateFlow()

    private var active: PieceColor? = null
    private var job: Job? = null

    fun start(color: PieceColor) {
        active = color
        job?.cancel()
        job = scope.launch {
            var last = System.nanoTime()
            while (isActive) {
                delay(100)
                val now = System.nanoTime()
                tick((now - last) / 1_000_000)
                last = now
            }
        }
    }

    fun switchTo(next: PieceColor) {
        val mover = active
        if (mover != null && mover != next && incrementMs > 0) addTime(mover, incrementMs)
        active = next
    }

    fun revertTo(color: PieceColor) {
        active = color
    }

    fun stop() {
        job?.cancel()
        active = null
    }

    fun reset() {
        stop()
        _times.value = initialMs to initialMs
    }

    private fun addTime(color: PieceColor, amount: Long) {
        val (white, black) = _times.value
        _times.value = if (color == PieceColor.White) (white + amount) to black else white to (black + amount)
    }

    private fun tick(elapsed: Long) {
        val color = active ?: return
        val (white, black) = _times.value
        val remaining = ((if (color == PieceColor.White) white else black) - elapsed).coerceAtLeast(0)
        _times.value = if (color == PieceColor.White) remaining to black else white to remaining
        if (remaining == 0L) {
            stop()
            onTimeout(color)
        }
    }

    fun restore(whiteMs: Long, blackMs: Long) {
        _times.value = whiteMs to blackMs
    }
}
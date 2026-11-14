package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.pieceDragGestures(
    vararg keys: Any?,
    canStartDrag: (down: Offset) -> Boolean,
    onDragStart: (down: Offset) -> Unit,
    onDragMove: (position: Offset) -> Unit,
    onDragEnd: (position: Offset?) -> Unit
): Modifier = pointerInput(*keys) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (!canStartDrag(down.position)) return@awaitEachGesture
        var lastPosition = down.position
        val slopChange = awaitTouchSlopOrCancellation(down.id) { change, _ ->
            change.consume()
            lastPosition = change.position
        } ?: return@awaitEachGesture
        onDragStart(down.position)
        onDragMove(lastPosition)
        val completed = drag(slopChange.id) { change ->
            change.consume()
            lastPosition = change.position
            onDragMove(change.position)
        }
        onDragEnd(if (completed) lastPosition else null)
    }
}

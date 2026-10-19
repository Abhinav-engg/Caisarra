
package com.abhinav.caisarra.presentation.game.components

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.abhinav.caisarra.R
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.Square
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun ChessMoveSound(
    board: Map<Square, Piece>,
    moveEventId: Long?,
    moveTo: Square?
) {
    val context = LocalContext.current
    val previousBoard = remember {
        mutableStateOf(board)
    }
    val lastHandledMoveId = rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(board, moveEventId) {
        val id = moveEventId

        if (id != null && id != lastHandledMoveId.value) {
            val oldBoard = previousBoard.value
            val isCapture = moveTo != null && (oldBoard.containsKey(moveTo) || oldBoard.size > board.size)
            val soundResource = if (isCapture) {
                R.raw.capture
            } else {
                R.raw.move_self
            }
            lastHandledMoveId.value = id
            val player = MediaPlayer.create(
                context,
                soundResource
            )
            player?.apply {
                setOnCompletionListener {
                    it.release()
                }
                start()
            }
        }
        previousBoard.value = board
    }
}

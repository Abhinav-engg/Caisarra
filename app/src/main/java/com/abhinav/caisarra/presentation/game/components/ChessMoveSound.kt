package com.abhinav.caisarra.presentation.game.components

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.abhinav.caisarra.R
import com.abhinav.caisarra.data.local.SOUND_EFFECTS_KEY
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.flow.map

@Composable
fun ChessMoveSound(
    board: Map<Square, Piece>,
    moveEventId: Long?,
    moveTo: Square?
) {
    val context = LocalContext.current

    val soundEnabled by remember(context) {
        context.settingsDataStore.data.map {
            it[SOUND_EFFECTS_KEY] ?: true
        }
    }.collectAsState(initial = true)

    val previousBoard = remember {
        mutableStateOf(board)
    }

    val lastHandledMoveId = rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(board, moveEventId, soundEnabled) {
        val id = moveEventId

        if (id != null && id != lastHandledMoveId.value) {
            val oldBoard = previousBoard.value

            val isCapture = moveTo != null &&
                    (oldBoard.containsKey(moveTo) || oldBoard.size > board.size)

            lastHandledMoveId.value = id

            if (soundEnabled) {
                val soundResource = if (isCapture) {
                    R.raw.capture
                } else {
                    R.raw.move_self
                }

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
        }

        previousBoard.value = board
    }
}
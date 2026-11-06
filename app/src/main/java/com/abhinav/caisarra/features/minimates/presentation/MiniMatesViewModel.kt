package com.abhinav.caisarra.features.minimates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.features.minimates.domain.MiniMatesEngine
import com.abhinav.caisarra.features.minimates.domain.MiniMatesLevel
import com.abhinav.caisarra.features.minimates.domain.MiniMatesMove
import com.abhinav.caisarra.features.minimates.domain.miniMatesLevels
import com.abhinav.caisarra.features.minimates.domain.parseMiniMatesLayout
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MiniMatesViewModel : ViewModel() {
    private val _state = MutableStateFlow(MiniMatesUiState())
    val state = _state.asStateFlow()

    fun openLevel(level: MiniMatesLevel) {
        _state.value = MiniMatesUiState(level, parseMiniMatesLayout(level.layout), movesLeft = level.mateIn)
    }

    fun retry() = _state.value.level?.let(::openLevel)

    fun nextLevel() = _state.value.level?.let { miniMatesLevels.getOrNull(it.number) }?.let(::openLevel) ?: closeLevel()

    fun closeLevel() {
        _state.value = MiniMatesUiState()
    }

    fun onSquareClick(square: Square) {
        val current = _state.value
        val position = current.position ?: return
        if (current.status != MiniMatesStatus.PLAYING) return
        if (square in current.targets) return submit(MiniMatesMove(current.selected!!, square))
        val ownPiece = position.pieces[square]?.color == PieceColor.WHITE
        val targets = if (ownPiece) MiniMatesEngine.legalMoves(position, PieceColor.WHITE).filter { it.from == square }.map { it.to } else emptyList()
        _state.update { it.copy(selected = square.takeIf { ownPiece }, targets = targets) }
    }

    private fun submit(move: MiniMatesMove) {
        val current = _state.value
        val after = MiniMatesEngine.play(current.position!!, move)
        _state.update { it.copy(position = after, lastMove = move, selected = null, targets = emptyList(), status = MiniMatesStatus.THINKING) }
        viewModelScope.launch(Dispatchers.Default) {
            val status = when {
                MiniMatesEngine.isMate(after, PieceColor.BLACK) -> MiniMatesStatus.SOLVED
                !MiniMatesEngine.canForceMateAfter(after, current.movesLeft) -> MiniMatesStatus.FAILED
                else -> null
            }
            if (status != null) return@launch _state.update { it.copy(status = status) }
            val reply = MiniMatesEngine.bestDefense(after, current.movesLeft - 1) ?: return@launch
            delay(400)
            _state.update {
                it.copy(
                    position = MiniMatesEngine.play(after, reply),
                    lastMove = reply,
                    movesLeft = current.movesLeft - 1,
                    status = MiniMatesStatus.PLAYING
                )
            }
        }
    }
}

package com.abhinav.caisarra.features.minimates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.mapper.toDomain
import com.abhinav.caisarra.domain.chess.game.mapper.toUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUiBoard
import com.abhinav.caisarra.domain.chess.game.mapper.uciToSquare
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor
import com.abhinav.caisarra.domain.chess.model.PieceType
import com.abhinav.caisarra.domain.chess.model.toMove
import com.abhinav.caisarra.features.minimates.data.MiniMatesMoveRequest
import com.abhinav.caisarra.features.minimates.data.MiniMatesRepository
import com.abhinav.caisarra.presentation.game.model.MoveFeedback
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MiniMatesViewModel(private val repository: MiniMatesRepository) : ViewModel() {
    private val _state = MutableStateFlow(MiniMatesUiState())
    val state = _state.asStateFlow()
    private val engine = ChesslibEngine()
    private var startedAt = 0L
    private var eventId = 0L

    init {
        loadOverview()
    }

    fun loadOverview() = viewModelScope.launch {
        _state.value = MiniMatesUiState()
        repository.overview()
            .onSuccess { overview ->
                _state.update { it.copy(overview = overview) }
                overview.chapters.flatMap { it.levels }.firstOrNull { it.unlocked && !it.completed }?.let { openLevel(it.levelNumber) }
                    ?: _state.update { it.copy(loading = false) }
            }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.userMessage()) } }
    }

    fun openLevel(number: Int) = viewModelScope.launch {
        repository.level(number).onFailure { e -> _state.update { it.copy(loading = false, level = null, error = e.userMessage()) } }
            .onSuccess { level ->
                engine.loadFen(level.fen)
                _state.value = MiniMatesUiState(loading = false, overview = _state.value.overview, level = level)
                refresh()
                startedAt = System.currentTimeMillis()
            }
    }

    fun retry() = _state.value.level?.let { openLevel(it.levelNumber) } ?: loadOverview()

    fun nextLevel() = _state.value.nextLevel?.let(::openLevel) ?: showLevels(true)

    fun showLevels(show: Boolean) = _state.update { it.copy(showLevels = show) }

    fun onMoveAnimationStarted(id: Long) = _state.update { if (it.moveAnimation?.id == id) it.copy(moveAnimationConsumedId = id) else it }

    private fun refreshOverview() = viewModelScope.launch {
        repository.overview().onSuccess { overview -> _state.update { it.copy(overview = overview) } }
    }

    fun hint() = viewModelScope.launch {
        val current = _state.value
        val level = current.level ?: return@launch
        repository.hint(level.levelNumber, current.moveIndex)
            .onSuccess { hint ->
                _state.update { it.copy(note = hint.message) }
                hint.fromSquare?.let(::uciToSquare)?.let(::onSquareClick)
            }
            .onFailure { e -> _state.update { it.copy(note = e.userMessage()) } }
    }

    fun onSquareClick(square: Square) {
        val current = _state.value
        if (current.status != MiniMatesStatus.PLAYING) return
        val from = current.selected
        if (from != null && square in current.targets) return submit(from, square)
        val own = current.board[square]?.color == engine.turn().toUi()
        val targets = if (own) engine.legalMovesFrom(square.toDomain()).map { it.to.toUi() } else emptyList()
        _state.update { it.copy(selected = square.takeIf { own }, targets = targets) }
    }

    private fun submit(from: Square, to: Square) {
        val level = _state.value.level ?: return
        val promotion = PieceType.Queen.takeIf { engine.isPromotion(from.toDomain(), to.toDomain()) }
        val move = Move(from.toDomain(), to.toDomain(), promotion)
        engine.play(move)
        refresh(move, MiniMatesStatus.THINKING)
        viewModelScope.launch {
            val request = MiniMatesMoveRequest(move.uci, _state.value.moveIndex, System.currentTimeMillis() - startedAt)
            repository.move(level.levelNumber, request).onFailure { e ->
                engine.undo()
                refresh(status = MiniMatesStatus.PLAYING)
                _state.update { it.copy(note = e.userMessage()) }
            }.onSuccess { result ->
                val reply = result.nextMove?.takeIf { it.isNotBlank() }
                _state.update { it.copy(moveFeedback = MoveFeedback(++eventId, to, result.correct)) }
                when {
                    !result.correct -> _state.update { it.copy(status = MiniMatesStatus.FAILED) }
                    result.isCheckmate || reply == null -> {
                        _state.update { it.copy(status = MiniMatesStatus.SOLVED, stars = result.stars, nextLevel = result.nextUnlockedLevel) }
                        refreshOverview()
                    }
                    else -> {
                        delay(600)
                        engine.play(reply.toMove())
                        refresh(reply.toMove(), MiniMatesStatus.PLAYING)
                        _state.update { it.copy(moveIndex = it.moveIndex + 1) }
                    }
                }
            }
        }
    }

    private fun refresh(move: Move? = null, status: MiniMatesStatus? = null) {
        val check = engine.checkSquare() != null
        _state.update {
            it.copy(
                board = engine.pieces().toUiBoard(),
                selected = null,
                targets = emptyList(),
                note = null,
                lastFrom = move?.from?.toUi() ?: it.lastFrom,
                lastTo = move?.to?.toUi() ?: it.lastTo,
                moveAnimation = move?.let { m -> it.board[m.from.toUi()]?.let { p -> PieceMoveAnimationEvent(++eventId, p, m.from.toUi(), m.to.toUi()) } } ?: it.moveAnimation,
                whiteInCheck = check && engine.turn() == PieceColor.White,
                blackInCheck = check && engine.turn() == PieceColor.Black,
                status = status ?: it.status
            )
        }
    }
}

package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.AnalysisSummaryResponse
import com.abhinav.caisarra.data.remote.dto.MoveAnalysisDto
import com.abhinav.caisarra.data.repository.GameAnalysisRepository
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.mapper.toUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUiBoard
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainPieceColor
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

enum class AnalysisPhase { ANALYSING, READY, FAILED }

data class GameAnalysisUiState(
    val phase: AnalysisPhase = AnalysisPhase.ANALYSING,
    val summary: AnalysisSummaryResponse? = null,
    val moves: List<MoveAnalysisDto> = emptyList(),
    val selectedMove: Int? = null,
    val errorMessage: String? = null,
    val currentPly: Int = 0,
    val totalPlies: Int = 0,
    val board: Map<Square, Piece> = emptyMap(),
    val lastMoveFrom: Square? = null,
    val lastMoveTo: Square? = null,
    val isWhiteInCheck: Boolean = false,
    val isBlackInCheck: Boolean = false,
    val isWhiteTurn: Boolean = true,
    val isFlipped: Boolean = false,
    val isPlayingToImportantMove: Boolean = false,
    val activeTab: Int = 0 // 0 = Moves / Review, 1 = Summary / Report
)

class GameAnalysisViewModel(
    private val repository: GameAnalysisRepository,
    private val gameId: String
) : ViewModel() {

    private val _state = MutableStateFlow(GameAnalysisUiState())
    val state: StateFlow<GameAnalysisUiState> = _state.asStateFlow()

    private var started = false
    private var playJob: Job? = null

    fun start() {
        if (started) return
        started = true
        viewModelScope.launch {
            if (repository.summary(gameId).getOrNull() == null) {
                repository.requestAnalysis(gameId).onFailure {
                    fail(it.message)
                    return@launch
                }
            }
            poll()
        }
    }

    fun retry() {
        stopPlayJob()
        _state.value = GameAnalysisUiState()
        viewModelScope.launch {
            repository.retry(gameId).onFailure {
                fail(it.message)
                return@launch
            }
            poll()
        }
    }

    fun selectMove(moveNumber: Int) {
        jumpToPly(moveNumber)
    }

    fun firstMove() {
        jumpToPly(0)
    }

    fun lastMove() {
        jumpToPly(_state.value.moves.size)
    }

    fun previousMove() {
        val curr = _state.value.currentPly
        if (curr > 0) {
            jumpToPly(curr - 1)
        }
    }

    fun nextMove() {
        val curr = _state.value.currentPly
        if (curr < _state.value.moves.size) {
            jumpToPly(curr + 1)
        }
    }

    fun jumpToPly(ply: Int) {
        stopPlayJob()
        renderPosition(ply.coerceIn(0, _state.value.moves.size))
    }

    fun flipBoard() {
        _state.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun setActiveTab(tab: Int) {
        _state.update { it.copy(activeTab = tab) }
    }

    fun stopPlayJob() {
        if (playJob?.isActive == true) {
            playJob?.cancel()
            _state.update { it.copy(isPlayingToImportantMove = false) }
        }
    }

    fun playToNextImportantMove() {
        if (playJob?.isActive == true) {
            stopPlayJob()
            return
        }

        val moves = _state.value.moves
        if (moves.isEmpty()) return

        val currentPly = _state.value.currentPly
        var targetPly = moves.firstOrNull { it.moveNumber > currentPly && isImportantMove(it) }?.moveNumber

        if (targetPly == null) {
            // Wrap around: check from beginning
            targetPly = moves.firstOrNull { isImportantMove(it) }?.moveNumber
        }

        if (targetPly == null) {
            // Fallback to the final move if no move is classified as important
            targetPly = moves.size
        }

        if (targetPly == currentPly) return

        playJob = viewModelScope.launch {
            _state.update { it.copy(isPlayingToImportantMove = true) }
            try {
                if (targetPly < currentPly) {
                    renderPosition(0)
                    delay(300L)
                }
                val start = _state.value.currentPly
                for (p in (start + 1)..targetPly) {
                    renderPosition(p)
                    delay(450L)
                }
            } finally {
                _state.update { it.copy(isPlayingToImportantMove = false) }
            }
        }
    }

    fun isImportantMove(move: MoveAnalysisDto): Boolean {
        val c = move.classification.trim().lowercase()
        return c in IMPORTANT_CLASSIFICATIONS || move.centipawnLoss >= 75
    }

    private fun renderPosition(ply: Int) {
        val moves = _state.value.moves
        val safePly = ply.coerceIn(0, moves.size)
        val fen = if (safePly == 0) {
            STARTING_FEN
        } else {
            moves.getOrNull(safePly - 1)?.positionFen?.takeIf { it.isNotBlank() } ?: STARTING_FEN
        }

        try {
            val engine = ChesslibEngine()
            engine.loadFen(fen)
            val domainPieces = engine.pieces()
            val checkSquare = engine.checkSquare()
            val turn = engine.turn()

            val (lastFrom, lastTo) = if (safePly > 0) {
                val currMove = moves[safePly - 1]
                val prevFen = if (safePly == 1) {
                    STARTING_FEN
                } else {
                    moves.getOrNull(safePly - 2)?.positionFen?.takeIf { it.isNotBlank() } ?: STARTING_FEN
                }
                findMoveSquares(currMove.playedMove, prevFen, fen, currMove.playerColor)
            } else {
                null to null
            }

            _state.update {
                it.copy(
                    currentPly = safePly,
                    totalPlies = moves.size,
                    board = domainPieces.toUiBoard(),
                    lastMoveFrom = lastFrom,
                    lastMoveTo = lastTo,
                    isWhiteInCheck = checkSquare != null && turn == DomainPieceColor.White,
                    isBlackInCheck = checkSquare != null && turn == DomainPieceColor.Black,
                    isWhiteTurn = turn == DomainPieceColor.White,
                    selectedMove = if (safePly > 0) moves[safePly - 1].moveNumber else null
                )
            }
        } catch (_: Exception) {
            _state.update {
                it.copy(currentPly = safePly, selectedMove = if (safePly > 0) safePly else null)
            }
        }
    }

    private fun findMoveSquares(
        playedMove: String,
        prevFen: String,
        currFen: String,
        playerColor: String
    ): Pair<Square?, Square?> {
        val clean = playedMove.trim()
        if (clean.length in 4..5 &&
            clean[0] in 'a'..'h' && clean[1] in '1'..'8' &&
            clean[2] in 'a'..'h' && clean[3] in '1'..'8'
        ) {
            val from = Square(row = 8 - clean[1].digitToInt(), column = clean[0] - 'a')
            val to = Square(row = 8 - clean[3].digitToInt(), column = clean[2] - 'a')
            return from to to
        }

        return try {
            val isWhite = playerColor.equals("white", ignoreCase = true)
            val moverColor = if (isWhite) DomainPieceColor.White else DomainPieceColor.Black

            if (clean.contains("O-O-O", ignoreCase = true)) {
                val rank = if (isWhite) 8 - 1 else 8 - 8
                return Square(row = rank, column = 'e' - 'a') to Square(row = rank, column = 'c' - 'a')
            } else if (clean.contains("O-O", ignoreCase = true)) {
                val rank = if (isWhite) 8 - 1 else 8 - 8
                return Square(row = rank, column = 'e' - 'a') to Square(row = rank, column = 'g' - 'a')
            }

            val prevEngine = ChesslibEngine().apply { loadFen(prevFen) }
            val currEngine = ChesslibEngine().apply { loadFen(currFen) }
            val prevPieces = prevEngine.pieces()
            val currPieces = currEngine.pieces()

            val prevMap = prevPieces.associateBy { it.square }
            val currMap = currPieces.associateBy { it.square }

            var fromSquare: Square? = null
            var toSquare: Square? = null

            for (file in 0..7) {
                for (rank in 0..7) {
                    val dSquare = DomainSquare(file, rank)
                    val pBefore = prevMap[dSquare]
                    val pAfter = currMap[dSquare]
                    val uiSq = dSquare.toUi()

                    if (pBefore != null && pBefore.color == moverColor && pAfter != pBefore) {
                        if (fromSquare == null || pBefore.type != DomainPieceType.Rook) {
                            fromSquare = uiSq
                        }
                    }
                    if (pAfter != null && pAfter.color == moverColor && pAfter != pBefore) {
                        if (toSquare == null || pAfter.type != DomainPieceType.Rook) {
                            toSquare = uiSq
                        }
                    }
                }
            }
            fromSquare to toSquare
        } catch (_: Exception) {
            null to null
        }
    }

    private suspend fun poll() {
        var lastError: String? = null
        repeat(MAX_POLLS) {
            val result = repository.summary(gameId)
            val summary = result.getOrNull()
            lastError = result.exceptionOrNull()?.message
            when (summary?.analysis?.status) {
                "completed" -> {
                    val moves = repository.moves(gameId).getOrNull().orEmpty()
                    _state.value = GameAnalysisUiState(
                        phase = AnalysisPhase.READY,
                        summary = summary,
                        moves = moves,
                        totalPlies = moves.size
                    )
                    renderPosition(0)
                    return
                }
                "failed" -> {
                    fail("Analysis failed. Please try again.")
                    return
                }
            }
            delay(POLL_INTERVAL_MS)
        }
        fail(lastError ?: "Analysis is taking too long. Please try again.")
    }

    private fun fail(message: String?) {
        _state.value = GameAnalysisUiState(
            phase = AnalysisPhase.FAILED,
            errorMessage = message ?: "Something went wrong. Please try again."
        )
    }

    companion object {
        private const val MAX_POLLS = 60
        private const val POLL_INTERVAL_MS = 2000L

        val IMPORTANT_CLASSIFICATIONS = setOf(
            "brilliant",
            "great",
            "inaccuracy",
            "mistake",
            "miss",
            "blunder"
        )
    }
}

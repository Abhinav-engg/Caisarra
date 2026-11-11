package com.abhinav.caisarra.presentation.puzzles

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.data.remote.dto.CustomPuzzleResponse
import com.abhinav.caisarra.data.repository.PuzzleRepository
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainPieceColor
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor as UiPieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType as UiPieceType
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.Square as UiSquare
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomTacticsViewModel(
    private val repository: PuzzleRepository
) : ViewModel() {

    private val engine = ChesslibEngine()

    private val _state =
        MutableStateFlow<CustomTacticsUiState>(
            CustomTacticsUiState.Loading
        )

    val state = _state.asStateFlow()

    private var animationId = 0L
    private var puzzleStartTime = 0L
    private var cachedThemes = emptyList<com.abhinav.caisarra.data.remote.dto.PuzzleThemeDto>()

    private var cachedFilters = CustomTacticsFilters()
    private var activePuzzle: CustomPuzzleResponse? = null

    init {
        loadThemes()
    }

    fun loadThemes() {
        viewModelScope.launch {
            val previous = _state.value as? CustomTacticsUiState.Filters

            _state.value = CustomTacticsUiState.Filters(
                availableThemes = previous?.availableThemes.orEmpty(),
                filters = previous?.filters ?: CustomTacticsFilters(),
                isLoadingThemes = true
            )

            repository.getPuzzleThemes()
                .onSuccess { response ->
                    cachedThemes = response.themes

                    val current = _state.value as? CustomTacticsUiState.Filters

                    _state.value = CustomTacticsUiState.Filters(
                        availableThemes = response.themes,
                        filters = current?.filters ?: cachedFilters,
                        isLoadingThemes = false
                    )
                }
                .onFailure { error ->
                    val current = _state.value as? CustomTacticsUiState.Filters

                    _state.value = CustomTacticsUiState.Filters(
                        availableThemes = current?.availableThemes.orEmpty(),
                        filters = current?.filters ?: CustomTacticsFilters(),
                        isLoadingThemes = false,
                        error = error.userMessage("Unable to load puzzle themes.")
                    )
                }
        }
    }

    private fun updateFilters(
        transform: (CustomTacticsFilters) -> CustomTacticsFilters
    ) {
        val current = _state.value as? CustomTacticsUiState.Filters ?: return

        val updatedFilters = transform(current.filters)
        cachedFilters = updatedFilters

        _state.value = current.copy(
            filters = updatedFilters,
            error = null
        )
    }

    fun toggleTheme(themeId: String) {
        updateFilters { filters ->
            val updated = filters.selectedThemes.toMutableSet()

            if (themeId in updated) {
                updated.remove(themeId)
            } else {
                updated.add(themeId)
            }

            filters.copy(selectedThemes = updated)
        }
    }

    fun clearThemes() {
        updateFilters {
            it.copy(selectedThemes = emptySet())
        }
    }

    fun setRatingRange(
        minRating: Int,
        maxRating: Int
    ) {
        updateFilters {
            it.copy(
                minRating = minRating.coerceAtLeast(0),
                maxRating = maxRating.coerceAtLeast(minRating)
            )
        }
    }

    fun setExcludeSolved(enabled: Boolean) {
        updateFilters {
            it.copy(excludeSolved = enabled)
        }
    }

    fun setReviewMistakes(enabled: Boolean) {
        updateFilters {
            it.copy(reviewMistakes = enabled)
        }
    }

    fun loadCustomPuzzle() {
        val current = _state.value as? CustomTacticsUiState.Filters ?: return

        if (current.filters.minRating > current.filters.maxRating) {
            _state.value = current.copy(
                error = "Minimum rating cannot exceed maximum rating."
            )
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(
                isFetchingPuzzle = true,
                error = null
            )

            val selectedThemes = current.filters.selectedThemes.toList()

            repository.getCustomPuzzle(
                theme = selectedThemes.singleOrNull(),
                themes = if (selectedThemes.size > 1) selectedThemes else emptyList(),
                minRating = current.filters.minRating,
                maxRating = current.filters.maxRating,
                excludeSolved = current.filters.excludeSolved,
                reviewMistakes = current.filters.reviewMistakes
            ).onSuccess { puzzle ->
                setupPuzzle(puzzle)
            }.onFailure { error ->
                _state.value = CustomTacticsUiState.Filters(
                    availableThemes = current.availableThemes,
                    filters = current.filters,
                    isFetchingPuzzle = false,
                    error = error.userMessage("Unable to load a custom puzzle.")
                )
            }
        }
    }

    fun returnToFilters() {
        val current = _state.value

        val filtersState = when (current) {
            is CustomTacticsUiState.Filters -> current
            else -> null
        }

        if (filtersState != null) {
            _state.value = filtersState.copy(
                isFetchingPuzzle = false
            )
        } else {
            loadThemes()
        }
    }
    private fun setupPuzzle(puzzle: CustomPuzzleResponse) {
        try {
            activePuzzle = puzzle
            engine.loadFen(puzzle.fen)

            val fenTokens = puzzle.fen.trim().split(" ")
            val fenSide = fenTokens.getOrNull(1) ?: "w"
            val playerColor =
                if (fenSide == "w") UiPieceColor.BLACK
                else UiPieceColor.WHITE

            val isFlipped = playerColor == UiPieceColor.BLACK

            val initialMove = parseUciMove(puzzle.initialMove)

            val movingPiece = engine.pieces()
                .find { it.square == initialMove.from }
                ?.toUiPiece()

            val played = engine.play(initialMove)

            if (!played) {
                _state.value = CustomTacticsUiState.Error(
                    "The puzzle contains an invalid initial move."
                )
                return
            }

            val checkSquare = engine.checkSquare()

            val animation = movingPiece?.let {
                PieceMoveAnimationEvent(
                    id = ++animationId,
                    from = initialMove.from.toUiSquare(),
                    to = initialMove.to.toUiSquare(),
                    piece = it
                )
            }

            puzzleStartTime = SystemClock.elapsedRealtime()

            _state.value = CustomTacticsUiState.Playing(
                puzzle = puzzle,
                board = currentBoardMap(),
                playerColor = playerColor,
                isBoardFlipped = isFlipped,
                lastMoveFrom = initialMove.from.toUiSquare(),
                lastMoveTo = initialMove.to.toUiSquare(),
                isWhiteInCheck = checkSquare != null &&
                        engine.turn() == DomainPieceColor.White,
                isBlackInCheck = checkSquare != null &&
                        engine.turn() == DomainPieceColor.Black,
                moveAnimationEvent = animation
            )
        } catch (e: Exception) {
            _state.value = CustomTacticsUiState.Error(
                e.userMessage("Unable to prepare the puzzle board.")
            )
        }
    }

    fun onMoveAnimationStarted(eventId: Long) {
        val current = _state.value

        if (
            current is CustomTacticsUiState.Playing &&
            current.moveAnimationEvent?.id == eventId
        ) {
            _state.value = current.copy(
                moveAnimationConsumedId = eventId
            )
        }
    }

    fun onSquareClicked(square: UiSquare) {
        val current = _state.value

        if (
            current !is CustomTacticsUiState.Playing ||
            current.isAwaitingServer
        ) return

        val selected = current.selectedSquare

        if (selected == null) {
            selectSquare(current, square)
            return
        }

        if (selected == square) {
            _state.value = current.copy(
                selectedSquare = null,
                legalMoves = emptyList()
            )
            return
        }

        val clickedPiece = current.board[square]

        if (clickedPiece?.color == current.playerColor) {
            selectSquare(current, square)
            return
        }

        if (square !in current.legalMoves) {
            _state.value = current.copy(
                selectedSquare = null,
                legalMoves = emptyList()
            )
            return
        }

        val from = selected.toDomainSquare()
        val to = square.toDomainSquare()

        if (engine.isPromotion(from, to)) {
            _state.value = current.copy(
                promotionPending = selected to square
            )
        } else {
            executePlayerMove(selected, square, null)
        }
    }

    private fun selectSquare(
        current: CustomTacticsUiState.Playing,
        square: UiSquare
    ) {
        val piece = current.board[square]

        if (piece == null || piece.color != current.playerColor) {
            return
        }

        val legalTargets = engine.legalMovesFrom(
            square.toDomainSquare()
        ).map {
            it.to.toUiSquare()
        }

        _state.value = current.copy(
            selectedSquare = square,
            legalMoves = legalTargets,
            message = null
        )
    }

    fun onPromotionPieceSelected(pieceType: UiPieceType) {
        val current = _state.value as? CustomTacticsUiState.Playing ?: return
        val pending = current.promotionPending ?: return

        val promotion = when (pieceType) {
            UiPieceType.QUEEN -> DomainPieceType.Queen
            UiPieceType.ROOK -> DomainPieceType.Rook
            UiPieceType.BISHOP -> DomainPieceType.Bishop
            UiPieceType.KNIGHT -> DomainPieceType.Knight
            else -> DomainPieceType.Queen
        }

        _state.value = current.copy(
            promotionPending = null
        )

        executePlayerMove(
            from = pending.first,
            to = pending.second,
            promotion = promotion
        )
    }

    fun onDismissPromotion() {
        val current = _state.value as? CustomTacticsUiState.Playing ?: return

        _state.value = current.copy(
            promotionPending = null,
            selectedSquare = null,
            legalMoves = emptyList()
        )
    }

    private fun executePlayerMove(
        from: UiSquare,
        to: UiSquare,
        promotion: DomainPieceType?
    ) {
        val current = _state.value as? CustomTacticsUiState.Playing ?: return

        val move = Move(
            from = from.toDomainSquare(),
            to = to.toDomainSquare(),
            promotion = promotion
        )

        val movingPiece = current.board[from] ?: return

        val played = engine.play(move)

        if (!played) return

        val elapsed = (
                SystemClock.elapsedRealtime() - puzzleStartTime
                ).coerceAtLeast(0L)

        val checkSquare = engine.checkSquare()

        val animation = PieceMoveAnimationEvent(
            id = ++animationId,
            from = from,
            to = to,
            piece = movingPiece
        )

        _state.value = current.copy(
            board = currentBoardMap(),
            selectedSquare = null,
            legalMoves = emptyList(),
            lastMoveFrom = from,
            lastMoveTo = to,
            isWhiteInCheck = checkSquare != null &&
                    engine.turn() == DomainPieceColor.White,
            isBlackInCheck = checkSquare != null &&
                    engine.turn() == DomainPieceColor.Black,
            moveAnimationEvent = animation,
            isAwaitingServer = true,
            elapsedTimeMs = elapsed,
            message = "Checking your move..."
        )

        viewModelScope.launch {
            repository.submitCustomPuzzleAttempt(
                puzzleId = current.puzzle.puzzleId,
                move = move.toUci(),
                timeTakenMs = elapsed,
                rated = false
            ).onSuccess { response ->

                val latest = _state.value
                        as? CustomTacticsUiState.Playing ?: return@onSuccess

                if (response.correct) {
                    _state.value = CustomTacticsUiState.Result(
                        puzzle = current.puzzle,
                        board = currentBoardMap(),
                        isBoardFlipped = current.isBoardFlipped,
                        correct = true,
                        solved = response.solved,
                        attemptedMoves = response.attemptedMoves,
                        solutionMoves = response.solutionMoves,
                        ratingChange = response.ratingChange,
                        userRatingBefore = response.userRatingBefore,
                        userRatingAfter = response.userRatingAfter,
                        rated = response.rated
                    )
                } else {
                    engine.loadFen(current.puzzle.fen)
                    engine.play(parseUciMove(current.puzzle.initialMove))

                    _state.value = latest.copy(
                        board = currentBoardMap(),
                        selectedSquare = null,
                        legalMoves = emptyList(),
                        isAwaitingServer = false,
                        message = "Incorrect move. Try another move."
                    )
                }

            }.onFailure { error ->

                val latest = _state.value
                        as? CustomTacticsUiState.Playing ?: return@onFailure
                engine.loadFen(current.puzzle.fen)
                engine.play(parseUciMove(current.puzzle.initialMove))

                _state.value = latest.copy(
                    board = currentBoardMap(),
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    isAwaitingServer = false,
                    message = error.userMessage("Unable to submit your move.")
                )
            }
        }
    }

    fun showHint() {
        val current = _state.value as? CustomTacticsUiState.Playing ?: return
        if (current.isAwaitingServer) return

        val candidate = engine.pieces()
            .asSequence()
            .filter { it.color == if (current.playerColor == UiPieceColor.WHITE) {
                DomainPieceColor.White
            } else {
                DomainPieceColor.Black
            } }
            .map { piece ->
                piece.square to engine.legalMovesFrom(piece.square)
            }
            .firstOrNull { it.second.isNotEmpty() }

        if (candidate == null) {
            _state.value = current.copy(message = "No legal moves available.")
            return
        }

        _state.value = current.copy(
            selectedSquare = candidate.first.toUiSquare(),
            legalMoves = candidate.second.map { it.to.toUiSquare() },
            message = "Hint: try moving the highlighted piece."
        )
    }

    fun skipPuzzle() {
        val current = _state.value as? CustomTacticsUiState.Playing ?: return
        if (current.isAwaitingServer) return

        _state.value = CustomTacticsUiState.Filters(
            availableThemes = cachedThemes,
            filters = cachedFilters,
            isFetchingPuzzle = true
        )
        loadCustomPuzzle()
    }

    fun tryAnotherPuzzle() {
        val current = _state.value

        val filters = when (current) {
            is CustomTacticsUiState.Filters -> current
            else -> null
        }

        if (filters != null) {
            _state.value = filters.copy(isFetchingPuzzle = false)
            loadCustomPuzzle()
        } else {
            loadThemes()
        }
    }

    fun onBackToFilters() {
        _state.value = CustomTacticsUiState.Filters(
            availableThemes = cachedThemes,
            filters = cachedFilters
        )
    }

    private fun currentBoardMap(): Map<UiSquare, Piece> {
        return engine.pieces().associate { domainPiece ->
            domainPiece.square.toUiSquare() to domainPiece.toUiPiece()
        }
    }

    private fun parseUciMove(uci: String): Move {
        require(uci.length >= 4) {
            "Invalid UCI move: $uci"
        }

        val fromFile = uci[0] - 'a'
        val fromRank = uci[1] - '1'
        val toFile = uci[2] - 'a'
        val toRank = uci[3] - '1'

        require(
            fromFile in 0..7 &&
                    fromRank in 0..7 &&
                    toFile in 0..7 &&
                    toRank in 0..7
        ) {
            "Invalid UCI square: $uci"
        }

        val promotion = if (uci.length >= 5) {
            when (uci[4].lowercaseChar()) {
                'q' -> DomainPieceType.Queen
                'r' -> DomainPieceType.Rook
                'b' -> DomainPieceType.Bishop
                'n' -> DomainPieceType.Knight
                else -> null
            }
        } else null

        return Move(
            from = DomainSquare(fromFile, fromRank),
            to = DomainSquare(toFile, toRank),
            promotion = promotion
        )
    }

    private fun Move.toUci(): String {
        val promotion = when (this.promotion) {
            DomainPieceType.Queen -> "q"
            DomainPieceType.Rook -> "r"
            DomainPieceType.Bishop -> "b"
            DomainPieceType.Knight -> "n"
            else -> ""
        }

        return "${'a' + from.file}${from.rank + 1}" +
                "${'a' + to.file}${to.rank + 1}$promotion"
    }

    private fun DomainSquare.toUiSquare(): UiSquare {
        return UiSquare(
            row = 7 - rank,
            column = file
        )
    }

    private fun UiSquare.toDomainSquare(): DomainSquare {
        return DomainSquare(
            file = column,
            rank = 7 - row
        )
    }

    private fun com.abhinav.caisarra.domain.chess.model.BoardPiece.toUiPiece(): Piece {
        return Piece(
            type = when (type) {
                DomainPieceType.King -> UiPieceType.KING
                DomainPieceType.Queen -> UiPieceType.QUEEN
                DomainPieceType.Rook -> UiPieceType.ROOK
                DomainPieceType.Bishop -> UiPieceType.BISHOP
                DomainPieceType.Knight -> UiPieceType.KNIGHT
                DomainPieceType.Pawn -> UiPieceType.PAWN
            },
            color = when (color) {
                DomainPieceColor.White -> UiPieceColor.WHITE
                DomainPieceColor.Black -> UiPieceColor.BLACK
            }
        )
    }
}


package com.abhinav.caisarra.presentation.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.abhinav.caisarra.core.ui.components.AnimatedTickMark
import com.abhinav.caisarra.data.local.BOARD_COORDINATES_KEY
import com.abhinav.caisarra.data.local.BOARD_THEME_KEY
import com.abhinav.caisarra.data.local.HAPTICS_KEY
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.presentation.game.model.BoardTheme
import com.abhinav.caisarra.presentation.game.model.BoardThemeId
import com.abhinav.caisarra.presentation.game.model.BoardThemes
import com.abhinav.caisarra.presentation.game.model.MoveFeedback
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.PieceTheme
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val MIN_MOVE_DURATION = 120
private const val MAX_MOVE_DURATION = 320
private const val MOVE_MILLIS_PER_SQUARE = 45f
private const val PENDING_DROP_TIMEOUT_MS = 5_000L

@Composable
fun ChessBoard(
    board: Map<Square, Piece>,
    selectedSquare: Square?,
    legalMoves: List<Square>,
    lastMoveFrom: Square?,
    lastMoveTo: Square?,
    isWhiteInCheck: Boolean,
    isBlackInCheck: Boolean,
    isFlipped: Boolean,
    boardSize: Int = 8,
    columns: Int = boardSize,
    origin: Square = Square(0, 0),
    boardTheme: BoardTheme? = null,
    pieceTheme: PieceTheme? = null,
    moveAnimationEvent: PieceMoveAnimationEvent? = null,
    moveAnimationConsumedId: Long? = null,
    onMoveAnimationStarted: (Long) -> Unit = {},
    moveFeedback: MoveFeedback? = null,
    onSquareClick: (Square) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val hapticsEnabled by remember(context) {
        context.settingsDataStore.data.map { it[HAPTICS_KEY] ?: true }
    }.collectAsState(initial = true)
    val coordinatesEnabled by remember(context) {
        context.settingsDataStore.data.map {
            it[BOARD_COORDINATES_KEY] ?: true
        }
    }.collectAsState(initial = true)
    val currentThemeKey by remember(context) {
        context.settingsDataStore.data.map {
            it[BOARD_THEME_KEY] ?: BoardThemeId.MIDNIGHT_CYAN.storageKey
        }
    }.collectAsState(initial = BoardThemeId.MIDNIGHT_CYAN.storageKey)
    val theme = boardTheme ?: remember(currentThemeKey) {
        BoardThemes.getByStorageKey(currentThemeKey)
    }

    var runningTransition by remember {
        mutableStateOf<MoveTransition?>(null)
    }
    var handledMoveId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var completedMoveId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    val movementProgress = remember(moveAnimationEvent?.id) {
        Animatable(0f)
    }

    var dragFrom by remember { mutableStateOf<Square?>(null) }
    var dragPiece by remember { mutableStateOf<Piece?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var pendingDrop by remember { mutableStateOf<PendingDrop?>(null) }

    val currentBoard by rememberUpdatedState(board)
    val currentSelected by rememberUpdatedState(selectedSquare)
    val currentLegalMoves by rememberUpdatedState(legalMoves)
    val currentOnSquareClick by rememberUpdatedState(onSquareClick)

    val boardBeforeMove = remember { HashMap(board) }
    val isStandardBoard = boardSize == 8 && columns == 8
    val eventTransition = remember(moveAnimationEvent?.id) {
        moveAnimationEvent?.let { event ->
            val drop = pendingDrop
            moveTransitionFor(
                event = event,
                boardBefore = HashMap(boardBeforeMove),
                boardAfter = board,
                isStandardBoard = isStandardBoard,
                primaryAlreadyPlaced = drop != null && drop.from == event.from && drop.to == event.to
            )
        }
    }
    SideEffect {
        boardBeforeMove.clear()
        boardBeforeMove.putAll(board)
    }

    LaunchedEffect(moveAnimationEvent?.id) {
        runningTransition = null
        val event = moveAnimationEvent ?: return@LaunchedEffect
        val transition = eventTransition ?: return@LaunchedEffect
        if (
            moveAnimationConsumedId == event.id ||
            handledMoveId == event.id ||
            completedMoveId == event.id
        ) {
            return@LaunchedEffect
        }

        handledMoveId = event.id
        onMoveAnimationStarted(event.id)

        if (transition.isEmpty) {
            completedMoveId = event.id
            return@LaunchedEffect
        }

        runningTransition = transition
        movementProgress.snapTo(0f)

        val lead = transition.flights.first()
        val fileDistance = abs(lead.to.column - lead.from.column)
        val rankDistance = abs(lead.to.row - lead.from.row)
        val distance = sqrt(
            (fileDistance * fileDistance + rankDistance * rankDistance).toFloat()
        )

        val duration = (distance * MOVE_MILLIS_PER_SQUARE)
            .roundToInt()
            .coerceIn(MIN_MOVE_DURATION, MAX_MOVE_DURATION)

        movementProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = duration,
                easing = LinearEasing
            )
        )

        if (runningTransition === transition) {
            runningTransition = null
            completedMoveId = event.id
            val primaryFlew = transition.flights.any { it.from == event.from && it.to == event.to }
            if (hapticsEnabled && primaryFlew) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    LaunchedEffect(pendingDrop, board) {
        val drop = pendingDrop ?: return@LaunchedEffect
        if (board != drop.boardAtDrop) {
            pendingDrop = null
            return@LaunchedEffect
        }
        val isPromotion = drop.piece.type == PieceType.PAWN && (drop.to.row == 0 || drop.to.row == boardSize - 1)
        if (isPromotion) return@LaunchedEffect
        delay(PENDING_DROP_TIMEOUT_MS)
        pendingDrop = null
    }

    val shakeOffset = remember { Animatable(0f) }
    val feedbackAlpha = remember { Animatable(0f) }
    LaunchedEffect(moveFeedback?.id) {
        val feedback = moveFeedback ?: return@LaunchedEffect
        if (hapticsEnabled) {
            haptic.performHapticFeedback(if (feedback.isCorrect) HapticFeedbackType.Confirm else HapticFeedbackType.Reject)
        }
        feedbackAlpha.snapTo(0.6f)
        launch { feedbackAlpha.animateTo(0f, tween(500)) }
        if (!feedback.isCorrect) {
            shakeOffset.animateTo(0f, spring(dampingRatio = 0.2f, stiffness = 1500f), initialVelocity = 3000f)
        }
    }

    val activeTransition = runningTransition ?: eventTransition?.takeIf {
        moveAnimationEvent != null &&
                moveAnimationEvent.id != moveAnimationConsumedId &&
                moveAnimationEvent.id != handledMoveId &&
                moveAnimationEvent.id != completedMoveId
    }
    val draggedPiece = dragPiece?.takeIf { dragFrom != null && dragFrom == selectedSquare }
    val hiddenSquares = buildSet {
        activeTransition?.flights?.forEach { add(it.to) }
        pendingDrop?.let {
            add(it.from)
            add(it.to)
        }
        if (draggedPiece != null) dragFrom?.let { add(it) }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .clipToBounds()
    ) {
        val density = LocalDensity.current

        val cellWidth = maxWidth / columns
        val cellHeight = maxHeight / boardSize

        val cellWidthPx = with(density) { cellWidth.toPx() }
        val cellHeightPx = with(density) { cellHeight.toPx() }

        fun squareAt(position: Offset): Square? {
            if (position.x < 0f || position.y < 0f) return null
            val displayColumn = (position.x / cellWidthPx).toInt()
            val displayRow = (position.y / cellHeightPx).toInt()
            if (displayColumn !in 0 until columns || displayRow !in 0 until boardSize) return null
            val actualRow = if (isFlipped) boardSize - 1 - displayRow else displayRow
            val actualColumn = if (isFlipped) columns - 1 - displayColumn else displayColumn
            return Square(row = origin.row + actualRow, column = origin.column + actualColumn)
        }

        fun squareOffset(square: Square): Offset {
            val row = (square.row - origin.row).let { if (isFlipped) boardSize - 1 - it else it }
            val column = (square.column - origin.column).let { if (isFlipped) columns - 1 - it else it }
            return Offset(column * cellWidthPx, row * cellHeightPx)
        }

        val hoverSquare by remember(cellWidthPx, cellHeightPx, isFlipped, origin, boardSize, columns) {
            derivedStateOf { if (dragFrom == null) null else squareAt(dragPosition) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .pieceDragGestures(
                    cellWidthPx, cellHeightPx, isFlipped, origin, boardSize, columns,
                    canStartDrag = { down ->
                        val square = squareAt(down)
                        square != null && currentBoard[square] != null && square !in currentLegalMoves
                    },
                    onDragStart = { down ->
                        val square = squareAt(down)
                        val piece = square?.let { currentBoard[it] }
                        if (square != null && piece != null) {
                            if (currentSelected != square || currentLegalMoves.isEmpty()) {
                                currentOnSquareClick(square)
                            }
                            dragFrom = square
                            dragPiece = piece
                        }
                    },
                    onDragMove = { position ->
                        dragPosition = position
                    },
                    onDragEnd = { position ->
                        val from = dragFrom
                        val piece = dragPiece
                        dragFrom = null
                        dragPiece = null
                        val target = position?.let { squareAt(it) }
                        if (
                            from != null &&
                            piece != null &&
                            target != null &&
                            target != from &&
                            currentSelected == from &&
                            target in currentLegalMoves
                        ) {
                            pendingDrop = PendingDrop(piece, from, target, currentBoard)
                            if (hapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            currentOnSquareClick(target)
                        }
                    }
                )
        ) {
            val lightTexture = theme.lightSquareTexture?.let { ImageBitmap.imageResource(it) }
            val darkTexture = theme.darkSquareTexture?.let { ImageBitmap.imageResource(it) }
            repeat(boardSize) { displayRow ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(columns) { displayColumn ->
                        val actualRow = if (isFlipped) boardSize - 1 - displayRow else displayRow
                        val actualColumn = if (isFlipped) columns - 1 - displayColumn else displayColumn
                        val square = Square(
                            row = origin.row + actualRow,
                            column = origin.column + actualColumn
                        )
                        val piece = board[square]
                        val isDark = (actualRow + actualColumn) % 2 == 1
                        val isSelected = selectedSquare == square
                        val isLegalMove = square in legalMoves
                        val isDropTarget = isLegalMove && hoverSquare == square
                        val isLastMove =
                            lastMoveFrom == square || lastMoveTo == square

                        val isCheck = piece != null && piece.type == PieceType.KING && ((piece.color == PieceColor.WHITE && isWhiteInCheck) || (piece.color == PieceColor.BLACK && isBlackInCheck))
                        val squareColor = if (isDark) theme.darkSquare else theme.lightSquare

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(squareColor)
                                .squareTexture(if (isDark) darkTexture else lightTexture, displayRow, displayColumn, maxOf(boardSize, columns))
                                .then((if (isDark) theme.darkSquareGradient else theme.lightSquareGradient).takeIf { it.size > 1 }?.let { Modifier.background(Brush.horizontalGradient(it)) } ?: Modifier)
                                .squareInnerShadows(theme.squareInnerShadows)
                                .then(
                                    if (isLastMove) Modifier.background(theme.lastMoveColor) else Modifier
                                )
                                .then(
                                    if (moveFeedback?.square == square) Modifier.background((if (moveFeedback?.isCorrect == true) StatsGreen else StatsRed).copy(alpha = feedbackAlpha.value)) else Modifier
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = theme.squareBorder
                                )
                                .clickable {
                                    onSquareClick(square)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCheck) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    theme.checkColor.copy(alpha = 0.85f),
                                                    theme.checkColor.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            if (isSelected) {
                                AnimatedSelectionCircle(
                                    modifier = Modifier.fillMaxSize(),
                                    color = theme.selectedColor
                                )
                            }

                            if (piece != null && square !in hiddenSquares) {
                                PieceView(
                                    piece = piece,
                                    pieceTheme = pieceTheme
                                )
                            }
                            if (moveFeedback?.isCorrect == true && moveFeedback.square == square) {
                                AnimatedTickMark(key = moveFeedback.id, color = StatsGreen, modifier = Modifier.align(Alignment.TopEnd).fillMaxSize(0.38f))
                            }
                            if (coordinatesEnabled &&displayColumn == 0) {
                                Text(
                                    text = (8 - square.row).toString(),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 3.dp, top = 1.dp),
                                    color = if (isDark) {
                                        theme.lightSquare.copy(alpha = 0.78f)
                                    } else {
                                        theme.darkSquare.copy(alpha = 0.55f)
                                    },
                                    fontSize = 10.sp,
                                    lineHeight = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (coordinatesEnabled &&displayRow == boardSize - 1) {
                                Text(
                                    text = ('a' + square.column).toString(),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 3.dp, bottom = 1.dp),
                                    color = if (isDark) {
                                        theme.lightSquare.copy(alpha = 0.78f)
                                    } else {
                                        theme.darkSquare.copy(alpha = 0.55f)
                                    },
                                    fontSize = 10.sp,
                                    lineHeight = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isDropTarget) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 3.dp,
                                            color = theme.legalMoveRing
                                        )
                                )
                            } else if (isLegalMove) {
                                if (piece == null) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(
                                                color = theme.legalMoveDot,
                                                shape = CircleShape
                                            )
                                            .then(
                                                if (theme.showLegalMoveDotBorder) {
                                                    Modifier.border(
                                                        width = 1.5.dp,
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        shape = CircleShape
                                                    )
                                                } else Modifier
                                            )
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.82f)
                                            .background(
                                                color = theme.legalMoveOverlay,
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 3.dp,
                                                color = theme.legalMoveRing,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        activeTransition?.let { transition ->
            MoveTransitionOverlay(
                transition = transition,
                progress = { movementProgress.value },
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                squareOffset = { squareOffset(it) },
                pieceTheme = pieceTheme
            )
        }

        pendingDrop?.let { drop ->
            val dropOffset = squareOffset(drop.to)
            Box(
                modifier = Modifier
                    .offset { IntOffset(dropOffset.x.roundToInt(), dropOffset.y.roundToInt()) }
                    .size(width = cellWidth, height = cellHeight),
                contentAlignment = Alignment.Center
            ) {
                PieceView(piece = drop.piece, pieceTheme = pieceTheme)
            }
        }

        draggedPiece?.let { piece ->
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .offset {
                        IntOffset(
                            x = (dragPosition.x - cellWidthPx / 2f).roundToInt(),
                            y = (dragPosition.y - cellHeightPx * 0.75f).roundToInt()
                        )
                    }
                    .size(width = cellWidth, height = cellHeight)
                    .graphicsLayer {
                        scaleX = 1.2f
                        scaleY = 1.2f
                    },
                contentAlignment = Alignment.Center
            ) {
                PieceView(piece = piece, pieceTheme = pieceTheme)
            }
        }
    }
}

@Composable
fun AnimatedSelectionCircle(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF10B981)
) {
    val progress = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 180,
                easing = LinearEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        val halfWidth = size.width / 2f
        val halfHeight = size.height / 2f
        val animatedHalfWidth = halfWidth * progress.value
        val animatedHalfHeight = halfHeight * progress.value

        drawRect(
            color = color,
            topLeft = Offset(
                x = halfWidth - animatedHalfWidth,
                y = halfHeight - animatedHalfHeight
            ),
            size = androidx.compose.ui.geometry.Size(
                width = animatedHalfWidth * 2f,
                height = animatedHalfHeight * 2f
            )
        )
    }
}

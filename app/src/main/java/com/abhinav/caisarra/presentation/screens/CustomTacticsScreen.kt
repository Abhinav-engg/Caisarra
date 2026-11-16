package com.abhinav.caisarra.presentation.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Tune
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.features.gameplay.presentation.GameActionButton
import com.abhinav.caisarra.features.mascot.domain.PuzzleMascotLines
import com.abhinav.caisarra.features.mascot.presentation.CustomTacticsMascot
import com.abhinav.caisarra.features.mascot.presentation.MascotSays
import com.abhinav.caisarra.features.puzzles.presentation.PuzzleInfoRow
import com.abhinav.caisarra.features.puzzles.presentation.PuzzleLayout
import com.abhinav.caisarra.features.puzzles.presentation.PuzzleThemeChips
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.model.PieceColor
import androidx.compose.ui.res.stringResource
import com.abhinav.caisarra.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.puzzles.CustomTacticsUiState
import com.abhinav.caisarra.presentation.puzzles.CustomTacticsViewModel
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsSkeletonChartBg
import com.caisaara.ui.theme.ThemeSlateAccent
import com.caisaara.ui.theme.White


@Composable
fun CustomTacticsScreen(
    viewModel: CustomTacticsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {

        CustomTacticsUiState.Loading -> {
            LoadingContent()
        }

        is CustomTacticsUiState.Error -> {
            ErrorContent(
                message = current.message,
                onRetry = viewModel::loadThemes,
                onBack = onBack
            )
        }

        is CustomTacticsUiState.Filters -> {
            CustomTacticsFiltersContent(
                state = current,
                viewModel = viewModel,
                onBack = onBack
            )
        }

        is CustomTacticsUiState.Playing -> {
            CustomTacticsPlayingContent(
                state = current,
                viewModel = viewModel,
                onBack = onBack
            )
        }

        is CustomTacticsUiState.Result -> {
            CustomTacticsResultContent(
                state = current,
                onNextPuzzle = viewModel::onBackToFilters,
                onBack = onBack
            )
        }
    }
}


@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = StatsCyan)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = White,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        ActionButton(
            text = "Retry",
            onClick = onRetry
        )

        Spacer(Modifier.height(8.dp))

        ActionButton(
            text = "Go Back",
            onClick = onBack
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomTacticsFiltersContent(
    state: CustomTacticsUiState.Filters,
    viewModel: CustomTacticsViewModel,
    onBack: () -> Unit
) {
    var minRatingText by remember(state.filters.minRating) {
        mutableStateOf(state.filters.minRating.toString())
    }

    var maxRatingText by remember(state.filters.maxRating) {
        mutableStateOf(state.filters.maxRating.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
            .imePadding()
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    color = White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Text(
                text = stringResource(R.string.custom_tactics),
                color = White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {

            Text(
                text = stringResource(R.string.puzzle_rating),
                color = StatsCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = minRatingText,
                    onValueChange = { value ->
                        minRatingText = value
                            .filter { it.isDigit() }
                            .take(5)
                    },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(stringResource(R.string.minimum))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = ThemeSlateAccent,
                        focusedLabelColor = White,
                        unfocusedLabelColor = ThemeSlateAccent,
                        cursorColor = White
                    )
                )
                OutlinedTextField(
                    value =maxRatingText,
                    onValueChange = { value ->
                        maxRatingText = value
                            .filter { it.isDigit() }
                            .take(5)
                    },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(stringResource(R.string.maximum))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = ThemeSlateAccent,
                        focusedLabelColor = White,
                        unfocusedLabelColor = ThemeSlateAccent,
                        cursorColor = White
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.selected_range, state.filters.minRating, state.filters.maxRating),
                color = ThemeSlateAccent,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingRow(
            title = "Review mistakes",
            subtitle = "Practice puzzles you previously failed.",
            checked = state.filters.reviewMistakes,
            onCheckedChange = viewModel::setReviewMistakes,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = stringResource(R.string.tactical_themes),
                color = StatsCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.clear_selection),
                color = StatsCyan,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(
                        StatsSkeletonChartBg,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        state.filters.selectedThemes.forEach {
                            viewModel.toggleTheme(it)
                        }
                    }
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
            )
        }

        Spacer(Modifier.height(10.dp))


        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            when {

                state.isLoadingThemes -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = StatsCyan
                        )
                    }
                }

                state.availableThemes.isEmpty() -> {

                    Text(
                        text = state.error ?: "No themes available.",
                        color = ThemeSlateAccent,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    ActionButton(
                        text = "Reload Themes",
                        onClick = viewModel::loadThemes
                    )
                }

                else -> {

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        state.availableThemes.forEach { theme ->

                            val selected =
                                theme.id in state.filters.selectedThemes

                            FilterChip(
                                selected = selected,
                                onClick = {
                                    viewModel.toggleTheme(theme.id)
                                },
                                label = {
                                    Text(
                                        text = theme.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatsCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = StatsBg,
                                    labelColor = ThemeSlateAccent
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = ThemeSlateAccent.copy(
                                        alpha = 0.7f
                                    ),
                                    selectedBorderColor = StatsCyan
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = if (
                            state.filters.selectedThemes.isEmpty()
                        ) {
                            "All themes are included"
                        } else {
                            "${state.filters.selectedThemes.size} theme(s) selected"
                        },
                        color = ThemeSlateAccent,
                        fontSize = 12.sp
                    )

                    Text(
                        text = stringResource(R.string.tap_a_selected_theme_again_to),
                        color = ThemeSlateAccent,
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StatsBg)
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
        ) {

            state.error?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF7777),
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(8.dp))
            }

            ActionButton(
                text = if (state.isFetchingPuzzle) {
                    "Loading Puzzle..."
                } else {
                    "Start Training"
                },
                enabled = !state.isFetchingPuzzle &&
                        !state.isLoadingThemes &&
                        state.availableThemes.isNotEmpty(),
                onClick = {

                    val min = minRatingText.toIntOrNull()
                    val max = maxRatingText.toIntOrNull()

                    if (
                        min == null ||
                        max == null ||
                        min > max
                    ) {
                        return@ActionButton
                    }

                    viewModel.setRatingRange(min, max)
                    viewModel.loadCustomPuzzle()
                }
            )
        }
    }
}

@Composable
private fun CustomTacticsPlayingContent(
    state: CustomTacticsUiState.Playing,
    viewModel: CustomTacticsViewModel,
    onBack: () -> Unit
) {
    ChessMoveSound(
        board = state.board,
        moveEventId = state.moveAnimationEvent?.id,
        moveTo = state.moveAnimationEvent?.to
    )

    PuzzleLayout(
        header = {
            CaisaaraTopBar(
                title = stringResource(R.string.custom_tactics_2),
                onBack = onBack
            )
            CustomTacticsMascot(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                mascotSize = 88.dp
            )
        },
        board = {
            ChessBoard(
                board = state.board,
                selectedSquare = state.selectedSquare,
                legalMoves = state.legalMoves,
                lastMoveFrom = state.lastMoveFrom,
                lastMoveTo = state.lastMoveTo,
                isWhiteInCheck = state.isWhiteInCheck,
                isBlackInCheck = state.isBlackInCheck,
                isFlipped = state.isBoardFlipped,
                moveAnimationEvent = state.moveAnimationEvent,
                moveAnimationConsumedId = state.moveAnimationConsumedId,
                onMoveAnimationStarted = viewModel::onMoveAnimationStarted,
                moveFeedback = state.moveFeedback,
                onSquareClick = viewModel::onSquareClicked
            )
        },
        panel = {
            CustomTacticsPanel(state = state, viewModel = viewModel)
        }
    )

    state.promotionPending?.let {
        PromotionDialog(onPieceSelected = viewModel::onPromotionPieceSelected)
    }
}

@Composable
private fun CustomTacticsPanel(
    state: CustomTacticsUiState.Playing,
    viewModel: CustomTacticsViewModel
) {
    val busy = state.isAwaitingServer

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PuzzleInfoRow(
            title = "Puzzle #${state.puzzle.puzzleId}",
            subtitle = if (busy) {
                "Checking move…"
            } else {
                "${if (state.playerColor == PieceColor.WHITE) "White" else "Black"} to move · ${state.puzzle.rating}"
            }
        )

        if (state.puzzle.themes.isNotEmpty()) {
            PuzzleThemeChips(state.puzzle.themes)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameActionButton(
                icon = Icons.Outlined.Lightbulb,
                label = "Hint",
                onClick = viewModel::showHint,
                modifier = Modifier.weight(1f),
                enabled = !busy,
                selected = state.hintCount > 0 && state.selectedSquare != null
            )
            GameActionButton(
                icon = Icons.Outlined.SkipNext,
                label = "Skip",
                onClick = viewModel::skipPuzzle,
                modifier = Modifier.weight(1f),
                enabled = !busy
            )
            GameActionButton(
                icon = Icons.Outlined.Tune,
                label = "Filters",
                onClick = viewModel::onBackToFilters,
                modifier = Modifier.weight(1f),
                enabled = !busy
            )
        }
    }
}

@Composable
private fun CustomTacticsResultContent(
    state: CustomTacticsUiState.Result,
    onNextPuzzle: () -> Unit,
    onBack: () -> Unit
) {
    val seed = state.puzzle.hashCode()
    val reaction = remember(state) {
        if (state.correct) {
            PuzzleMascotLines.solved(0, state.ratingChange, seed).copy(
                detail = if (state.ratingChange > 0) {
                    "+${state.ratingChange} rating. Ready for another?"
                } else {
                    "Ready for another one?"
                }
            )
        } else {
            PuzzleMascotLines.failed(seed)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val boardSize = minOf(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .size(boardSize)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            ) {
                ChessBoard(
                    board = state.board,
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    lastMoveFrom = null,
                    lastMoveTo = null,
                    isWhiteInCheck = false,
                    isBlackInCheck = false,
                    isFlipped = state.isBoardFlipped,
                    onSquareClick = {}
                )
            }
        }

        MascotSays(
            reaction = reaction,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            mascotSize = 64.dp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF0F172A),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (state.solved) {
                    "Puzzle Solved!"
                } else if (state.correct) {
                    "Correct Move!"
                } else {
                    "Incorrect Move"
                },
                color = if (state.correct) StatsCyan else Color(0xFFFF7777),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.puzzle_rating_2, state.puzzle.rating),
                color = White,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.your_rating, state.userRatingBefore, state.userRatingAfter),
                color = ThemeSlateAccent,
                fontSize = 13.sp
            )

            Text(
                text = stringResource(R.string.rating_change, if (state.ratingChange > 0) "+" else "", state.ratingChange),
                color = StatsCyan,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.your_moves, state.attemptedMoves.joinToString(", ")),
                color = ThemeSlateAccent,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.solution_2, state.solutionMoves.joinToString(", ")),
                color = ThemeSlateAccent,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            ActionButton(text = "Next Puzzle", onClick = onNextPuzzle)

            Spacer(Modifier.height(10.dp))

            ActionButton(text = "Back", onClick = onBack)
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                StatsSkeletonChartBg,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = ThemeSlateAccent,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StatsCyan,
            contentColor = Color.Black,
            disabledContainerColor = StatsCyan.copy(alpha = 0.35f)
        )
    ) {

        if (text == "Loading Puzzle...") {

            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )

        } else {

            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

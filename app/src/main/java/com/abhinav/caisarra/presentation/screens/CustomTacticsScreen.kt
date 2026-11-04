package com.abhinav.caisarra.presentation.screens

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
                text = "Custom Tactics",
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
                text = "PUZZLE RATING",
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
                        Text("Minimum")
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
                        Text("Maximum")
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
                text = "Selected range: ${state.filters.minRating} – ${state.filters.maxRating}",
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
                text = "TACTICAL THEMES",
                color = StatsCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Clear selection",
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
                                        text = "${theme.name} (${theme.count})",
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
                        text = "Tap a selected theme again to remove it.",
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BoxWithConstraints(
                    modifier = Modifier.weight(1.15f).padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val boardSize = minOf(
                        maxWidth,
                        (configuration.screenHeightDp.dp - 150.dp).coerceAtLeast(160.dp)
                    )
                    Box(Modifier.size(boardSize).aspectRatio(1f)) {
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
                            onSquareClick = viewModel::onSquareClicked
                        )
                    }
                }
                PuzzleInfoPanel(state = state, modifier = Modifier.weight(0.85f))
            }
        } else {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PuzzleInfoPanel(state = state, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val boardSize = maxWidth

                    Box(
                        modifier = Modifier
                            .size(boardSize)
                            .aspectRatio(1f)
                    ) {
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
                            onSquareClick = { square ->
                                viewModel.onSquareClicked(square)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        CustomTacticsBottomControls(
            enabled = !state.isAwaitingServer,
            onBack = viewModel::onBackToFilters,
            onHint = viewModel::showHint
        )
    }

    state.promotionPending?.let {
        PromotionDialog(onPieceSelected = viewModel::onPromotionPieceSelected)
    }
}

@Composable
private fun PuzzleInfoPanel(
    state: CustomTacticsUiState.Playing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(StatsSkeletonChartBg, RoundedCornerShape(18.dp))
            .border(1.dp, StatsCyan.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "CUSTOM TACTICS",
            color = StatsCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Puzzle rating: ${state.puzzle.rating}",
            color = White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = state.puzzle.themes.joinToString(" • "),
            color = ThemeSlateAccent,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = state.message ?: "Find the best move!",
            color = if (state.message?.startsWith("Incorrect") == true) {
                Color(0xFFFF7777)
            } else White,
            fontSize = 14.sp
        )
        if (state.isAwaitingServer) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator(color = StatsCyan, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun CustomTacticsBottomControls(
    enabled: Boolean,
    onBack: () -> Unit,
    onHint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StatsBg)
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomPuzzleControl(
            icon = "‹",
            label = "Back",
            enabled = enabled,
            onClick = onBack,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onHint,
            enabled = enabled,
            modifier = Modifier.weight(1.00f).height(40.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StatsSkeletonChartBg,
                contentColor = White,
                disabledContainerColor = StatsSkeletonChartBg.copy(alpha = 0.45f)
            )
        ) {
            Text("💡  Hint", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BottomPuzzleControl(
    icon: String,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            color = if (enabled) White else ThemeSlateAccent,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            color = if (enabled) White else ThemeSlateAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
private fun CustomTacticsResultContent(
    state: CustomTacticsUiState.Result,
    onNextPuzzle: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = if (state.solved) {
                "Puzzle Solved!"
            } else if (state.correct) {
                "Correct Move!"
            } else {
                "Incorrect Move"
            },
            color = if (state.correct) {
                StatsCyan
            } else {
                Color(0xFFFF7777)
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Puzzle rating: ${state.puzzle.rating}",
            color = White,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Your rating: ${state.userRatingBefore} → ${state.userRatingAfter}",
            color = ThemeSlateAccent,
            fontSize = 14.sp
        )

        Text(
            text = "Rating change: ${
                if (state.ratingChange > 0) "+" else ""
            }${state.ratingChange}",
            color = StatsCyan,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Your moves: ${
                state.attemptedMoves.joinToString(", ")
            }",
            color = ThemeSlateAccent,
            fontSize = 12.sp
        )

        Text(
            text = "Solution: ${
                state.solutionMoves.joinToString(", ")
            }",
            color = ThemeSlateAccent,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(24.dp))

        ActionButton(
            text = "Next Puzzle",
            onClick = onNextPuzzle
        )

        Spacer(Modifier.height(10.dp))

        ActionButton(
            text = "Back",
            onClick = onBack
        )
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

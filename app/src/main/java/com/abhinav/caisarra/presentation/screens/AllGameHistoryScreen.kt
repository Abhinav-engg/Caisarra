
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.presentation.game.components.GameHistorySection
import com.abhinav.caisarra.presentation.screens.components.SkeletonBox
import com.abhinav.caisarra.presentation.screens.components.rememberShimmerBrush
import com.abhinav.caisarra.presentation.viewmodel.HomeViewModel
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
private val HistoryBackground = Color(0xFF080D13)
private val HistoryWhite = Color(0xFFF5F5F5)
private val HistoryCardBackground = Color(0xFF151F2B)
private val HistoryCardBorder = Color(0xFF304154)

@Composable
fun AllGameHistoryScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onReviewGame: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(HistoryBackground)
    ) {
        CaisaaraTopBar(
            title = "GAME HISTORY",
            onBack = onBack,
            backgroundColor = HistoryBackground,
            titleColor = HistoryWhite,
            iconTint = HistoryWhite,
            titleFontSize = 18.sp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                )
        ) {


            Spacer(Modifier.height(20.dp))

            when {
                state.isLoadingHistory -> {
                    GameHistorySkeleton()
                }

                state.error != null && state.gameHistory.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(HistoryCardBackground)
                            .border(
                                1.dp,
                                HistoryCardBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error
                                ?: "Unable to load game history.",
                            color = HistoryWhite,
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp
                        )

                        TextButton(
                            onClick = { viewModel.refresh() }
                        ) {
                            Text(
                                text = "RETRY",
                                color = Color(0xFF10B981),
                                fontFamily = JetBrainsMono
                            )
                        }
                    }
                }

                else -> {
                    GameHistorySection(
                        games = state.gameHistory,
                        onReviewGame = onReviewGame,
                        modifier = Modifier.fillMaxWidth(),
                        showViewAll = false,
                        isLoading = false
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GameHistorySkeleton() {
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBox(
                modifier = Modifier.size(27.dp),
                shape = CircleShape,
                brush = shimmerBrush
            )

            SkeletonBox(
                modifier = Modifier
                    .width(150.dp)
                    .height(15.dp),
                brush = shimmerBrush
            )
        }

        repeat(5) {
            GameHistoryCardSkeleton(shimmerBrush)
        }
    }
}

@Composable
private fun GameHistoryCardSkeleton(
    brush: Brush
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HistoryCardBackground)
            .border(
                1.dp,
                HistoryCardBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            SkeletonBox(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                brush = brush
            )

            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(13.dp),
                brush = brush
            )

            SkeletonBox(
                modifier = Modifier
                    .width(22.dp)
                    .height(10.dp),
                brush = brush
            )

            SkeletonBox(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                brush = brush
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(105.dp)
                    .height(12.dp),
                brush = brush
            )

            SkeletonBox(
                modifier = Modifier
                    .width(65.dp)
                    .height(20.dp),
                shape = RoundedCornerShape(8.dp),
                brush = brush
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(125.dp)
                    .height(10.dp),
                brush = brush
            )

            SkeletonBox(
                modifier = Modifier
                    .width(72.dp)
                    .height(28.dp),
                shape = RoundedCornerShape(8.dp),
                brush = brush
            )
        }
    }
}

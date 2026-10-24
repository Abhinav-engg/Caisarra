package com.abhinav.caisarra.presentation.screens.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsCardItemSpacing
import com.abhinav.caisarra.core.ui.theme.StatsCardPadding
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsShimmerBase
import com.caisaara.ui.theme.StatsShimmerHighlight
import com.caisaara.ui.theme.StatsSkeletonChartBg

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = listOf(
            StatsShimmerBase,
            StatsShimmerHighlight,
            StatsShimmerBase
        ),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 400f, translateAnim + 400f)
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
    brush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun StatsSkeletonScreen(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(StatsScreenPadding),
        verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
    ) {
        RatingProgressionCardSkeleton(brush)
        ResultsCardSkeleton(brush)
        KeyMetricsGridSkeleton(brush)
        ColorPerformanceCardSkeleton(brush)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RatingProgressionCardSkeleton(brush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(110.dp)
                        .height(14.dp),
                    brush = brush
                )
                SkeletonBox(
                    modifier = Modifier
                        .width(88.dp)
                        .height(22.dp),
                    shape = RoundedCornerShape(6.dp),
                    brush = brush
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(130.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    brush = brush
                )
                SkeletonBox(
                    modifier = Modifier
                        .width(36.dp)
                        .height(18.dp)
                        .padding(bottom = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    brush = brush
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(42.dp)
                            .height(26.dp),
                        shape = RoundedCornerShape(8.dp),
                        brush = brush
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StatsSkeletonChartBg)
                    .border(1.dp, StatsCardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(StatsCardPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SkeletonBox(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(10.dp),
                                brush = brush
                            )
                            SkeletonBox(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp),
                                brush = brush
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(105.dp)
                        .height(14.dp),
                    brush = brush
                )
                SkeletonBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp),
                    brush = brush
                )
            }
        }
    }
}

@Composable
private fun ResultsCardSkeleton(brush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(18.dp),
                    brush = brush
                )
                SkeletonBox(
                    modifier = Modifier
                        .width(95.dp)
                        .height(14.dp),
                    brush = brush
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(48.dp)
                                .height(26.dp),
                            shape = RoundedCornerShape(6.dp),
                            brush = brush
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width(70.dp)
                                .height(12.dp),
                            brush = brush
                        )
                    }
                }
            }

            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                shape = RoundedCornerShape(4.dp),
                brush = brush
            )

            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(13.dp),
                brush = brush
            )
        }
    }
}

@Composable
private fun KeyMetricsGridSkeleton(brush: Brush) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(StatsCardBg)
                            .border(1.dp, StatsCardBorder, RoundedCornerShape(14.dp))
                            .padding(StatsCardPadding)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SkeletonBox(
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(24.dp),
                                brush = brush
                            )
                            SkeletonBox(
                                modifier = Modifier
                                    .width(85.dp)
                                    .height(12.dp),
                                brush = brush
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPerformanceCardSkeleton(brush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(150.dp)
                    .height(16.dp),
                brush = brush
            )

            repeat(2) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SkeletonBox(
                                modifier = Modifier.size(12.dp),
                                shape = CircleShape,
                                brush = brush
                            )
                            SkeletonBox(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(13.dp),
                                brush = brush
                            )
                        }
                        SkeletonBox(
                            modifier = Modifier
                                .width(120.dp)
                                .height(12.dp),
                            brush = brush
                        )
                    }
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        brush = brush
                    )
                }
            }
        }
    }
}

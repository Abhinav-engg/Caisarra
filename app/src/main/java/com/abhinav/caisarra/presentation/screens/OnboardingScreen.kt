package com.abhinav.caisarra.presentation.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.theme.OnboardingButtonCornerRadius
import com.abhinav.caisarra.core.ui.theme.OnboardingButtonHeight
import com.abhinav.caisarra.core.ui.theme.OnboardingDotSize
import com.abhinav.caisarra.core.ui.theme.OnboardingDotSpacing
import com.abhinav.caisarra.core.ui.theme.OnboardingHorizontalPadding
import com.abhinav.caisarra.core.ui.theme.OnboardingTextSpacing
import com.abhinav.caisarra.core.ui.theme.OnboardingTopPadding
import com.abhinav.caisarra.presentation.common.singleClick
import com.caisaara.ui.theme.CaisaaraFontFamily
import kotlinx.coroutines.delay

private data class OnboardingPageData(
    val eyebrow: String,
    val title: String,
    val description: String,
    val isBolderDesc: Boolean = false
)

private val OnboardingPages = listOf(
    OnboardingPageData(
        eyebrow = "01 / THE FIRST MOVE",
        title = "Unleash Your Strategy",
        description = "Find an opponent in seconds and play live, anytime."
    ),
    OnboardingPageData(
        eyebrow = "02 / TRAIN & IMPROVE",
        title = "Custom Board Themes",
        description = "Solve puzzles and review your games with engine-powered insights.",
        isBolderDesc = true
    ),
    OnboardingPageData(
        eyebrow = "03 / YOUR NEXT LEVEL",
        title = "Compete & Rise",
        description = "Challenge players worldwide or invite friends to live matches."
    )
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        (context as? Activity)?.finish()
    }

    var currentPage by remember { mutableIntStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(currentPage) {
        if (currentPage > 0) {
            isAnimating = true
            val duration = if (currentPage == 1) 700L else 900L
            delay(duration)
            isAnimating = false
        }
    }

    val easeInOut = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)
    val transitionDuration = if (currentPage == 2) 900 else 700

    val knightRotation by animateFloatAsState(
        targetValue = when (currentPage) {
            0 -> 0f
            1 -> 55f
            else -> 0f
        },
        animationSpec = tween(durationMillis = transitionDuration, easing = easeInOut),
        label = "knightRotation"
    )

    val knightScale by animateFloatAsState(
        targetValue = when (currentPage) {
            0 -> 1f
            1 -> 0.95f
            else -> 1.15f
        },
        animationSpec = tween(durationMillis = transitionDuration, easing = easeInOut),
        label = "knightScale"
    )

    val heroKnightAlpha by animateFloatAsState(
        targetValue = if (currentPage == 2) 0f else 1f,
        animationSpec = tween(durationMillis = transitionDuration, easing = easeInOut),
        label = "heroKnightAlpha"
    )

    val crownedKnightAlpha by animateFloatAsState(
        targetValue = if (currentPage == 2) 1f else 0f,
        animationSpec = tween(durationMillis = transitionDuration, easing = easeInOut),
        label = "crownedKnightAlpha"
    )

    val headerAlpha by animateFloatAsState(
        targetValue = if (currentPage == 2) 0f else 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )

    val topTextAlpha by animateFloatAsState(
        targetValue = if (currentPage == 2) 1f else 0f,
        animationSpec = tween(durationMillis = 450, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "topTextAlpha"
    )

    val topTextOffsetY by animateDpAsState(
        targetValue = if (currentPage == 2) 0.dp else 48.dp,
        animationSpec = tween(durationMillis = 500, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "topTextOffsetY"
    )

    val bottomTextAlpha by animateFloatAsState(
        targetValue = if (currentPage == 2) 0f else 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "bottomTextAlpha"
    )

    val bottomTextOffsetY by animateDpAsState(
        targetValue = if (currentPage == 2) (-48).dp else 0.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "bottomTextOffsetY"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val totalHeight = maxHeight
        val totalWidth = maxWidth

        val knightOffsetY: Dp by animateDpAsState(
            targetValue = when (currentPage) {
                0 -> totalHeight * 0.16f
                1 -> totalHeight * 0.12f
                else -> totalHeight * 0.38f
            },
            animationSpec = tween(durationMillis = transitionDuration, easing = easeInOut),
            label = "knightOffsetY"
        )

        val knightOffsetX: Dp by animateDpAsState(
            targetValue = when (currentPage) {
                0 -> 0.dp
                1 -> totalWidth * 0.18f
                else -> 0.dp
            },
            animationSpec = tween(durationMillis = transitionDuration, easing = easeInOut),
            label = "knightOffsetX"
        )

        if (headerAlpha > 0f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = OnboardingHorizontalPadding,
                        vertical = OnboardingTopPadding
                    )
                    .alpha(headerAlpha),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MOSAIC CHESS",
                    fontFamily = CaisaaraFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "Skip",
                    fontFamily = CaisaaraFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = singleClick {
                            onFinishOnboarding()
                        }
                    )
                )
            }
        }

        if (topTextAlpha > 0f) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = OnboardingHorizontalPadding,
                        vertical = OnboardingTopPadding
                    )
                    .offset(y = topTextOffsetY)
                    .alpha(topTextAlpha)
            ) {
                Text(
                    text = OnboardingPages[2].eyebrow,
                    fontFamily = CaisaaraFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.height(OnboardingTextSpacing))
                Text(
                    text = OnboardingPages[2].title,
                    fontFamily = CaisaaraFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp,
                    color = Color(0xFFF8FAFC)
                )
                Spacer(modifier = Modifier.height(OnboardingTextSpacing))
                Text(
                    text = OnboardingPages[2].description,
                    fontFamily = CaisaaraFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = knightOffsetX, y = knightOffsetY)
                .scale(knightScale)
                .rotate(knightRotation),
            contentAlignment = Alignment.Center
        ) {
            if (heroKnightAlpha > 0f) {
                Image(
                    painter = painterResource(id = R.drawable.vector_knight_hero),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 177.dp, height = 225.dp)
                        .alpha(heroKnightAlpha)
                )
            }
            if (crownedKnightAlpha > 0f) {
                Image(
                    painter = painterResource(id = R.drawable.vector_knight_crowned),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 161.dp, height = 246.dp)
                        .alpha(crownedKnightAlpha)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            if (bottomTextAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = OnboardingHorizontalPadding)
                        .offset(y = bottomTextOffsetY)
                        .alpha(bottomTextAlpha)
                ) {
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(350, delayMillis = 150)) +
                                    slideInVertically(animationSpec = tween(350, delayMillis = 150)) { it / 3 })
                                .togetherWith(
                                    fadeOut(animationSpec = tween(250)) +
                                            slideOutVertically(animationSpec = tween(250)) { -it / 3 }
                                )
                        },
                        label = "BottomTextAnimation"
                    ) { targetPage ->
                        val pageData = OnboardingPages.getOrElse(targetPage) { OnboardingPages[0] }
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = pageData.eyebrow,
                                fontFamily = CaisaaraFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.height(OnboardingTextSpacing))
                            Text(
                                text = pageData.title,
                                fontFamily = CaisaaraFontFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 38.sp,
                                color = Color(0xFFF8FAFC)
                            )
                            Spacer(modifier = Modifier.height(OnboardingTextSpacing))
                            Text(
                                text = pageData.description,
                                fontFamily = CaisaaraFontFamily,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = if (pageData.isBolderDesc) FontWeight.Medium else FontWeight.Normal,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isActive = index == currentPage
                    val dotColor by animateColorAsState(
                        targetValue = if (isActive) Color(0xFF00E5FF) else Color(0xFF475569),
                        animationSpec = tween(300),
                        label = "dotColor"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = OnboardingDotSpacing / 2)
                            .size(OnboardingDotSize)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OnboardingHorizontalPadding)
            ) {
                OnboardingNextButton(
                    enabled = !isAnimating,
                    onClick = singleClick {
                        if (!isAnimating) {
                            if (currentPage < 2) {
                                currentPage++
                            } else {
                                onFinishOnboarding()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingNextButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonAlpha = if (enabled) 1f else 0.45f
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1D828C),
            Color(0xFF0EB4C4),
            Color(0xFF01E3F9)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(OnboardingButtonHeight)
            .alpha(buttonAlpha)
            .clip(RoundedCornerShape(OnboardingButtonCornerRadius))
            .background(gradientBrush)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Next",
            fontFamily = CaisaaraFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color(0xFF0F172A),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(20.dp)
        )
    }
}

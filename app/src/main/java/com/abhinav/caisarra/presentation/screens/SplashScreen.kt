package com.abhinav.caisarra.presentation.screens

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.theme.SplashProgressHeight
import com.caisaara.ui.theme.CaisaaraFontFamily
import com.caisaara.ui.theme.CaisaaraTypography
import com.caisaara.ui.theme.SplashBackground
import com.caisaara.ui.theme.SplashCyan
import com.caisaara.ui.theme.SplashProgressLabel
import com.caisaara.ui.theme.SplashTrack
import com.caisaara.ui.theme.SplashWordmarkDim
import java.util.Random
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

private class Shard(
    val path: Path,
    val color: Color,
    val targetAlpha: Float,
    val center: Offset,
    val from: Offset,
    val spin: Float,
    val delay: Float
)

@Composable
fun SplashScreen(
    loadDestination: suspend () -> String,
    onSplashFinished: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val destDeferred = async { loadDestination() }
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1600,
                easing = LinearEasing
            )
        )
        delay(600)
        val targetDestination = destDeferred.await()
        onSplashFinished(targetDestination)
    }

    val vector = ImageVector.vectorResource(R.drawable.mosaic_logo)
    val shards = remember(vector) { buildShards(vector) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val logoWidth = screenWidth * 0.48f
        val logoHeight = logoWidth * (vector.viewportHeight / vector.viewportWidth)

        MosaicLogo(
            vector = vector,
            shards = shards,
            progress = { animatable.value },
            modifier = Modifier
                .size(logoWidth, logoHeight)
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.36f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.53f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MOSAIC",
                style = CaisaaraTypography.H1.copy(
                    fontFamily = CaisaaraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = 1.sp,
                    color = SplashWordmarkDim
                )
            )
            Text(
                text = "MOSAIC",
                style = CaisaaraTypography.H1.copy(
                    fontFamily = CaisaaraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = 1.sp,
                    color = SplashCyan
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = FastOutSlowInEasing.transform(animatable.value)
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.785f)
                .padding(horizontal = screenWidth * 0.07f)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ASSEMBLING YOUR NEXT MOVE",
                style = CaisaaraTypography.MicroBold.copy(
                    fontFamily = CaisaaraFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    color = SplashProgressLabel
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            SplashProgressBar(
                progress = { animatable.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SplashProgressHeight)
            )
        }
    }
}

@Composable
private fun MosaicLogo(
    vector: ImageVector,
    shards: List<Shard>,
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val unit = size.width / vector.viewportWidth
        scale(unit, pivot = Offset.Zero) {
            val currentProgress = progress()
            shards.forEach { shard ->
                val local = if (shard.delay >= 1f) 0f else {
                    ((currentProgress - shard.delay) / (1f - shard.delay)).coerceIn(0f, 1f)
                }
                val eased = FastOutSlowInEasing.transform(local)
                withTransform({
                    translate(
                        left = shard.from.x * (1f - eased),
                        top = shard.from.y * (1f - eased)
                    )
                    rotate(
                        degrees = shard.spin * (1f - eased),
                        pivot = shard.center
                    )
                    scale(
                        scaleX = 0.6f + 0.4f * eased,
                        scaleY = 0.6f + 0.4f * eased,
                        pivot = shard.center
                    )
                }) {
                    drawPath(
                        path = shard.path,
                        color = shard.color,
                        alpha = (0.35f + 0.65f * eased) * shard.targetAlpha
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashProgressBar(
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val currentProgress = FastOutSlowInEasing.transform(progress().coerceIn(0f, 1f))
        drawLine(
            color = SplashTrack,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
        if (currentProgress > 0f) {
            drawLine(
                color = SplashCyan,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width * currentProgress, size.height / 2f),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun buildShards(vector: ImageVector): List<Shard> {
    val paths = mutableListOf<VectorPath>()
    extractPaths(vector.root, paths)
    return paths.mapIndexed { index, vectorPath ->
        val random = Random(1337L + index * 37L)
        val xSpread = (random.nextFloat() * 2f - 1f) * vector.viewportWidth * 1.0f
        val ySpread = (random.nextFloat() * 2f - 1f) * vector.viewportHeight * 1.5f
        val spin = (random.nextFloat() * 90f - 45f)
        val delay = random.nextFloat() * 0.3f
        val baseColor = (vectorPath.fill as? SolidColor)?.value ?: SplashCyan
        val path = vectorPath.pathData.toPath()
        val bounds = path.getBounds()
        Shard(
            path = path,
            color = baseColor.copy(alpha = 1f),
            targetAlpha = vectorPath.fillAlpha,
            center = bounds.center,
            from = Offset(xSpread, ySpread),
            spin = spin,
            delay = delay
        )
    }
}

private fun extractPaths(group: VectorGroup, target: MutableList<VectorPath>) {
    for (node in group) {
        when (node) {
            is VectorPath -> target.add(node)
            is VectorGroup -> extractPaths(node, target)
        }
    }
}

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.caisaara.ui.theme.CaisaaraFontFamily
import com.caisaara.ui.theme.CaisaaraTypography
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

private class Shard(
    val path: Path,
    val color: Color,
    val alpha: Float,
    val delta: Offset,
    val delay: Float
)

private data class ShardData(
    val pathData: String,
    val color: Color,
    val alpha: Float,
    val delta: Offset,
    val delay: Float
)

private val ShardDefinitions = listOf(
    ShardData(
        pathData = "M53.1992 498.512C53.1992 497.124 54.3237 496 55.7108 496H78.2405C79.6276 496 80.7521 497.124 80.7521 498.512V523.226C80.7521 524.613 79.6276 525.738 78.2405 525.738H55.7108C54.3237 525.738 53.1992 524.613 53.1992 523.226V498.512Z",
        color = Color(0xFF092E37),
        alpha = 1.0f,
        delta = Offset(72.01f, -138.13f),
        delay = 0.042f
    ),
    ShardData(
        pathData = "M133.199 543.512C133.199 542.124 134.324 541 135.711 541H141.848C142.801 541 143.672 541.539 144.097 542.393L152.325 558.929C152.498 559.277 152.588 559.66 152.588 560.048V568.226C152.588 569.613 151.464 570.738 150.077 570.738H135.711C134.324 570.738 133.199 569.613 133.199 568.226V543.512Z",
        color = Color(0xFF00E5FF),
        alpha = 1.0f,
        delta = Offset(19.54f, -183.12f),
        delay = 0.05f
    ),
    ShardData(
        pathData = "M117.26 448.727C116.334 447.053 117.545 445 119.458 445H122.892C124.28 445 125.404 446.124 125.404 447.512V453.724C125.404 456.319 121.95 457.21 120.694 454.939L117.26 448.727Z",
        color = Color(0xFF00E5FF),
        alpha = 0.5f,
        delta = Offset(-0.20f, -86.91f),
        delay = 0.024f
    ),
    ShardData(
        pathData = "M90.9556 397.975C90.9556 395.271 94.6413 394.476 95.7555 396.939L99.556 405.341C100.308 407.004 99.0923 408.888 97.2676 408.888H93.4672C92.08 408.888 90.9556 407.763 90.9556 406.376V397.975Z",
        color = Color(0xFF00E5FF),
        alpha = 0.5f,
        delta = Offset(61.76f, -51.33f),
        delay = 0.022f
    ),
    ShardData(
        pathData = "M11.1992 399.368C11.1992 398.703 11.4622 398.067 11.9305 397.596L22.7499 386.724C23.7061 385.763 25.2524 385.734 26.2436 386.659L37.954 397.586C38.4631 398.061 38.7521 398.726 38.7521 399.422V418.379C38.7521 419.766 37.6276 420.89 36.2405 420.89H13.7108C12.3237 420.89 11.1992 419.766 11.1992 418.379V399.368Z",
        color = Color(0xFF00E5FF),
        alpha = 1.0f,
        delta = Offset(114.01f, -62.91f),
        delay = 0.035f
    ),
    ShardData(
        pathData = "M34.1992 345.512C34.1992 344.124 35.3237 343 36.7108 343H41.8924C43.2795 343 44.404 344.124 44.404 345.512V363.048C44.404 364.435 43.2795 365.56 41.8924 365.56H36.7108C35.3237 365.56 34.1992 364.435 34.1992 363.048V345.512Z",
        color = Color(0xFF1D5757),
        alpha = 1.0f,
        delta = Offset(80.80f, -7.61f),
        delay = 0.022f
    ),
    ShardData(
        pathData = "M73.1354 242.703C74.1238 241.757 75.6873 241.776 76.6523 242.746L92.1933 258.362C92.8619 259.034 93.0953 260.025 92.7969 260.925L89.7724 270.042C89.4316 271.07 88.4709 271.763 87.3885 271.763H78.447C77.0598 271.763 75.9354 270.639 75.9354 269.252V264.02C75.9354 262.633 74.8109 261.509 73.4238 261.509H59.7419C57.4776 261.509 56.3695 258.748 58.0053 257.183L73.1354 242.703Z",
        color = Color(0xFF00E5FF),
        alpha = 1.0f,
        delta = Offset(99.22f, 73.94f),
        delay = 0.034f
    ),
    ShardData(
        pathData = "M15.859 213.348C14.1921 211.793 15.2927 209 17.5724 209H32.7093C34.944 209 36.0659 211.699 34.4896 213.283L27.2015 220.607C26.2452 221.568 24.6989 221.596 23.7077 220.672L15.859 213.348Z",
        color = Color(0xFF0B0E14),
        alpha = 1.0f,
        delta = Offset(127.22f, 113.02f),
        delay = 0.046f
    ),
    ShardData(
        pathData = "M153.834 240.329C152.92 239.34 152.949 237.807 153.898 236.852L170.358 220.313C171.938 218.725 174.65 219.844 174.65 222.084V226.819C174.65 228.206 175.774 229.331 177.161 229.331H182.894C185.128 229.331 186.25 232.03 184.674 233.614L167.313 251.059C166.305 252.072 164.657 252.041 163.688 250.992L153.834 240.329Z",
        color = Color(0xFFAFF0D7),
        alpha = 1.0f,
        delta = Offset(-26.92f, 69.20f),
        delay = 0.02f
    ),
    ShardData(
        pathData = "M115 329.906C115 327.554 117.943 326.492 119.445 328.303L121.792 331.134C123.15 332.77 121.986 335.248 119.859 335.248H117.512C116.124 335.248 115 334.124 115 332.737V329.906Z",
        color = Color(0xFF00E5FF),
        alpha = 0.5f,
        delta = Offset(0.02f, 0.05f),
        delay = 0.0f
    ),
    ShardData(
        pathData = "M115 317.905C115 317.196 115.3 316.519 115.827 316.043L121.008 311.357C122.623 309.896 125.205 311.042 125.205 313.219V328.285C125.205 330.637 122.261 331.699 120.76 329.889L115.578 323.64C115.205 323.19 115 322.623 115 322.037V317.905Z",
        color = Color(0xFF092E37),
        alpha = 1.0f,
        delta = Offset(0.09f, 0.27f),
        delay = 0.0f
    ),
    ShardData(
        pathData = "M328.589 468.512C328.589 467.124 327.464 466 326.077 466H319.939C318.986 466 318.115 466.539 317.691 467.393L309.463 483.929C309.29 484.277 309.2 484.66 309.2 485.048V493.226C309.2 494.613 310.324 495.738 311.711 495.738H326.077C327.464 495.738 328.589 494.613 328.589 493.226V468.512Z",
        color = Color(0xFF00E5FF),
        alpha = 1.0f,
        delta = Offset(-88.12f, -108.12f),
        delay = 0.038f
    ),
    ShardData(
        pathData = "M379.752 543.512C379.752 542.124 378.628 541 377.241 541H354.711C353.324 541 352.199 542.124 352.199 543.512V568.226C352.199 569.613 353.324 570.738 354.711 570.738H377.241C378.628 570.738 379.752 569.613 379.752 568.226V543.512Z",
        color = Color(0xFF092E37),
        alpha = 1.0f,
        delta = Offset(-111.75f, -183.13f),
        delay = 0.058f
    ),
    ShardData(
        pathData = "M364.285 469.727C365.211 468.053 364 466 362.087 466H358.652C357.265 466 356.141 467.124 356.141 468.512V474.724C356.141 477.319 359.595 478.21 360.85 475.939L364.285 469.727Z",
        color = Color(0xFF00E5FF),
        alpha = 0.5f,
        delta = Offset(-88.14f, -107.91f),
        delay = 0.038f
    ),
    ShardData(
        pathData = "M301.404 391.975C301.404 389.271 297.719 388.476 296.604 390.94L292.804 399.341C292.052 401.004 293.268 402.888 295.092 402.888H298.893C300.28 402.888 301.404 401.764 301.404 400.376V391.975Z",
        color = Color(0xFF00E5FF),
        alpha = 0.5f,
        delta = Offset(-60.92f, -45.33f),
        delay = 0.021f
    ),
    ShardData(
        pathData = "M328.956 381.368C328.956 380.703 328.693 380.067 328.225 379.596L317.405 368.724C316.449 367.763 314.903 367.734 313.912 368.659L302.201 379.586C301.692 380.061 301.403 380.726 301.403 381.422V400.379C301.403 401.766 302.528 402.89 303.915 402.89H326.444C327.832 402.89 328.956 401.766 328.956 400.379V381.368Z",
        color = Color(0xFF00E5FF),
        alpha = 1.0f,
        delta = Offset(-60.95f, -44.91f),
        delay = 0.021f
    ),
    ShardData(
        pathData = "M339.161 382.84C339.161 381.453 338.036 380.328 336.649 380.328H331.468C330.081 380.328 328.956 381.453 328.956 382.84V400.376C328.956 401.764 330.081 402.888 331.468 402.888H336.649C338.036 402.888 339.161 401.764 339.161 400.376V382.84Z",
        color = Color(0xFF1D5757),
        alpha = 1.0f,
        delta = Offset(-60.95f, -44.94f),
        delay = 0.021f
    ),
    ShardData(
        pathData = "M341.347 204.703C340.359 203.757 338.795 203.776 337.83 204.746L322.289 220.362C321.621 221.034 321.387 222.025 321.686 222.925L324.71 232.042C325.051 233.07 326.011 233.763 327.094 233.763H336.035C337.423 233.763 338.547 232.639 338.547 231.252V226.02C338.547 224.633 339.672 223.509 341.059 223.509H354.741C357.005 223.509 358.113 220.748 356.477 219.183L341.347 204.703Z",
        color = Color(0xFF00E5FF),
        alpha = 1.0f,
        delta = Offset(-120.49f, 111.94f),
        delay = 0.045f
    ),
    ShardData(
        pathData = "M250.08 326.265C251.747 324.709 250.647 321.917 248.367 321.917H233.23C230.995 321.917 229.873 324.616 231.45 326.2L238.738 333.524C239.694 334.485 241.24 334.513 242.231 333.588L250.08 326.265Z",
        color = Color(0xFF0B0E14),
        alpha = 1.0f,
        delta = Offset(0.05f, 0.10f),
        delay = 0.0f
    ),
    ShardData(
        pathData = "M232.974 300.041C232.022 299.085 230.484 299.051 229.492 299.966L219.833 308.862C219.317 309.338 219.023 310.008 219.023 310.709V313.599C219.023 314.322 219.334 315.009 219.876 315.486L226.476 321.288C226.934 321.691 227.524 321.914 228.134 321.914H248.697C250.932 321.914 252.054 319.214 250.477 317.63L232.974 300.041Z",
        color = Color(0xFF1D5757),
        alpha = 1.0f,
        delta = Offset(0.07f, 0.27f),
        delay = 0.0f
    ),
    ShardData(
        pathData = "M268.006 313.98C268.006 311.695 265.201 310.597 263.649 312.276L256.374 320.149C255.46 321.137 255.489 322.671 256.438 323.625L263.714 330.936C265.294 332.524 268.006 331.405 268.006 329.164V313.98Z",
        color = Color(0xFF00E5FF),
        alpha = 1.0f,
        delta = Offset(0.09f, 0.17f),
        delay = 0.0f
    ),
    ShardData(
        pathData = "M326.302 309.329C327.216 308.34 327.187 306.807 326.238 305.852L309.778 289.313C308.198 287.725 305.486 288.844 305.486 291.084V295.819C305.486 297.206 304.362 298.331 302.975 298.331H297.242C295.008 298.331 293.886 301.03 295.462 302.614L312.823 320.059C313.831 321.072 315.479 321.041 316.448 319.992L326.302 309.329Z",
        color = Color(0xFFAFF0D7),
        alpha = 1.0f,
        delta = Offset(-60.01f, 0.20f),
        delay = 0.016f
    ),
    ShardData(
        pathData = "M338.142 329.906C338.142 327.554 335.198 326.492 333.697 328.303L331.349 331.134C329.992 332.77 331.156 335.248 333.283 335.248H335.63C337.017 335.248 338.142 334.124 338.142 332.737V329.906Z",
        color = Color(0xFF00E5FF),
        alpha = 0.5f,
        delta = Offset(-59.95f, 0.05f),
        delay = 0.016f
    ),
    ShardData(
        pathData = "M59.0316 81.7128C59.9834 80.7564 61.521 80.7229 62.5135 81.6372L72.1719 90.5338C72.6881 91.0093 72.9818 91.6792 72.9818 92.3811V95.271C72.9818 95.9931 72.671 96.6804 72.1287 97.1572L65.5293 102.96C65.0709 103.363 64.4813 103.585 63.8709 103.585H43.3079C41.0733 103.585 39.9514 100.886 41.5277 99.3019L59.0316 81.7128Z",
        color = Color(0xFF1D5757),
        alpha = 1.0f,
        delta = Offset(101.14f, 218.60f),
        delay = 0.066f
    ),
    ShardData(
        pathData = "M23.9995 95.6518C23.9995 93.3662 26.8045 92.2686 28.3557 93.9473L35.6311 101.82C36.5447 102.809 36.5163 104.342 35.5667 105.297L28.2914 112.607C26.7109 114.195 23.9995 113.076 23.9995 110.836V95.6518Z",
        color = Color(0xFF00E5FF),
        alpha = 1.0f,
        delta = Offset(101.12f, 218.50f),
        delay = 0.065f
    ),
    ShardData(
        pathData = "M29.2046 682.344C29.2046 681.634 28.9042 680.957 28.3777 680.481L23.1961 675.795C21.5812 674.335 18.9998 675.48 18.9998 677.658V692.724C18.9998 695.076 21.9433 696.138 23.4447 694.327L28.6263 688.079C29 687.628 29.2046 687.061 29.2046 686.476V682.344Z",
        color = Color(0xFF092E37),
        alpha = 1.0f,
        delta = Offset(248.92f, -364.17f),
        delay = 0.12f
    )
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

    SplashScreenContent(
        progress = animatable.value,
        modifier = modifier
    )
}

@Composable
fun SplashScreenContent(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val shards = remember {
        ShardDefinitions.map { data ->
            Shard(
                path = PathParser().parsePathString(data.pathData).toPath(),
                color = data.color,
                alpha = data.alpha,
                delta = data.delta,
                delay = data.delay
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 393f
            val scaleY = size.height / 852f

            withTransform({
                scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
            }) {
                shards.forEach { shard ->
                    val local = if (shard.delay >= 1f) 0f else {
                        ((progress - shard.delay) / (1f - shard.delay)).coerceIn(0f, 1f)
                    }
                    val eased = FastOutSlowInEasing.transform(local)
                    withTransform({
                        translate(
                            left = shard.delta.x * eased,
                            top = shard.delta.y * eased
                        )
                    }) {
                        drawPath(
                            path = shard.path,
                            color = shard.color,
                            alpha = shard.alpha
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * (678f / 852f))
                .padding(horizontal = screenWidth * (22f / 393f))
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ASSEMBLING YOUR NEXT MOVE",
                style = CaisaaraTypography.MicroBold.copy(
                    fontFamily = CaisaaraFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFF94A3B8)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            SplashProgressBar(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "MOSAIC",
                    style = CaisaaraTypography.H1.copy(
                        fontFamily = CaisaaraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF00E5FF).copy(alpha = 0.35f)
                    )
                )
                Text(
                    text = "MOSAIC",
                    style = CaisaaraTypography.H1.copy(
                        fontFamily = CaisaaraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier.graphicsLayer {
                        alpha = 0.35f + 0.65f * FastOutSlowInEasing.transform(progress)
                    }
                )
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
        val initialProgress = 0.06f
        val effectiveProgress = initialProgress + (1f - initialProgress) * currentProgress

        drawLine(
            color = Color(0xFF25334A),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF00E5FF),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width * effectiveProgress, size.height / 2f),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SplashScreenPreview() {
    SplashScreenContent(progress = 0f)
}

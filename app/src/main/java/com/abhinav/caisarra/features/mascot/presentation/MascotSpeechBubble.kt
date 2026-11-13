package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.features.mascot.domain.MascotReaction

enum class BubbleTail { START, TOP }

private val BubbleBg = Color(0xFF1C2A38)
private val LineColor = Color(0xFFF4F7FB)
private val DetailColor = Color(0xFF9FB0C3)

@Composable
fun MascotSpeechBubble(
    reaction: MascotReaction,
    tail: BubbleTail,
    modifier: Modifier = Modifier,
    centered: Boolean = false
) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .size(12.dp)
                .align(if (tail == BubbleTail.START) Alignment.CenterStart else Alignment.TopCenter)
                .offset(
                    x = if (tail == BubbleTail.START) (-7).dp else 0.dp,
                    y = if (tail == BubbleTail.TOP) (-7).dp else 0.dp
                )
        ) {
            val path = Path().apply {
                if (tail == BubbleTail.START) {
                    moveTo(size.width, 0f)
                    lineTo(0f, size.height / 2f)
                    lineTo(size.width, size.height)
                } else {
                    moveTo(0f, size.height)
                    lineTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                }
                close()
            }
            drawPath(path, BubbleBg)
        }
        AnimatedContent(
            targetState = reaction,
            contentKey = { it.key },
            transitionSpec = {
                (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 6 }) togetherWith fadeOut(tween(120))
            },
            label = "mascotSpeech",
            modifier = Modifier
                .fillMaxWidth()
                .background(BubbleBg, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        ) { current ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = current.line,
                    color = LineColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp,
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                current.detail?.let {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = it,
                        color = DetailColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

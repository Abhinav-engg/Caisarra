package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.caisaara.ui.theme.PuzzleCardBorder
import com.caisaara.ui.theme.PuzzleHeaderBg

@Composable
fun SettingsPopup(
    expanded: Boolean,
    width: Dp,
    onDismiss: () -> Unit,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = expanded
    if (!visibleState.currentState && !visibleState.targetState && visibleState.isIdle) return
    val gap = with(LocalDensity.current) { 10.dp.roundToPx() }
    val positionProvider = remember(gap) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ) = IntOffset(anchorBounds.left, anchorBounds.top - popupContentSize.height - gap)
        }
    }
    val shape = RoundedCornerShape(20.dp)
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = expandVertically(spring(dampingRatio = 0.75f, stiffness = 400f), expandFrom = Alignment.Bottom) +
                fadeIn(tween(180)),
            exit = shrinkVertically(tween(220, 80), shrinkTowards = Alignment.Bottom) + fadeOut(tween(200, 80))
        ) {
            Column(
                modifier = Modifier
                    .width(width)
                    .background(PuzzleHeaderBg, shape)
                    .border(1.dp, PuzzleCardBorder, shape)
                    .padding(8.dp)
            ) {
                content()
            }
        }
    }
}

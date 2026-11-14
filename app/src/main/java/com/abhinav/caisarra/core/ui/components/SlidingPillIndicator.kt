package com.abhinav.caisarra.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.abhinav.caisarra.core.ui.theme.NavCornerRadius
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlidingPillIndicator(
    selectedIndex: Int,
    itemCount: Int,
    modifier: Modifier = Modifier,
    inset: PaddingValues = PaddingValues()
) {
    BoxWithConstraints(modifier = modifier) {
        val slot = constraints.maxWidth.toFloat() / itemCount
        val left = remember { Animatable(slot * selectedIndex) }
        val right = remember { Animatable(slot * (selectedIndex + 1)) }
        val leading = remember { spring<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium) }
        val trailing = remember { spring<Float>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow) }

        LaunchedEffect(selectedIndex, slot) {
            val target = slot * selectedIndex
            val movingRight = target > left.value
            launch { left.animateTo(target, if (movingRight) trailing else leading) }
            launch { right.animateTo(target + slot, if (movingRight) leading else trailing) }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(left.value.roundToInt(), 0) }
                .width(with(LocalDensity.current) { (right.value - left.value).toDp() })
                .fillMaxHeight()
                .padding(inset)
                .glassSurface(RoundedCornerShape(NavCornerRadius))
        )
    }
}

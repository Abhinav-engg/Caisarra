package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.features.mascot.domain.MascotReaction

@Composable
fun MascotSpotlight(
    reaction: MascotReaction,
    modifier: Modifier = Modifier,
    mascotSize: Dp = 120.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Mascot(mood = reaction.mood, reactionKey = reaction.key, size = mascotSize)
        Spacer(Modifier.height(14.dp))
        MascotSpeechBubble(
            reaction = reaction,
            tail = BubbleTail.TOP,
            centered = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

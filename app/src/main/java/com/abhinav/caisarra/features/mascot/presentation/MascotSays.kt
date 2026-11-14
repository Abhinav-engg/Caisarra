package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.features.mascot.domain.MascotReaction

@Composable
fun MascotSays(
    reaction: MascotReaction,
    modifier: Modifier = Modifier,
    mascotSize: Dp = 56.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Mascot(mood = reaction.mood, reactionKey = reaction.key, size = mascotSize)
        Spacer(Modifier.width(12.dp))
        MascotSpeechBubble(
            reaction = reaction,
            tail = BubbleTail.START,
            modifier = Modifier.weight(1f)
        )
    }
}

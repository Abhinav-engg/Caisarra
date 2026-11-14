package com.abhinav.caisarra.features.mascot.domain

import kotlin.math.abs
import kotlin.math.roundToInt

object GameResultMascotLines {

    private val victory = listOf(
        "You won! That was a proper fight.",
        "Victory! I was cheering the whole time.",
        "Yes! Well played, really well played.",
        "You did it! Go on, do a little dance."
    )

    private val defeat = listOf(
        "Hey, it's okay. Even the greats lose games.",
        "That one stung, I know. Want to look at it together?",
        "Losses are just lessons in disguise. You'll get the next one.",
        "Come here. Deep breath. We go again."
    )

    private val draw = listOf(
        "A draw! Neither of you blinked.",
        "Half a point, full respect.",
        "Evenly matched. That was a good game."
    )

    fun victory(ratingChange: Int?, accuracy: Double?, seed: Int): MascotReaction {
        val detail = when {
            accuracy != null && accuracy >= 90 -> "${accuracy.roundToInt()}% accuracy?! Who taught you that?"
            ratingChange != null && ratingChange > 0 -> "+$ratingChange rating. You earned every point."
            else -> null
        }
        val mood = if (ratingChange != null && ratingChange >= 10) MascotMood.CHAMPION else MascotMood.JOYFUL
        return MascotReaction(mood, victory.pick(seed), detail, key = "victory")
    }

    fun defeat(ratingChange: Int?, accuracy: Double?, seed: Int): MascotReaction {
        val detail = when {
            accuracy != null && accuracy >= 80 -> "You still played at ${accuracy.roundToInt()}% accuracy. That's really solid."
            ratingChange != null && ratingChange < 0 -> "It's only ${abs(ratingChange)} points. We'll win them back."
            else -> "The analysis might show you something cool."
        }
        return MascotReaction(MascotMood.HUG_PAWN, defeat.pick(seed), detail, key = "defeat")
    }

    fun draw(seed: Int): MascotReaction =
        MascotReaction(MascotMood.HAPPY, draw.pick(seed), key = "draw")
}

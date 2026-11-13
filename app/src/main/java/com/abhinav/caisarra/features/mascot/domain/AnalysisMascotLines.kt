package com.abhinav.caisarra.features.mascot.domain

object AnalysisMascotLines {

    fun start(): MascotReaction =
        MascotReaction(
            MascotMood.WAVING,
            "Let's walk through this game together. Tap next when you're ready!",
            key = "start"
        )

    fun forMove(classification: String, explanation: String, moveNumber: Int): MascotReaction {
        val (mood, lines) = when (classification.trim().lowercase()) {
            "brilliant" -> MascotMood.LOVE to listOf(
                "Wait, WHAT? That's brilliant!",
                "Oh, that's gorgeous. I need a moment.",
                "Brilliant! I'm framing this one."
            )
            "great" -> MascotMood.EXCITED to listOf(
                "Ooh, great find! Not many would see that.",
                "That's the only move that holds. Great eye!"
            )
            "best" -> MascotMood.HAPPY to listOf(
                "Spot on. That's exactly what I'd play.",
                "Textbook. Nothing better here.",
                "Perfect. The engine agrees."
            )
            "good" -> MascotMood.SMUG to listOf(
                "Solid. Not perfect, but solid.",
                "Good move. Something slightly better existed."
            )
            "book" -> MascotMood.HUG_PAWN to listOf(
                "Straight out of the opening book.",
                "Theory! Players have been here many times."
            )
            "inaccuracy" -> MascotMood.THINKING to listOf(
                "Hmm, a little loose. There was something sharper.",
                "Not bad, but not the best either."
            )
            "mistake" -> MascotMood.WORRIED to listOf(
                "Uh oh… that gives the other side a chance.",
                "Hmm, that one lets things slip."
            )
            "miss" -> MascotMood.SURPRISED to listOf(
                "Aww, so close! There was a win hiding here.",
                "Wait, there was something big here! Missed it."
            )
            "blunder" -> MascotMood.CRYING to listOf(
                "Oof. That one really hurts.",
                "Nooo… that changes everything.",
                "Ouch. We all have moves like this."
            )
            else -> MascotMood.THINKING to listOf("Let's see what happens next.")
        }
        return MascotReaction(
            mood = mood,
            line = lines.pick(moveNumber),
            detail = explanation.takeIf { it.isNotBlank() },
            key = "move-$moveNumber"
        )
    }
}

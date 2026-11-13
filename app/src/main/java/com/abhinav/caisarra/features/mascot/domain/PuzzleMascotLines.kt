package com.abhinav.caisarra.features.mascot.domain

object PuzzleMascotLines {

    private val idle = listOf(
        "Take your time. The best move is hiding in there somewhere.",
        "Checks, captures, threats. Start there.",
        "No rush. I believe in you.",
        "Hmm… what's the most forcing move here?"
    )

    private val correctStep = listOf(
        "Yes! That's the one. Keep going.",
        "Ooh, nice. Now what comes next?",
        "Right on! Don't stop now.",
        "Good eye! Finish it off."
    )

    private val wrong = listOf(
        "Not quite, but you're close. Look again.",
        "Hmm, that's not it. Shake it off.",
        "Almost! There's something sharper.",
        "Oops. Happens to the best of us. Try again?"
    )

    private val lastTry = listOf(
        "Last try. Deep breath, you've got this.",
        "One shot left. Slow down and check every check.",
        "Okay, final try. I'm right here with you."
    )

    private val hint = listOf(
        "Psst… see the piece I lit up? Start there.",
        "Here's a little nudge. The rest is all you.",
        "Little secret: that glowing piece is the hero."
    )

    private val solved = listOf(
        "You cracked it! I knew you would.",
        "Yesss! That was beautiful.",
        "Puzzle solved! Look at you go."
    )

    private val failed = listOf(
        "This one was a tough cookie. Peek at the solution, tomorrow's yours.",
        "Hey, it happens. Every miss teaches you a pattern.",
        "Not today, and that's okay. We'll get the next one together."
    )

    fun idle(attemptsLeft: Int, message: String?, seed: Int): MascotReaction =
        if (attemptsLeft == 1) {
            MascotReaction(MascotMood.NERVOUS, lastTry.pick(seed), message, key = "idle-last")
        } else {
            MascotReaction(MascotMood.THINKING, idle.pick(seed), message, key = "idle")
        }

    fun moveResult(isCorrect: Boolean, attemptsLeft: Int, seed: Long): MascotReaction = when {
        isCorrect -> MascotReaction(MascotMood.EXCITED, correctStep.pick(seed), key = "correct-$seed")
        attemptsLeft == 1 -> MascotReaction(MascotMood.NERVOUS, lastTry.pick(seed), key = "wrong-$seed")
        else -> MascotReaction(MascotMood.WORRIED, wrong.pick(seed), key = "wrong-$seed")
    }

    fun hint(seed: Int): MascotReaction =
        MascotReaction(MascotMood.IDEA, hint.pick(seed), key = "hint-$seed")

    fun watchingSolution(message: String?): MascotReaction =
        MascotReaction(MascotMood.THINKING, "Watch closely, here's how it goes.", message, key = "solution")

    fun solved(streak: Int, ratingChange: Int, seed: Int): MascotReaction {
        val detail = when {
            streak > 1 -> "That's $streak days in a row. I'm so proud of you."
            ratingChange > 0 -> "+$ratingChange rating. See you tomorrow?"
            else -> "See you tomorrow for the next one?"
        }
        val mood = if (streak >= 7) MascotMood.CHAMPION else MascotMood.JOYFUL
        return MascotReaction(mood, solved.pick(seed), detail, key = "solved")
    }

    fun failed(seed: Int): MascotReaction =
        MascotReaction(MascotMood.HUG_PAWN, failed.pick(seed), key = "failed")
}

package com.abhinav.caisarra.features.mascot.domain

import com.abhinav.caisarra.data.remote.dto.RushSummaryDto

object RushMascotLines {

    private const val SURVIVAL = "survival"

    private val solved = listOf(
        "Got it!",
        "Clean!",
        "Too easy for you.",
        "Next! Keep that rhythm.",
        "Smooth. Really smooth."
    )

    private val milestone = listOf(
        "You're on fire!",
        "I can barely keep up!",
        "Okay, now you're just showing off."
    )

    private val step = listOf(
        "Good, keep going.",
        "That's it. One more.",
        "Nice. Finish it!"
    )

    private val strike = listOf(
        "Oops. Shake it off, next one's yours.",
        "Ah, sneaky one. Don't let it rattle you.",
        "That's okay. Reset and go again."
    )

    private val hint = listOf(
        "Psst… try the glowing piece.",
        "Little nudge. You take it from here."
    )

    fun idle(mode: String, strikesLeft: Int, secondsRemaining: Int?, seed: Int): MascotReaction = when {
        mode == SURVIVAL && strikesLeft == 1 ->
            MascotReaction(MascotMood.NERVOUS, "One life left. Slow and steady.", key = "idle-lastlife")
        secondsRemaining != null && secondsRemaining <= 15 ->
            MascotReaction(MascotMood.NERVOUS, "Clock's ticking! Trust your gut.", key = "idle-clock")
        mode == SURVIVAL ->
            MascotReaction(MascotMood.THINKING, listOf("Three lives. Make them count.", "Steady hands, clear head.").pick(seed), key = "idle")
        else ->
            MascotReaction(MascotMood.THINKING, listOf("Fast hands, calm mind.", "Spot it, play it, next!").pick(seed), key = "idle")
    }

    fun solved(score: Int): MascotReaction =
        if (score > 0 && score % 5 == 0) {
            MascotReaction(MascotMood.CHAMPION, "$score solved! ${milestone.pick(score / 5)}", key = "solved-$score")
        } else {
            val mood = if (score % 2 == 0) MascotMood.EXCITED else MascotMood.JOYFUL
            MascotReaction(mood, solved.pick(score), key = "solved-$score")
        }

    fun step(seed: Long): MascotReaction =
        MascotReaction(MascotMood.HAPPY, step.pick(seed), key = "step-$seed")

    fun strike(mode: String, strikes: Int, strikesLeft: Int): MascotReaction = when {
        mode == SURVIVAL && strikesLeft == 1 ->
            MascotReaction(MascotMood.NERVOUS, "Eek, one life left. Slow down, we've got this.", key = "strike-$strikes")
        mode == SURVIVAL && strikesLeft > 1 ->
            MascotReaction(MascotMood.WORRIED, "Shake it off. $strikesLeft lives left.", key = "strike-$strikes")
        else ->
            MascotReaction(MascotMood.WORRIED, strike.pick(strikes), key = "strike-$strikes")
    }

    fun hint(seed: Int): MascotReaction =
        MascotReaction(MascotMood.IDEA, hint.pick(seed), key = "hint-$seed")

    fun gameOver(summary: RushSummaryDto): MascotReaction {
        val score = summary.finalScore
        return when {
            summary.isNewHighScore ->
                MascotReaction(MascotMood.CHAMPION, "New personal best! $score. I knew you had it in you!", key = "over")
            score == 0 ->
                MascotReaction(MascotMood.HUG_PAWN, "Rough round. Everyone has those. Warm up and try again?", key = "over")
            summary.mode == SURVIVAL ->
                MascotReaction(MascotMood.HAPPY, "You survived $score puzzles. Respect!", "Your best is ${summary.bestScore}. Let's chase it.", key = "over")
            else ->
                MascotReaction(MascotMood.HAPPY, "$score solved. Not bad at all! One more?", "Your best is ${summary.bestScore}.", key = "over")
        }
    }
}

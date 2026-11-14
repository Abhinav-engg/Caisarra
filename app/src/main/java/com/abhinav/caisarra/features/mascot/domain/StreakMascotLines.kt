package com.abhinav.caisarra.features.mascot.domain

object StreakMascotLines {

    fun forStreak(
        currentStreak: Int,
        bestStreak: Int,
        todayCompleted: Boolean,
        status: String
    ): MascotReaction {
        val isRecord = currentStreak >= 3 &&currentStreak >= bestStreak
        return when {
            status == "broken" || (currentStreak == 0 && bestStreak > 0) -> MascotReaction(
                MascotMood.CRYING,
                "Our streak broke…",
                "But every streak starts at day one. Let's start a new one today?",
                key = "broken"
            )
            status == "paused" -> MascotReaction(
                MascotMood.CRYING,
                "You missed a day… I waited for you.",
                "Play today and we can still save it!",
                key = "paused"
            )
            isRecord && todayCompleted -> MascotReaction(
                MascotMood.CHAMPION,
                "New record! $currentStreak days!",
                "Your longest streak ever. I'm so proud of you!",
                key = "record"
            )
            isRecord -> MascotReaction(
                MascotMood.EXCITED,
                "You're at your best ever!",
                "One game today and the record grows.",
                key = "record-pending"
            )
            todayCompleted -> MascotReaction(
                MascotMood.HAPPY,
                "Done for today!",
                "See you tomorrow, same time?",
                key = "done"
            )
            currentStreak == 0 -> MascotReaction(
                MascotMood.WAVING,
                "Let's start a streak!",
                "One game today is all it takes.",
                key = "fresh"
            )
            else -> MascotReaction(
                MascotMood.HUG_PAWN,
                "Don't leave me hanging!",
                "One game today keeps the flame alive.",
                key = "pending"
            )
        }
    }
}

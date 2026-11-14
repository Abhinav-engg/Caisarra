package com.abhinav.caisarra.features.mascot.domain

enum class MascotMood(val isCelebration: Boolean = false) {
    CHAMPION(isCelebration = true),
    CRYING,
    EXCITED(isCelebration = true),
    GRUMPY,
    HAPPY,
    HUG_PAWN,
    IDEA,
    JOYFUL(isCelebration = true),
    LOVE(isCelebration = true),
    NERVOUS,
    SLEEPY,
    SMUG,
    SURPRISED,
    THINKING,
    WAVING,
    WORRIED
}

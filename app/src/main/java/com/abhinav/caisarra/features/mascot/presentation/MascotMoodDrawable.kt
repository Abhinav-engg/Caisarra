package com.abhinav.caisarra.features.mascot.presentation

import androidx.annotation.DrawableRes
import com.abhinav.caisarra.R
import com.abhinav.caisarra.features.mascot.domain.MascotMood

@get:DrawableRes
val MascotMood.drawableRes: Int
    get() = when (this) {
        MascotMood.CHAMPION -> R.drawable.mascot_champion
        MascotMood.CRYING -> R.drawable.mascot_crying
        MascotMood.EXCITED -> R.drawable.mascot_excited
        MascotMood.GRUMPY -> R.drawable.mascot_grumpy
        MascotMood.HAPPY -> R.drawable.mascot_happy
        MascotMood.HUG_PAWN -> R.drawable.mascot_hug_pawn
        MascotMood.IDEA -> R.drawable.mascot_idea
        MascotMood.JOYFUL -> R.drawable.mascot_joyful
        MascotMood.LOVE -> R.drawable.mascot_love
        MascotMood.NERVOUS -> R.drawable.mascot_nervous
        MascotMood.SLEEPY -> R.drawable.mascot_sleepy
        MascotMood.SMUG -> R.drawable.mascot_smug
        MascotMood.SURPRISED -> R.drawable.mascot_surprised
        MascotMood.THINKING -> R.drawable.mascot_thinking
        MascotMood.WAVING -> R.drawable.mascot_waving
        MascotMood.WORRIED -> R.drawable.mascot_worried
    }

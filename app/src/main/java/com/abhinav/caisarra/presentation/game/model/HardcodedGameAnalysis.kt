package com.abhinav.caisarra.presentation.game.model

data class AnalysisMoveNote(
    val ply: String,
    val move: String,
    val tag: String,
    val comment: String
)

object HardcodedGameAnalysis {
    const val accuracyPercent = 94.2
    const val brilliantCount = 2
    const val bestMoveCount = 24
    const val inaccuracyCount = 1
    const val mistakeCount = 0
    const val blunderCount = 0

    val summary =
        "Your play stayed accurate through the middlegame. Two brilliant shots decided the game, and nearly every move matched the engine’s first choice."

    val notes = listOf(
        AnalysisMoveNote("12. Qg4", "Qg4", "Brilliant", "A forcing queen lift that wins a tempo and opens the kingside."),
        AnalysisMoveNote("18. Nxe6", "Nxe6", "Brilliant", "Sacrifice that rips open the pawn cover around the king."),
        AnalysisMoveNote("19. Qxe6+", "Qxe6+", "Best", "The only move that keeps the attack rolling."),
        AnalysisMoveNote("24. Rd7", "Rd7", "Best", "Seizes the seventh rank before the opponent can reorganize."),
        AnalysisMoveNote("27. h4", "h4", "Inaccuracy", "A quieter push. h5 would have stretched the defense further."),
        AnalysisMoveNote("31. Qg7#", "Qg7#", "Best", "Mate on g7 finishes a clean attacking game.")
    )
}

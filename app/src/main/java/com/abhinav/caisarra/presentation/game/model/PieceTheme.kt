package com.abhinav.caisarra.presentation.game.model

import androidx.annotation.DrawableRes
import com.abhinav.caisarra.R

enum class PieceThemeId(val storageKey: String, val displayName: String) {
    CLASSIC("classic", "Classic (Burnett)"),
    MERIDA("merida", "Merida"),
    ALPHA("alpha", "Alpha"),
    ANARCANDY("anarcandy", "Anarcandy"),
    PIXEL("pixel", "8-Bit Pixel");

    companion object {
        fun fromStorageKey(key: String?): PieceThemeId {
            return entries.firstOrNull { it.storageKey == key } ?: CLASSIC
        }
    }
}

data class PieceTheme(
    val id: PieceThemeId,
    val name: String,
    val description: String,
    @DrawableRes val previewWhitePiece: Int,
    @DrawableRes val previewBlackPiece: Int
) {
    @DrawableRes
    fun getPieceDrawable(color: PieceColor, type: PieceType): Int {
        return when (id) {
            PieceThemeId.CLASSIC -> when (color) {
                PieceColor.WHITE -> when (type) {
                    PieceType.KING -> R.drawable.white_king
                    PieceType.QUEEN -> R.drawable.white_queen
                    PieceType.ROOK -> R.drawable.white_castle
                    PieceType.BISHOP -> R.drawable.white_bishop
                    PieceType.KNIGHT -> R.drawable.white_knight
                    PieceType.PAWN -> R.drawable.white_pawn
                }
                PieceColor.BLACK -> when (type) {
                    PieceType.KING -> R.drawable.black_king
                    PieceType.QUEEN -> R.drawable.black_queen
                    PieceType.ROOK -> R.drawable.black_castle
                    PieceType.BISHOP -> R.drawable.black_bishop
                    PieceType.KNIGHT -> R.drawable.black_knight
                    PieceType.PAWN -> R.drawable.black_pawn
                }
            }

            PieceThemeId.MERIDA -> when (color) {
                PieceColor.WHITE -> when (type) {
                    PieceType.KING -> R.drawable.merida_white_king
                    PieceType.QUEEN -> R.drawable.merida_white_queen
                    PieceType.ROOK -> R.drawable.merida_white_castle
                    PieceType.BISHOP -> R.drawable.merida_white_bishop
                    PieceType.KNIGHT -> R.drawable.merida_white_knight
                    PieceType.PAWN -> R.drawable.merida_white_pawn
                }
                PieceColor.BLACK -> when (type) {
                    PieceType.KING -> R.drawable.merida_black_king
                    PieceType.QUEEN -> R.drawable.merida_black_queen
                    PieceType.ROOK -> R.drawable.merida_black_castle
                    PieceType.BISHOP -> R.drawable.merida_black_bishop
                    PieceType.KNIGHT -> R.drawable.merida_black_knight
                    PieceType.PAWN -> R.drawable.merida_black_pawn
                }
            }

            PieceThemeId.ALPHA -> when (color) {
                PieceColor.WHITE -> when (type) {
                    PieceType.KING -> R.drawable.alpha_white_king
                    PieceType.QUEEN -> R.drawable.alpha_white_queen
                    PieceType.ROOK -> R.drawable.alpha_white_castle
                    PieceType.BISHOP -> R.drawable.alpha_white_bishop
                    PieceType.KNIGHT -> R.drawable.alpha_white_knight
                    PieceType.PAWN -> R.drawable.alpha_white_pawn
                }
                PieceColor.BLACK -> when (type) {
                    PieceType.KING -> R.drawable.alpha_black_king
                    PieceType.QUEEN -> R.drawable.alpha_black_queen
                    PieceType.ROOK -> R.drawable.alpha_black_castle
                    PieceType.BISHOP -> R.drawable.alpha_black_bishop
                    PieceType.KNIGHT -> R.drawable.alpha_black_knight
                    PieceType.PAWN -> R.drawable.alpha_black_pawn
                }
            }

            PieceThemeId.ANARCANDY -> when (color) {
                PieceColor.WHITE -> when (type) {
                    PieceType.KING -> R.drawable.anarcandy_white_king
                    PieceType.QUEEN -> R.drawable.anarcandy_white_queen
                    PieceType.ROOK -> R.drawable.anarcandy_white_castle
                    PieceType.BISHOP -> R.drawable.anarcandy_white_bishop
                    PieceType.KNIGHT -> R.drawable.anarcandy_white_knight
                    PieceType.PAWN -> R.drawable.anarcandy_white_pawn
                }
                PieceColor.BLACK -> when (type) {
                    PieceType.KING -> R.drawable.anarcandy_black_king
                    PieceType.QUEEN -> R.drawable.anarcandy_black_queen
                    PieceType.ROOK -> R.drawable.anarcandy_black_castle
                    PieceType.BISHOP -> R.drawable.anarcandy_black_bishop
                    PieceType.KNIGHT -> R.drawable.anarcandy_black_knight
                    PieceType.PAWN -> R.drawable.anarcandy_black_pawn
                }
            }

            PieceThemeId.PIXEL -> when (color) {
                PieceColor.WHITE -> when (type) {
                    PieceType.KING -> R.drawable.pixel_white_king
                    PieceType.QUEEN -> R.drawable.pixel_white_queen
                    PieceType.ROOK -> R.drawable.pixel_white_castle
                    PieceType.BISHOP -> R.drawable.pixel_white_bishop
                    PieceType.KNIGHT -> R.drawable.pixel_white_knight
                    PieceType.PAWN -> R.drawable.pixel_white_pawn
                }
                PieceColor.BLACK -> when (type) {
                    PieceType.KING -> R.drawable.pixel_black_king
                    PieceType.QUEEN -> R.drawable.pixel_black_queen
                    PieceType.ROOK -> R.drawable.pixel_black_castle
                    PieceType.BISHOP -> R.drawable.pixel_black_bishop
                    PieceType.KNIGHT -> R.drawable.pixel_black_knight
                    PieceType.PAWN -> R.drawable.pixel_black_pawn
                }
            }
        }
    }
}

object PieceThemes {
    val Classic = PieceTheme(
        id = PieceThemeId.CLASSIC,
        name = "Classic (Burnett)",
        description = "Standard modern vector Staunton pieces with crisp outlines",
        previewWhitePiece = R.drawable.white_knight,
        previewBlackPiece = R.drawable.black_knight
    )

    val Merida = PieceTheme(
        id = PieceThemeId.MERIDA,
        name = "Merida",
        description = "Iconic tournament style standard on Chess.com & Lichess",
        previewWhitePiece = R.drawable.merida_white_knight,
        previewBlackPiece = R.drawable.merida_black_knight
    )

    val Alpha = PieceTheme(
        id = PieceThemeId.ALPHA,
        name = "Alpha",
        description = "Smooth, minimalist curved silhouettes designed by Eric Bentzen",
        previewWhitePiece = R.drawable.alpha_white_knight,
        previewBlackPiece = R.drawable.alpha_black_knight
    )

    val Anarcandy = PieceTheme(
        id = PieceThemeId.ANARCANDY,
        name = "Anarcandy",
        description = "Vibrant pop candy illustration style with colorful outlines",
        previewWhitePiece = R.drawable.anarcandy_white_knight,
        previewBlackPiece = R.drawable.anarcandy_black_knight
    )

    val Pixel = PieceTheme(
        id = PieceThemeId.PIXEL,
        name = "8-Bit Pixel",
        description = "Retro arcade pixel-art sprites for nostalgic chess sessions",
        previewWhitePiece = R.drawable.pixel_white_knight,
        previewBlackPiece = R.drawable.pixel_black_knight
    )

    val allThemes: List<PieceTheme> = listOf(Classic, Merida, Alpha, Anarcandy, Pixel)

    fun getById(id: PieceThemeId): PieceTheme = when (id) {
        PieceThemeId.CLASSIC -> Classic
        PieceThemeId.MERIDA -> Merida
        PieceThemeId.ALPHA -> Alpha
        PieceThemeId.ANARCANDY -> Anarcandy
        PieceThemeId.PIXEL -> Pixel
    }

    fun getByStorageKey(key: String?): PieceTheme {
        return getById(PieceThemeId.fromStorageKey(key))
    }
}

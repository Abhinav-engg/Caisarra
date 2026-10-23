
package com.abhinav.caisarra.presentation.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.data.remote.dto.CommunityGameDto
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.pgn.PgnHolder

private val GameBackground = Color(0xFF0B121A)
private val GameText = Color(0xFFF4F6FA)
private val GameMuted = Color(0xFFADB8C6)
private val GameAccent = Color(0xFF10B981)
private val BoardLight = Color(0xFFE8EDF3)
private val BoardDark = Color(0xFF53677E)

private const val STARTING_FEN =
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

private data class ReplayPosition(
    val fen: String,
    val move: String
)

private fun parseReplay(pgn: String): List<ReplayPosition> {
    val positions = mutableListOf(
        ReplayPosition(
            fen = STARTING_FEN,
            move = "Starting position"
        )
    )

    return try {
        val holder = PgnHolder("community.pgn")
        holder.loadPgn(pgn)

        val game = holder.games.firstOrNull()
            ?: return positions

        val board = Board()

        game.halfMoves.forEachIndexed { index, move ->
            board.doMove(move)

            positions.add(
                ReplayPosition(
                    fen = board.fen,
                    move = "Move ${index + 1}"
                )
            )
        }

        positions
    } catch (e: Exception) {
        android.util.Log.e(
            "CommunityGameReplay",
            "Unable to parse shared PGN",
            e
        )
        positions
    }
}

@Composable
fun CommunityGamePostCard(
    game: CommunityGameDto,
    fen: String?
) {
    val positions = remember(game.pgn) {
        parseReplay(game.pgn)
    }

    var currentIndex by remember(game.id) {
        mutableIntStateOf(0)
    }

    val currentFen = positions
        .getOrNull(currentIndex)
        ?.fen
        ?: fen
        ?: STARTING_FEN

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                GameBackground,
                RoundedCornerShape(18.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.white_pawn),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "SHARED GAME",
                    color = GameAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Text(
                text = game.mode.uppercase(),
                color = GameMuted,
                fontSize = 12.sp
            )
        }

        PlayerRow(
            pieceDrawable = R.drawable.white_king,
            name = game.whiteName,
            rating = game.whiteRating
        )

        ChessReplayBoard(
            fen = currentFen,
            modifier = Modifier.fillMaxWidth()
        )

        PlayerRow(
            pieceDrawable = R.drawable.black_king,
            name = game.blackName,
            rating = game.blackRating
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RESULT",
                    color = GameMuted,
                    fontSize = 10.sp
                )

                Text(
                    text = game.result
                        .replace("_", " ")
                        .uppercase(),
                    color = GameAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "END REASON",
                    color = GameMuted,
                    fontSize = 10.sp
                )

                Text(
                    text = game.endReason.replace("_", " "),
                    color = GameText,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = if (positions.size > 1) {
                "Position ${currentIndex + 1} of ${positions.size}"
            } else {
                "Replay unavailable for this PGN"
            },
            color = GameMuted,
            fontSize = 12.sp
        )

        Slider(
            value = currentIndex.toFloat(),
            onValueChange = {
                currentIndex = it.toInt().coerceIn(
                    0,
                    positions.lastIndex
                )
            },
            valueRange = 0f..positions.lastIndex
                .coerceAtLeast(1).toFloat(),
            enabled = positions.size > 1,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = GameAccent,
                activeTrackColor = GameAccent
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReplayButton("⏮") {
                currentIndex = 0
            }

            ReplayButton("◀") {
                currentIndex = (currentIndex - 1).coerceAtLeast(0)
            }

            Text(
                text = if (currentIndex == 0) {
                    "Start"
                } else {
                    positions[currentIndex].move
                },
                color = GameText,
                fontWeight = FontWeight.SemiBold
            )

            ReplayButton("▶") {
                currentIndex = (currentIndex + 1)
                    .coerceAtMost(positions.lastIndex)
            }

            ReplayButton("⏭") {
                currentIndex = positions.lastIndex
            }
        }
    }
}

@Composable
private fun ReplayButton(
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = GameAccent
        )
    ) {
        Text(
            text = label,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlayerRow(
    pieceDrawable: Int,
    name: String,
    rating: Int?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(pieceDrawable),
            contentDescription = "$name chess king",
            modifier = Modifier.size(30.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = name,
            color = GameText,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = rating?.toString() ?: "—",
            color = GameMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ChessReplayBoard(
    fen: String,
    modifier: Modifier = Modifier
) {
    val pieces = remember(fen) {
        parseFenPieces(fen)
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(3.dp)
            )
    ) {
        repeat(8) { row ->
            Row(
                modifier = Modifier.weight(1f)
            ) {
                repeat(8) { column ->
                    val rank = 7 - row
                    val file = column
                    val piece = pieces[rank * 8 + file]
                    val light = (rank + file) % 2 == 0

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                if (light) BoardLight else BoardDark
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (piece != null) {
                            Image(
                                painter = painterResource(
                                    id = pieceDrawableFor(piece)
                                ),
                                contentDescription = pieceDescription(piece),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun pieceDrawableFor(piece: Char): Int {
    return when (piece) {
        'K' -> R.drawable.white_king
        'Q' -> R.drawable.white_queen
        'R' -> R.drawable.white_castle
        'B' -> R.drawable.white_bishop
        'N' -> R.drawable.white_knight
        'P' -> R.drawable.white_pawn
        'k' -> R.drawable.black_king
        'q' -> R.drawable.black_queen
        'r' -> R.drawable.black_castle
        'b' -> R.drawable.black_bishop
        'n' -> R.drawable.black_knight
        'p' -> R.drawable.black_pawn

        else -> R.drawable.white_pawn
    }
}
private fun pieceDescription(piece: Char): String {
    val color = if (piece.isUpperCase()) "White" else "Black"

    val name = when (piece.lowercaseChar()) {
        'k' -> "king"
        'q' -> "queen"
        'r' -> "rook"
        'b' -> "bishop"
        'n' -> "knight"
        'p' -> "pawn"
        else -> "chess piece"
    }

    return "$color $name"
}

private fun parseFenPieces(fen: String): List<Char?> {
    val board = MutableList<Char?>(64) { null }
    val placement = fen.substringBefore(" ")

    var rank = 7
    var file = 0

    for (character in placement) {
        when {
            character == '/' -> {
                rank--
                file = 0
            }

            character.isDigit() -> {
                file += character.digitToInt()
            }

            character in "pnbrqkPNBRQK" -> {
                if (rank in 0..7 && file in 0..7) {
                    board[rank * 8 + file] = character
                }
                file++
            }
        }
    }

    return board
}

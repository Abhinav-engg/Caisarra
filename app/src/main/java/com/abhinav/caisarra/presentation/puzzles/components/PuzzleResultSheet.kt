package com.abhinav.caisarra.presentation.puzzles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

@Composable
fun PuzzleResultSheet(
    isSolved: Boolean,
    ratingChange: Int,
    newRating: Int,
    streak: Int,
    message: String,
    onViewSolution: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSolved) "PUZZLE SOLVED!" else "PUZZLE FAILED",
                fontFamily = JetBrainsMono,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSolved) Color(0xFF00D6A0) else Color(0xFFF04452)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RATING",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMono,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$newRating",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val chipBg = when {
                        ratingChange > 0 -> Color(0xFF064E3B)
                        ratingChange < 0 -> Color(0xFF7F1D1D)
                        else -> Color(0xFF334155)
                    }
                    val chipTextColor = when {
                        ratingChange > 0 -> Color(0xFF34D399)
                        ratingChange < 0 -> Color(0xFFF87171)
                        else -> Color(0xFF94A3B8)
                    }
                    val chipText = when {
                        ratingChange > 0 -> "+$ratingChange"
                        ratingChange < 0 -> "$ratingChange"
                        else -> "0"
                    }
                    Box(
                        modifier = Modifier
                            .background(chipBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipTextColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "STREAK",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMono,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$streak days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isSolved && onViewSolution != null) {
                GeneralButton(
                    text = "View Solution",
                    onClick = onViewSolution
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            GeneralButton(
                text = "Back to Puzzles",
                onClick = onClose
            )
        }
    }
}

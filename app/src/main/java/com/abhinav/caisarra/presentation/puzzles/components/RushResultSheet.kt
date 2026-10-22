package com.abhinav.caisarra.presentation.puzzles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.abhinav.caisarra.data.remote.dto.RushSummaryDto

@Composable
fun RushResultSheet(
    summary: RushSummaryDto,
    onPlayAgain: () -> Unit,
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
                text = "RUSH COMPLETED",
                fontFamily = JetBrainsMono,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF23CDE8),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (summary.isNewHighScore) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF064E3B), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF059669), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW HIGH SCORE!",
                        color = Color(0xFF34D399),
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "${summary.finalScore}",
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono,
                color = Color.White
            )

            Text(
                text = "PUZZLES SOLVED",
                fontSize = 11.sp,
                fontFamily = JetBrainsMono,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BEST SCORE",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMono,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.bestScore}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "STRIKES",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMono,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalStrikes}/3",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF87171)
                    )
                }

                if (summary.durationSec > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DURATION",
                            fontSize = 11.sp,
                            fontFamily = JetBrainsMono,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val mins = summary.durationSec / 60
                        val secs = summary.durationSec % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GeneralButton(
                text = "Play Again",
                onClick = onPlayAgain
            )

            Spacer(modifier = Modifier.height(10.dp))

            GeneralButton(
                text = "Back to Puzzles",
                onClick = onClose
            )
        }
    }
}

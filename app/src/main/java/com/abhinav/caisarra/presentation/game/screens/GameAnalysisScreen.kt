package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.model.HardcodedGameAnalysis

private val ScreenBg = Color(0xFF0B1218)
private val Card = Color(0xFF121A22)
private val Border = Color(0xFF2A3848)
private val TextMain = Color(0xFFF4F7FB)
private val Muted = Color(0xFF8FA0B3)
private val Win = Color(0xFF45E5AF)
private val Warn = Color(0xFFFFC629)

@Composable
fun GameAnalysisScreen(onBack: () -> Unit) {
    val data = HardcodedGameAnalysis
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .systemBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← BACK", color = TextMain, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Text("Game Analysis", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(data.summary, color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AnalysisStat("ACCURACY", "${data.accuracyPercent}%", Win, Modifier.weight(1f))
                AnalysisStat("BRILLIANT", data.brilliantCount.toString(), Win, Modifier.weight(1f))
                AnalysisStat("BEST", data.bestMoveCount.toString(), TextMain, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AnalysisStat("INACCURACY", data.inaccuracyCount.toString(), Warn, Modifier.weight(1f))
                AnalysisStat("MISTAKE", data.mistakeCount.toString(), Muted, Modifier.weight(1f))
                AnalysisStat("BLUNDER", data.blunderCount.toString(), Muted, Modifier.weight(1f))
            }
            Spacer(Modifier.height(22.dp))
            Text("KEY MOMENTS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            data.notes.forEach { note ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(Card, RoundedCornerShape(16.dp))
                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(note.ply, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            note.tag.uppercase(),
                            color = if (note.tag == "Inaccuracy") Warn else Win,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(note.comment, color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun AnalysisStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

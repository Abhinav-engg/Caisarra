package com.abhinav.caisarra.presentation.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage

@Composable
fun PlayWithFriendScreen(
    onCreateMatchLink: () -> Unit,
    onBack: () -> Unit
) {
    SetupPage {
        SetupHeader("Play with a Friend", onBack)
        Spacer(Modifier.height(30.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            placeholder = { Text("Search friends by username or rating...", color = SetupColors.Secondary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = SetupColors.Secondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color(0xFF101925),
                disabledBorderColor = Color(0xFF26384D),
                disabledTextColor = SetupColors.Text,
                disabledPlaceholderColor = SetupColors.Secondary,
                disabledLeadingIconColor = SetupColors.Secondary
            )
        )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SetupColors.Card, RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF26384D), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text("Create Challenge Link", color = SetupColors.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            GeneralButton("Create Match Link", onCreateMatchLink)
        }
    }
}

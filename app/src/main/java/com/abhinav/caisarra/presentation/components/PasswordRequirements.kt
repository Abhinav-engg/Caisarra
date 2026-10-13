package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caisaara.ui.theme.Feildbackground
import com.caisaara.ui.theme.EmeraldNormal
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.SubtleText

@Composable
fun PasswordRequirements(
    password: String,
    isPasswordFocused: Boolean,
    modifier: Modifier = Modifier
) {
    val hasLength = password.length in 8..16

    val hasSpecial = password.any {
        !it.isLetterOrDigit() && !it.isWhitespace()
    }

    val hasNumber = password.any { it.isDigit() }
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasNoSpaces = !password.any { it.isWhitespace() }

    val remainingRequirements = listOf(
        "8–16 Characters" to hasLength,
        "Special character" to hasSpecial,
        "Number" to hasNumber,
        "Uppercase letter" to hasUppercase,
        "Lowercase letter" to hasLowercase,
        "No spaces" to hasNoSpaces
    ).filter { (_, fulfilled) -> !fulfilled }
    if (password.isEmpty() || remainingRequirements.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Feildbackground,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = RedNormal,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = "Password Requirements",
            color = SubtleText,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))
        remainingRequirements.forEach { (requirement, _) ->
            Text(
                text = "• $requirement",
                color = RedNormal,
                fontSize = 12.sp
            )
        }
    }
}

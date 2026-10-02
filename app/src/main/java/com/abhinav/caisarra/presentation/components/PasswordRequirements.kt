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
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.SubtleText
import com.caisaara.ui.theme.BorderColor

@Composable
fun PasswordRequirements(
    password: String,
    modifier: Modifier = Modifier
) {
    if (password.isEmpty()) return

    val hasLength =
        password.length in 8..16

    val hasSpecial =
        password.any { !it.isLetterOrDigit() && !it.isWhitespace() }

    val hasNumber =
        password.any { it.isDigit() }

    val hasUppercase =
        password.any { it.isUpperCase() }

    val hasLowercase =
        password.any { it.isLowerCase() }

    val hasNoSpaces =
        !password.any { it.isWhitespace() }

    val allValid =
        hasLength && hasSpecial &&
                hasNumber && hasUppercase &&
                hasLowercase && hasNoSpaces

    val borderColor =
        if (allValid) BorderColor else RedNormal

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Feildbackground,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp,
                vertical = 8.dp
            )
    ) {

        Text(text = "Password must include:",
            color = SubtleText,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        RequirementText(text = "8–16 Characters",
            fulfilled = hasLength
        )

        RequirementText(text = "Special character",
            fulfilled = hasSpecial
        )

        RequirementText(text = "Number",
            fulfilled = hasNumber
        )

        RequirementText(text = "Uppercase letter",
            fulfilled = hasUppercase
        )

        RequirementText(text = "Lowercase letter",
            fulfilled = hasLowercase
        )

        RequirementText(
            text = "No spaces",
            fulfilled = hasNoSpaces
        )
    }
}

@Composable
private fun RequirementText(
    text: String,
    fulfilled: Boolean
) {
    Text(text = "• $text",
        color = if (fulfilled) SubtleText else RedNormal,
        fontSize = 12.sp
    )
}


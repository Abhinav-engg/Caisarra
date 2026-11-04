package com.abhinav.caisarra.presentation.screens.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
private val EditBackground = Color(0xFF080D13)

private val EditCard = Color(0xFF151F2E)

private val EditCardSecondary = Color(0xFF0F1925)

private val EditCyan = Color(0xFF08D5E5)

private val EditTeal = Color(0xFF20B9B0)

private val EditWhite = Color(0xFFF3F6FC)

private val EditMuted = Color(0xFF9BAABD)

private val EditBorder = Color(0xFF29384A)

private val EditError = Color(0xFFFF7777)


@Composable
 fun ReadOnlyProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EditCardSecondary)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, color = EditMuted, fontSize = 12.sp)
        Text(
            value.ifBlank { "Not available" },
            color = EditWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
 fun EditProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = false,
    supportingText: String? = null,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        supportingText = supportingText?.let { text ->
            { Text(text, color = EditMuted) }
        },
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = EditWhite,
            unfocusedTextColor = EditWhite,
            focusedBorderColor = EditCyan,
            unfocusedBorderColor = EditBorder,
            focusedLabelColor = EditCyan,
            unfocusedLabelColor = EditMuted,
            cursorColor = EditCyan,
            focusedContainerColor = EditBackground,
            unfocusedContainerColor = EditBackground
        )
    )
}

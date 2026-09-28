package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.GeneralButton


@Composable
fun VerifyScreen(
    onContinue: (String) -> Boolean = { false },
    onResendCode: () -> Unit = {}
) {
    var otp by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val isCodeComplete = otp.length == 6
    AuthCard(
        cardHeight = 350.dp
    ) {

        AuthHeader(
            "VERIFY",
            "Enter the 6-digit verification code."
        )
        Spacer(modifier = Modifier.height(26.dp))
        Text(text = "Enter Code",
            color = Color(0xFFB5C0CC),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = otp,
            onValueChange = { value ->
                val newOtp = value
                    .filter {
                        it.isDigit()
                    }
                    .take(6)
                otp = newOtp
                isError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),


            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),

            textStyle = TextStyle(color = Color.Transparent),

            cursorBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent
                    )
                ),


            singleLine = true,
            decorationBox = { _ ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {
                    repeat(6) { index ->
                        val digit = if (index < otp.length) {
                                otp[index].toString()
                            } else {
                                ""
                            }

                        Box(modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .border(
                                    width = 1.dp,
                                    color =
                                        if (isError) {
                                            Color(0xFFFF3344)
                                        } else {
                                            Color(0xFF24333F)
                                        },
                                    shape =
                                        RoundedCornerShape(
                                            9.dp
                                        )
                                )
                                .background(
                                    Color(0xFF0B1016),

                                    RoundedCornerShape(
                                        9.dp
                                    )
                                ),

                            contentAlignment =
                                Alignment.Center
                        ) {
                            if (
                                isError &&
                                digit.isNotEmpty()
                            ) {
                                Box(modifier = Modifier
                                        .size(5.dp)
                                        .background(
                                            Color(0xFFFF3344),
                                            RoundedCornerShape(
                                                50
                                            )
                                        )
                                )

                            } else {
                                Text(text = digit,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        )
        Row(modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isError) {
                Text(text = "Invalid Code! Please retry.",
                    color = Color(0xFFFF3344),
                    fontSize = 11.sp
                )

            } else {
                Spacer(modifier = Modifier.size(1.dp))
            }
            Text(text = "Resend Code",
                color = Color(0xFF00D5E9),
                fontSize = 12.sp,
                modifier = Modifier.clickable {
                    otp = ""
                    isError = false
                    onResendCode()
                }
            )
        }
        Spacer(modifier = Modifier.height(30.dp))

        GeneralButton(
            text = "Continue",
            onClick = {
                val isValid = onContinue(otp)
                if (!isValid) {
                    isError = true
                }
            },

            enabled = isCodeComplete
        )
    }
}
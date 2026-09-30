package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.viewmodel.VerifyViewModel
import com.abhinav.caisarra.ui.theme.VerifyCardHeight
import com.caisaara.ui.theme.BorderColor
import com.caisaara.ui.theme.Feildbackground
import com.caisaara.ui.theme.Lablecolor
import com.caisaara.ui.theme.TextColor
import kotlinx.coroutines.launch

private val ErrorRed = Color(0xFFFF3344)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerifyScreen(
    viewModel: VerifyViewModel,
    onVerified: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val paste: () -> Unit = {
        scope.launch {
            val text = clipboard.getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()
            if (text != null) viewModel.onOtpChange(text)
        }
    }

    AuthCard(cardHeight = VerifyCardHeight) {
        AuthHeader("VERIFY", "Enter the 6-digit verification code.")

        Text(
            text = "Enter Code",
            style = MaterialTheme.typography.titleMedium,
            color = Lablecolor,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = state.otp,
            onValueChange = viewModel::onOtpChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        propagateMinConstraints = true
                    ) {
                        innerTextField()
                    }
                    Row(
                        modifier = Modifier.combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { focusRequester.requestFocus() },
                            onLongClick = paste
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(6) { index ->
                            OtpBox(
                                digit = state.otp.getOrNull(index)?.toString().orEmpty(),
                                isError = state.error != null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = state.error.orEmpty(),
                color = ErrorRed,
                fontSize = 11.sp,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            if (state.resendSeconds > 0) {
                Text(
                    text = buildAnnotatedString {
                        append("Resend code in ")
                        withStyle(SpanStyle(color = Color(0xFF00D5E9))) {
                            append("00:%02d".format(state.resendSeconds))
                        }
                    },
                    color = Color(0xFF82909E),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                Text(
                    text = "Resend Code",
                    color = Color(0xFF00D5E9),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { viewModel.resend() }
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        GeneralButton(
            text = if (state.isLoading) "Please wait..." else "Continue",
            onClick = { viewModel.verify(onVerified) },
            enabled = state.canSubmit
        )
    }
}

@Composable
private fun OtpBox(digit: String, isError: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .background(Feildbackground, shape)
            .border(1.dp, if (isError) ErrorRed else BorderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        if (digit.isNotEmpty()) {
            if (isError) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(ErrorRed, CircleShape)
                )
            } else {
                Text(text = digit, color = TextColor, fontSize = 18.sp)
            }
        }
    }
}
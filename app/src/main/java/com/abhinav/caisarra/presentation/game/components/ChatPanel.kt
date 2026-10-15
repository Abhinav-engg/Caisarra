package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.ChatMessageDto
import com.abhinav.caisarra.core.ui.components.clearFocusOnTap

private val Accent = Color(0xFF27C7E8)
private val Panel = Color(0xFF0C141B)
private val Border = Color(0xFF34434B)
private val Muted = Color(0xFF98A7B0)
private val OtherBubble = Color(0xFF1B222A)
private val MyBubble = Color(0xFF0E2D35)
private val QuickReplies = listOf("Good luck! 🍀", "Well played 👏", "Nice move! ♟")

@Composable
fun ChatPanel(
    messages: List<ChatMessageDto>,
    myUserId: Long?,
    muted: Boolean,
    onToggleMute: () -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    fun submit() {
        val message = text.trim()
        if (message.isEmpty()) return
        onSend(message)
        text = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Panel)
            .clearFocusOnTap()
    ) {
        ChatHeader(muted = muted, onToggleMute = onToggleMute)
        QuickReplyRow(onReply = { text = it })

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (messages.isEmpty()) {
                Text(
                    text = "No messages yet",
                    color = Muted,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    items(items = messages, key = { it.id }) { message ->
                        ChatMessage(message = message, isMine = message.senderId == myUserId)
                    }
                }
            }
        }

        ChatComposer(text = text, onTextChange = { if (it.length <= 500) text = it }, onSend = ::submit)
    }
}

@Composable
private fun ChatHeader(muted: Boolean, onToggleMute: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHAT",
                color = Color(0xFFE5EAF0),
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleMute) {
                Icon(
                    imageVector = if (muted) Icons.Outlined.NotificationsOff else Icons.Outlined.Notifications,
                    contentDescription = if (muted) "Unmute chat" else "Mute chat",
                    tint = Muted
                )
            }
            Text(text = "⋮", color = Muted, fontSize = 30.sp, modifier = Modifier.padding(start = 12.dp))
        }
        Row(
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Game Chat",
                color = Accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (muted) {
                Text(
                    text = "Muted",
                    color = Muted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border))
    }
}

@Composable
private fun QuickReplyRow(onReply: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "QUICK:", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        QuickReplyChip(text = QuickReplies[0], onClick = { onReply(QuickReplies[0]) })
        QuickReplyChip(text = QuickReplies[1], onClick = { onReply(QuickReplies[1]) })
        QuickReplyChip(text = QuickReplies[2], onClick = { onReply(QuickReplies[2]) })
    }
}

@Composable
private fun QuickReplyChip(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFD2D9DF)),
        border = BorderStroke(1.dp, Border),
        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ChatMessage(message: ChatMessageDto, isMine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isMine) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF24313D))
                        .border(1.dp, Border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 13.dp, vertical = 11.dp)
                ) {
                    Text(
                        text = message.username.orEmpty().firstOrNull()?.uppercase() ?: "?",
                        color = Accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.padding(start = 10.dp))
            }
            Text(
                text = if (isMine) "You" else message.username ?: "Opponent",
                color = if (isMine) Accent else Color(0xFFE2E7EC),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            MessageTime(message.createdAt)
        }
        Box(
            modifier = Modifier
                .padding(top = 8.dp, start = if (isMine) 0.dp else 64.dp)
                .widthIn(max = 480.dp)
                .background(if (isMine) MyBubble else OtherBubble, RoundedCornerShape(22.dp))
                .border(1.dp, if (isMine) Color(0xFF187A90) else Border, RoundedCornerShape(22.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(text = message.message, color = Color(0xFFDDE4EA), fontSize = 18.sp, lineHeight = 25.sp)
        }
    }
}

@Composable
private fun MessageTime(createdAt: String) {
    val time = createdAt.substringAfter('T', "").take(5)
    if (time.isNotEmpty()) {
        Text(text = time, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun ChatComposer(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1117))
            .border(1.dp, Border)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message...", color = Muted, fontSize = 17.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                cursorColor = Accent
            )
        )
        Button(
            onClick = onSend,
            enabled = text.isNotBlank(),
            modifier = Modifier.height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Color(0xFF06212A),
                disabledContainerColor = Color(0xFF1A505C),
                disabledContentColor = Color(0xFF8EA1A8)
            ),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            Text(text = "Send  ▷", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
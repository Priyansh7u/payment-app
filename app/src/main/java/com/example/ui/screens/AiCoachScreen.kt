package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatMessage
import com.example.ui.PaymentViewModel
import com.example.ui.theme.PhonePePurple
import com.example.ui.theme.PhonePePurpleLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(
    viewModel: PaymentViewModel
) {
    val conversation by viewModel.aiConversation.collectAsState()
    val isGenerating by viewModel.isAiGenerating.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }

    // Scroll to bottom when new messages are added
    LaunchedEffect(conversation.size) {
        if (conversation.isNotEmpty()) {
            listState.animateScrollToItem(conversation.size - 1)
        }
    }

    val quickQueries = listOf(
        "Analyze my spending habits",
        "Explain UPI PIN safety rules",
        "What are UPI transfer limits?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PhonePePurple),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Coach",
                            tint = Color.Yellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Smart Spend AI Assistant", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF3F4F6))
        ) {
            // Chat message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(conversation) { chat ->
                    ChatBubble(message = chat)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = PhonePePurple
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Coach is analyzing your transactions...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Quick suggestion chips deck
            if (!isGenerating) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickQueries) { query ->
                        SuggestionChip(
                            onClick = {
                                viewModel.askAiCoach(query)
                            },
                            label = { Text(query, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // Text input panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Ask about budgets, safety, or spendings...", fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF1F2937),
                            unfocusedTextColor = Color(0xFF1F2937)
                        )
                    )

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.askAiCoach(textInput)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (textInput.isNotBlank() && !isGenerating) PhonePePurple else Color(0xFFE5E7EB))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank() && !isGenerating) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) Color(0xFFEEF2FF) else Color.White
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.message,
                    fontSize = 13.sp,
                    color = if (isUser) Color(0xFF1E1B4B) else Color(0xFF1F2937),
                    lineHeight = 18.sp
                )
            }
            Text(
                text = message.sender,
                fontSize = 9.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, end = 4.dp)
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BankAccountEntity

@Composable
fun UpiPinDialog(
    recipientName: String,
    recipientUpi: String,
    amount: String,
    fundingAccount: BankAccountEntity?,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val maxPinLength = 4

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1014).copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = Color(0xFF1E212A),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .navigationBarsPadding()
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NPCI SECURE UPI PAYMENT",
                            color = Color(0xFF9E9E9E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Paying to $recipientName",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color(0xFF2C313D), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close PIN",
                            tint = Color.White
                        )
                    }
                }

                Divider(color = Color(0xFF2D323E))

                // Payment Info Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .background(Color(0xFF272C38), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Debit From: ${fundingAccount?.bankName ?: "Linked Account"}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = fundingAccount?.accountNumber ?: "",
                            color = Color(0xFFA5B4FC),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "₹$amount",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ENTER UPI PIN TEXT
                Text(
                    text = "ENTER 4-DIGIT UPI PIN",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // PIN Indicator Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(maxPinLength) { index ->
                        val active = index < pin.length
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (active) Color(0xFF6366F1) else Color(0xFF4B5563))
                        )
                    }
                }

                // Custom Numeric Keypad
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161922))
                        .padding(vertical = 12.dp)
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("BACK", "0", "OK")
                    )

                    keys.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clickable {
                                            when (key) {
                                                "BACK" -> {
                                                    if (pin.isNotEmpty()) {
                                                        pin = pin.dropLast(1)
                                                    }
                                                }
                                                "OK" -> {
                                                    if (pin.length == maxPinLength) {
                                                        onSubmit(pin)
                                                    }
                                                }
                                                else -> {
                                                    if (pin.length < maxPinLength) {
                                                        pin += key
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (key) {
                                        "BACK" -> {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Backspace",
                                                tint = Color.White
                                            )
                                        }
                                        "OK" -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        color = if (pin.length == maxPinLength) Color(0xFF22C55E) else Color(0xFF374151),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Confirm",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = key,
                                                color = Color.White,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Security Banner footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F111A))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unified Payments Interface (UPI) • 100% Secure NPCI Node",
                        color = Color(0xFF6B7280),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

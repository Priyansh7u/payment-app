package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.PhonePeGreen
import com.example.ui.theme.PhonePePurple
import com.example.ui.theme.PhonePeRed
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentResultScreen(
    status: String, // AUTHENTICATING, PROCESSING, SUCCESS, FAILED
    errorMessage: String,
    recipientName: String,
    amount: String,
    transactionId: String,
    fundingBankName: String,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    category: String = "Transfer"
) {
    Dialog(
        onDismissRequest = { if (status == "SUCCESS" || status == "FAILED") onDone() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = (status == "SUCCESS" || status == "FAILED"),
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .safeDrawingPadding()
        ) {
            when (status) {
                "AUTHENTICATING", "PROCESSING" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(120.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(80.dp),
                                color = PhonePePurple,
                                strokeWidth = 6.dp
                            )
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secure",
                                tint = PhonePePurple,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (status == "AUTHENTICATING") "Verifying secure PIN..." else "Processing transaction...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Please do not press back or close the application.",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                "SUCCESS" -> {
                    if (category == "Tax") {
                        TaxSuccessLayout(
                            amount = amount,
                            recipientName = recipientName,
                            transactionId = transactionId,
                            onDone = onDone
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF0FDF4)) // Soft Green
                                .padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                        // Success checkmark and animation anchor
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(PhonePeGreen, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = Color.White,
                                    modifier = Modifier.size(56.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "₹$amount",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )

                            Text(
                                text = "Paid successfully to $recipientName",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Detailed Digital Receipt Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "TRANSACTION DETAILS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B5563),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                ReceiptRow(label = "Recipient UPI", value = recipientName)
                                ReceiptRow(label = "Source Bank", value = fundingBankName)
                                ReceiptRow(label = "Transaction ID", value = transactionId)
                                ReceiptRow(
                                    label = "Date & Time",
                                    value = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                                )
                                ReceiptRow(label = "Status", value = "SUCCESS", isStatus = true)
                            }
                        }

                        // Promo/Reward visual accent
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Reward",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "You've won a Scratch Card!",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "Scratched card added to your Rewards section.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        // Action Button
                        Button(
                            onClick = onDone,
                            colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "DONE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    }
                }

                "FAILED" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFEF2F2)) // Soft Red
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(PhonePeRed, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Failed",
                                    tint = Color.White,
                                    modifier = Modifier.size(56.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "₹$amount",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )

                            Text(
                                text = "Payment Failed",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = errorMessage,
                                fontSize = 14.sp,
                                color = Color(0xFF7F1D1D),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                ReceiptRow(label = "Recipient", value = recipientName)
                                ReceiptRow(label = "Requested Amount", value = "₹$amount")
                                ReceiptRow(label = "Failure Diagnostic", value = errorMessage)
                                ReceiptRow(label = "Technical Code", value = "UPI_RESP_SEC_ERR")
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "TRY AGAIN",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = onDone,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CANCEL & GO TO HOME",
                                    color = PhonePePurple,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, isStatus: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        if (isStatus) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 11.sp,
                    color = PhonePeGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
    }
}

@Composable
fun TaxSuccessLayout(
    amount: String,
    recipientName: String,
    transactionId: String,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F9D58)) // Solid Emerald/PhonePe Tax Success Green
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // Leave space for sticky footer
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Checkmark in white circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, CircleShape)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF0F9D58),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Successful Amount Text
            Text(
                text = "Tax Payment of ₹$amount is successful.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // CIN Info Subtext
            Text(
                text = "The CIN number for this payment will be updated within 5 days.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // VIEW DETAILS & SPLIT EXPENSES buttons side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* View details flow */ },
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "VIEW DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                OutlinedButton(
                    onClick = { /* Split expenses flow */ },
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "SPLIT EXPENSES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Beautiful High-Fidelity Floor Cleaning/Vacuuming Advertisement Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Image-like simulated layout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE0F2FE), Color(0xFFF0FDFA))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFEDD5), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SUPER HOME DEALS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEA580C)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Get Spotless Floors in Minutes!",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Up to 50% Off on Smart Vacuum Cleaners & Mops",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }

                            // Cleaner / Mop Visual Representation using Icons
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Mop Promo",
                                    tint = PhonePePurple,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Lower details bar of advertisement
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Highly Rated on PhonePe Shop",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }

                        Button(
                            onClick = { /* Buy Now promo link */ },
                            colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Buy Now",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Pinned white footer with full-width DONE button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "DONE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}


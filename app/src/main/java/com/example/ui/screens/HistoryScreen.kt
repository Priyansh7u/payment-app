package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TransactionEntity
import com.example.ui.PaymentViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: PaymentViewModel
) {
    val transactions by viewModel.allTransactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, SEND, RECEIVE, FAILED

    // Active detail receipt dialog state
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = transactions.filter { t ->
        val matchesSearch = t.title.contains(searchQuery, ignoreCase = true) ||
                t.category.contains(searchQuery, ignoreCase = true) ||
                t.upiId.contains(searchQuery, ignoreCase = true)
        
        val matchesFilter = when (selectedFilter) {
            "SEND" -> t.type == "SEND" || t.type == "BILL_PAY" || t.type == "RECHARGE"
            "RECEIVE" -> t.type == "RECEIVE"
            "FAILED" -> t.status == "FAILED"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PhonePePurple),
                title = { Text("Transaction History", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF3F4F6))
        ) {
            // Search Bar & Filter chips
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, category, or UPI ID...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhonePePurpleLight,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row of filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All Transactions", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "SEND",
                            onClick = { selectedFilter = "SEND" },
                            label = { Text("Debits", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "RECEIVE",
                            onClick = { selectedFilter = "RECEIVE" },
                            label = { Text("Credits", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "FAILED",
                            onClick = { selectedFilter = "FAILED" },
                            label = { Text("Failed", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Transaction items column
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "Empty list",
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text("No matching transactions found.", color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Try clearing search queries or active filters.", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredTransactions) { t ->
                        TransactionItemRow(
                            transaction = t,
                            onClick = { selectedTransaction = t }
                        )
                        Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    // --- TRANSACTION DETAIL DIGITAL INVOICE DIALOG ---
    if (selectedTransaction != null) {
        val t = selectedTransaction!!
        val isDebit = t.amount < 0
        val isSuccess = t.status == "SUCCESS"

        Dialog(onDismissRequest = { selectedTransaction = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UPI DIGITAL RECEIPT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            letterSpacing = 1.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = t.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) PhonePeGreen else PhonePeRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = t.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = t.upiId,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Amount paid", color = Color.Gray, fontSize = 13.sp)
                        Text(
                            text = "₹" + "%,.2f".format(kotlin.math.abs(t.amount)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDebit) Color(0xFF1F2937) else PhonePeGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Transaction Type", color = Color.Gray, fontSize = 13.sp)
                        Text(t.type, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Category Group", color = Color.Gray, fontSize = 13.sp)
                        Text(t.category, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Executed On", color = Color.Gray, fontSize = 13.sp)
                        val date = Date(t.timestamp)
                        val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
                        Text(formattedDate, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Funding Reference", color = Color.Gray, fontSize = 13.sp)
                        Text(t.subtitle.substringAfter("from ").substringAfter("successfully "), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { selectedTransaction = null },
                        colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DONE", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val isDebit = transaction.amount < 0
    val isSuccess = transaction.status == "SUCCESS"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (transaction.category) {
                            "Transfer" -> Color(0xFFF3E8FF)
                            "Mobile" -> Color(0xFFE8F5E9)
                            "Electricity" -> Color(0xFFFFF9C4)
                            "Reward" -> Color(0xFFE0F7FA)
                            else -> Color(0xFFECEFF1)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.category) {
                        "Transfer" -> Icons.Default.Send
                        "Mobile" -> Icons.Default.PhoneAndroid
                        "Electricity" -> Icons.Default.Lightbulb
                        "Reward" -> Icons.Default.CardGiftcard
                        else -> Icons.Default.ReceiptLong
                    },
                    contentDescription = null,
                    tint = when (transaction.category) {
                        "Transfer" -> PhonePePurple
                        "Mobile" -> PhonePeGreen
                        "Electricity" -> PhonePeOrange
                        "Reward" -> Color(0xFF00B8D4)
                        else -> Color.DarkGray
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Formatted simple date
                val formattedDate = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }

        // Amount & Status Icon
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (if (isDebit) "- ₹" else "+ ₹") + "%,.2f".format(kotlin.math.abs(transaction.amount)),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (isDebit) Color(0xFF1F2937) else PhonePeGreen
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isSuccess) PhonePeGreen else PhonePeRed,
                    modifier = Modifier.size(14.dp)
                )
            }
            if (!isSuccess) {
                Text(
                    text = "FAILED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhonePeRed
                )
            }
        }
    }
}

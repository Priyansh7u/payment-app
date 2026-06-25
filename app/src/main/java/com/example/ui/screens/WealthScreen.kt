package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BankAccountEntity
import com.example.ui.PaymentViewModel
import com.example.ui.components.UpiPinDialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WealthScreen(
    viewModel: PaymentViewModel
) {
    val bankAccounts by viewModel.allBankAccounts.collectAsState()
    val scope = rememberCoroutineScope()

    // Dialog triggering states
    var showAddBankDialog by remember { mutableStateOf(false) }
    var selectedCheckAccount by remember { mutableStateOf<BankAccountEntity?>(null) }
    var showBalanceDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var balanceResultText by remember { mutableStateOf("") }
    var isCheckingBalance by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PhonePePurple),
                title = { Text("Bank Accounts & Wealth", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF0F3F8)) // Geometric slate-50 background
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp), // geometric balance rounded-3xl
                colors = CardDefaults.cardColors(containerColor = PhonePePurpleDark),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Active UPI Accounts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Your bank accounts are linked to UPI and ready for transfers.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bank Accounts section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LINKED BANK ACCOUNTS",
                    color = Color(0xFF94A3B8), // slate-400
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Button(
                    onClick = { showAddBankDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD NEW", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            bankAccounts.forEach { account ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp), // geometric balance rounded-3xl
                    border = if (account.isPrimary) BorderStroke(1.5.dp, PhonePePurple) else BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(PhonePePurple.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = PhonePePurple,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = account.bankName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                    if (account.isPrimary) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE0F2FE))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PRIMARY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Savings A/c No. ${account.accountNumber}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "UPI ID: ${account.upiId}",
                                    fontSize = 11.sp,
                                    color = PhonePePurple,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Check balance button
                        Button(
                            onClick = {
                                selectedCheckAccount = account
                                enteredPin = ""
                                balanceResultText = ""
                                showBalanceDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E8FF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Check Balance", color = PhonePePurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Extra security tips section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified",
                        tint = PhonePeGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("NPCI Encryption Active", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PhonePeGreen)
                        Text("All linked banks operate under secure 256-bit AES standard encryption. Your PIN is never saved.", fontSize = 11.sp, color = Color(0xFF1B5E20))
                    }
                }
            }
        }
    }

    // --- CHECK BALANCE UPI PIN DIALOGUE ---
    if (showBalanceDialog && selectedCheckAccount != null) {
        UpiPinDialog(
            recipientName = selectedCheckAccount!!.bankName,
            recipientUpi = "Balance Inquiry",
            amount = "0.00",
            fundingAccount = selectedCheckAccount,
            onDismiss = { showBalanceDialog = false },
            onSubmit = { pin ->
                scope.launch {
                    isCheckingBalance = true
                    delay(1200) // Simulated secure routing delay
                    isCheckingBalance = false
                    if (selectedCheckAccount!!.upiPin == pin) {
                        balanceResultText = "SUCCESS: ₹%,.2f".format(selectedCheckAccount!!.balance)
                    } else {
                        balanceResultText = "ERROR: Incorrect UPI PIN. Balance access denied."
                    }
                }
            }
        )
    }

    // BALANCE DISPLAY RESULT POPUP
    if (balanceResultText.isNotEmpty()) {
        Dialog(onDismissRequest = { balanceResultText = "" }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isSuccess = balanceResultText.startsWith("SUCCESS")
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSuccess) PhonePeGreen else PhonePeRed,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isSuccess) "Account Balance Inquiry" else "Authorization Error",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isSuccess) selectedCheckAccount?.bankName ?: "Bank" else "Declined",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isSuccess) balanceResultText.substringAfter("SUCCESS: ") else balanceResultText.substringAfter("ERROR: "),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSuccess) PhonePePurple else Color.Red
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { balanceResultText = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE", color = Color.White)
                    }
                }
            }
        }
    }

    // --- ADD BANK ACCOUNT DIALOG ---
    if (showAddBankDialog) {
        val standardBanks = listOf("HDFC Bank", "ICICI Bank", "SBI", "Axis Bank", "Kotak Bank", "Punjab National Bank")
        var enteringBankAmount by remember { mutableStateOf("5000.00") }

        Dialog(onDismissRequest = { showAddBankDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Link New Bank Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "Select a bank to link using your registered mobile number via UPI auto-SMS.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = enteringBankAmount,
                        onValueChange = { enteringBankAmount = it },
                        label = { Text("Initial Deposit Balance (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Popular Banks:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    standardBanks.forEach { bank ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    showAddBankDialog = false
                                    viewModel.addBankAccount(
                                        bankName = bank,
                                        balance = enteringBankAmount.toDoubleOrNull() ?: 5000.00
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                            border = BorderStroke(1.dp, Color(0xFFE9D5FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PhonePePurple)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(bank, fontWeight = FontWeight.SemiBold, color = Color(0xFF3E146C))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddBankDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

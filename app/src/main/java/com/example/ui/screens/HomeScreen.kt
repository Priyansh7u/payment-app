package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BankAccountEntity
import com.example.data.SavedBillEntity
import com.example.ui.PaymentViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PaymentViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToWealth: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val bankAccounts by viewModel.allBankAccounts.collectAsState()
    val bills by viewModel.allBills.collectAsState()
    val scope = rememberCoroutineScope()

    // Dialog trigger states
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferDialogType by remember { mutableStateOf("") } // CONTACT, ACCOUNT, SELF

    // Transfer inputs
    var upiInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var fromAccountSelected by remember { mutableStateOf<BankAccountEntity?>(null) }
    var toAccountSelected by remember { mutableStateOf<BankAccountEntity?>(null) }

    // Bill picker dialog state
    var showBillPicker by remember { mutableStateOf(false) }
    var selectedBillCategory by remember { mutableStateOf("") }

    // Tax flow states
    var showTaxFlow by remember { mutableStateOf(false) }
    var taxStep by remember { mutableStateOf(1) }
    var selectedTaxType by remember { mutableStateOf("Self Assessment Tax (300)") }
    var selectedAssessmentYear by remember { mutableStateOf("2023-24") }
    var financialYear by remember { mutableStateOf("2022-23") }
    var panInput by remember { mutableStateOf("ABCDE1234F") }
    var taxAmountInput by remember { mutableStateOf("20000") }

    // Init first selected bank
    LaunchedEffect(bankAccounts) {
        if (fromAccountSelected == null && bankAccounts.isNotEmpty()) {
            fromAccountSelected = bankAccounts.find { it.isPrimary } ?: bankAccounts.first()
        }
        if (toAccountSelected == null && bankAccounts.size > 1) {
            toAccountSelected = bankAccounts.find { !it.isPrimary } ?: bankAccounts[1]
        }
    }

    Scaffold(
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(Color(0xFFF0F3F8)) // Geometric Balance slate background
                .verticalScroll(rememberScrollState())
        ) {
            // --- GEOMETRIC BALANCE HIGH-FIDELITY HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PhonePePurple,
                        shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
                    )
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                ) {
                    // Row 1: Profile Avatar, Location, Scan QR, Help
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // User Avatar
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "U",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Location
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Indiranagar, Bengaluru",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Location",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Work • Corporate Tower",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onNavigateToScan,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan QR",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { /* Help */ },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Help",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Translucent Search Bar (Geometric Balance Theme)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .clickable { onNavigateToHistory() } // Deep links to transaction history for search!
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search by name, number or UPI ID",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // content overlapping layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-16).dp)
                    .padding(horizontal = 4.dp)
            ) {
                // Quick Hero Promo Banner Slider Simulation
                BannerSlider()

                // 1. Transfer Money Module
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(24.dp), // geometric balance very rounded-3xl
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)), // subtle slate-100 border
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TRANSFER MONEY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8), // slate-400
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TransferButton(
                            icon = Icons.Default.PersonOutline,
                            label = "To Contact",
                            onClick = {
                                transferDialogType = "CONTACT"
                                upiInput = ""
                                nameInput = ""
                                amountInput = ""
                                showTransferDialog = true
                            }
                        )
                        TransferButton(
                            icon = Icons.Default.AccountBalance,
                            label = "To Account",
                            onClick = {
                                transferDialogType = "ACCOUNT"
                                upiInput = "" // holds account number here
                                nameInput = "" // holds bank name here
                                amountInput = ""
                                showTransferDialog = true
                            }
                        )
                        TransferButton(
                            icon = Icons.Default.SwapHoriz,
                            label = "To Self",
                            onClick = {
                                transferDialogType = "SELF"
                                amountInput = ""
                                showTransferDialog = true
                            }
                        )
                        TransferButton(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Check Balance",
                            onClick = onNavigateToWealth
                        )
                    }
                }
            }

            // Quick Wallet info strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .background(Color(0xFFEEF2FF), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color(0xFFC7D2FE)), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Wallet",
                            tint = PhonePePurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PhonePe UPI Lite: Fast transfers with Zero PIN!",
                            fontSize = 11.sp,
                            color = Color(0xFF1E1B4B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "ACTIVATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PhonePePurple,
                        modifier = Modifier.clickable { /* Activate flow */ }
                    )
                }
            }

            // 2. Recharge & Pay Bills Module
            // 2. Recharge & Pay Bills Module
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RECHARGE & PAY BILLS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8), // slate-400
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UtilityButton(
                            icon = Icons.Default.PhoneAndroid,
                            label = "Mobile",
                            onClick = {
                                selectedBillCategory = "Mobile"
                                showBillPicker = true
                            }
                        )
                        UtilityButton(
                            icon = Icons.Default.Lightbulb,
                            label = "Electricity",
                            onClick = {
                                selectedBillCategory = "Electricity"
                                showBillPicker = true
                            }
                        )
                        UtilityButton(
                            icon = Icons.Default.Tv,
                            label = "DTH",
                            onClick = {
                                selectedBillCategory = "DTH"
                                showBillPicker = true
                            }
                        )
                        UtilityButton(
                            icon = Icons.Default.LocalGasStation,
                            label = "LPG Gas",
                            onClick = {
                                selectedBillCategory = "Gas"
                                showBillPicker = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UtilityButton(
                            icon = Icons.Default.Home,
                            label = "Rent Pay",
                            onClick = {
                                selectedBillCategory = "Rent"
                                showBillPicker = true
                            }
                        )
                        UtilityButton(
                            icon = Icons.Default.ReceiptLong,
                            label = "Loan Repay",
                            onClick = {
                                selectedBillCategory = "Loan"
                                showBillPicker = true
                            }
                        )
                        UtilityButton(
                            icon = Icons.Default.School,
                            label = "Education",
                            onClick = {
                                selectedBillCategory = "Education"
                                showBillPicker = true
                            }
                        )
                        UtilityButton(
                            icon = Icons.Default.Percent,
                            label = "Income Tax",
                            onClick = {
                                taxStep = 1
                                selectedTaxType = "Self Assessment Tax (300)"
                                selectedAssessmentYear = "2023-24"
                                financialYear = "2022-23"
                                panInput = "ABCDE1234F"
                                taxAmountInput = "20000"
                                showTaxFlow = true
                            }
                        )
                    }
                }
            }

            // 3. Smart Insured / Sponsored Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INSURANCE & INVESTMENTS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8), // slate-400
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InvestmentItem(icon = Icons.Default.DirectionsCar, label = "Car Insur.")
                        InvestmentItem(icon = Icons.Default.FavoriteBorder, label = "Health")
                        InvestmentItem(icon = Icons.Default.TrendingUp, label = "Gold Mutual")
                        InvestmentItem(icon = Icons.Default.HomeWork, label = "Home Insur.")
                    }
                }
            }

            // 4. Promotions and Safety Tips Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                border = BorderStroke(1.dp, Color(0xFFF3E8FF)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFF3E8FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield logo",
                            tint = PhonePePurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Safety Tip: Check recipient name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PhonePePurpleDark
                        )
                        Text(
                            text = "Always double check the payee name displayed before completing UPI transfers. PhonePe never asks for passwords.",
                            fontSize = 11.sp,
                            color = Color(0xFF5B21B6)
                        )
                    }
                }
            }
        }
    }
}

    // --- DIALOG 1: Transfer Money (To Contact, To Account, To Self) ---
    if (showTransferDialog) {
        Dialog(onDismissRequest = { showTransferDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = when (transferDialogType) {
                            "CONTACT" -> "Send Money to Contact"
                            "ACCOUNT" -> "Send Money to Bank Account"
                            else -> "Self Transfer Funds"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    when (transferDialogType) {
                        "CONTACT" -> {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Recipient Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = upiInput,
                                onValueChange = { upiInput = it },
                                label = { Text("UPI ID (e.g. name@okhdfcbank)") },
                                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "ACCOUNT" -> {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Account Holder Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = upiInput,
                                onValueChange = { upiInput = it },
                                label = { Text("Bank Account Number") },
                                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "SELF" -> {
                            // Source bank account selection
                            Text("Source Bank Account", fontSize = 12.sp, color = Color.Gray)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* Toggle list or pick first */ },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PhonePePurple)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(fromAccountSelected?.bankName ?: "Choose Account", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Destination Bank Account", fontSize = 12.sp, color = Color.Gray)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* Toggle list or pick second */ },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PhonePePurpleLight)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(toAccountSelected?.bankName ?: "Choose Account", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Enter Amount (₹)") },
                        leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTransferDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (amountInput.isNotBlank()) {
                                    showTransferDialog = false
                                    if (transferDialogType == "SELF") {
                                        viewModel.activeRecipientName.value = "Self Transfer"
                                        viewModel.activeRecipientUpi.value = "To ${toAccountSelected?.bankName}"
                                        viewModel.activeAmount.value = amountInput
                                        viewModel.activeCategory.value = "Transfer"
                                        // Trigger custom PIN dialog sequence
                                        viewModel.pinScreenVisible.value = true
                                        viewModel.paymentStatus.value = "INPUT"
                                    } else {
                                        val upiId = if (transferDialogType == "ACCOUNT") "${upiInput}@bank" else upiInput
                                        viewModel.initiatePayment(
                                            name = nameInput.ifBlank { "Direct Recipient" },
                                            upi = upiId.ifBlank { "recipient@upi" },
                                            amount = amountInput,
                                            category = "Transfer"
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple)
                        ) {
                            Text("PROCEED", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 2: Pending Bill Selection Picker ---
    if (showBillPicker) {
        val categoryBills = bills.filter { it.category == selectedBillCategory }

        Dialog(onDismissRequest = { showBillPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "$selectedBillCategory Biller Search",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (categoryBills.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Text("No pending bills in this category.", color = Color.Gray)
                            }
                        }
                    } else {
                        Column {
                            Text("Pending Bills found:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                            categoryBills.forEach { bill ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable {
                                            showBillPicker = false
                                            viewModel.payBill(bill)
                                        },
                                    colors = CardDefaults.cardColors(containerColor = if (bill.isPaid) Color(0xFFF3F4F6) else Color(0xFFFAF5FF)),
                                    border = BorderStroke(1.dp, if (bill.isPaid) Color.LightGray else Color(0xFFE9D5FF))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(bill.billerName, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                                            Text("No: ${bill.consumerNumber}", fontSize = 12.sp, color = Color.Gray)
                                            Text("Due Date: ${bill.dueDate}", fontSize = 11.sp, color = if (bill.isPaid) Color.Gray else PhonePeOrange)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("₹${bill.amount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PhonePePurple)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (bill.isPaid) Color(0xFFE5E7EB) else Color(0xFFFEE2E2))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (bill.isPaid) "PAID" else "PAY NOW",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (bill.isPaid) Color.Gray else Color.Red
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Simulated Search input for new manual recharge/bill
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Add New Number/Consumer ID", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    var manualInput by remember { mutableStateOf("") }
                    var manualAmountInput by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        placeholder = { Text(if (selectedBillCategory == "Mobile") "Enter 10 digit Mobile Number" else "Enter Consumer ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualAmountInput,
                        onValueChange = { manualAmountInput = it },
                        placeholder = { Text("Enter Recharge/Bill Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showBillPicker = false }) { Text("Cancel", color = Color.Gray) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (manualInput.isNotBlank() && manualAmountInput.isNotBlank()) {
                                    showBillPicker = false
                                    viewModel.initiatePayment(
                                        name = "$selectedBillCategory Bill Pay",
                                        upi = "$manualInput@$selectedBillCategory".lowercase(),
                                        amount = manualAmountInput,
                                        category = selectedBillCategory
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple)
                        ) {
                            Text("PAY")
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 3: Custom High-Fidelity Income Tax Flow ---
    if (showTaxFlow) {
        Dialog(
            onDismissRequest = { showTaxFlow = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF8FAFC)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            if (taxStep > 1) {
                                taxStep--
                            } else {
                                showTaxFlow = false 
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack, 
                                contentDescription = "Back",
                                tint = Color(0xFF1E293B)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (taxStep == 1) "New Tax Payment" else "Tax Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    if (taxStep == 1) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Select Tax Details",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { /* Select Tax Type */ }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Type of Tax Payment",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = selectedTaxType,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            if (selectedAssessmentYear == "2023-24") {
                                                selectedAssessmentYear = "2024-25"
                                                financialYear = "2023-24"
                                            } else {
                                                selectedAssessmentYear = "2023-24"
                                                financialYear = "2022-23"
                                            }
                                        }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Assessment Year (A.Y.)",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = selectedAssessmentYear,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }

                            Text(
                                text = "Financial year : $financialYear",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Enter your PAN card",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "PAN is mandatory to verify your details on Government site",
                                fontSize = 11.sp,
                                color = PhonePePurple,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = panInput,
                                onValueChange = { panInput = it.uppercase() },
                                label = { Text("Enter your PAN") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PhonePePurple,
                                    focusedLabelColor = PhonePePurple
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Where do I find my PAN details?",
                                fontSize = 12.sp,
                                color = PhonePePurple,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { /* Info Dialogue link */ }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = { 
                                    if (panInput.isNotBlank() && panInput.length >= 5) {
                                        taxStep = 2 
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "PROCESS",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color(0xFFF3E8FF), CircleShape)
                                    ) {
                                        Text(
                                            text = "PS",
                                            color = PhonePePurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = if (panInput.length >= 10) panInput.take(4) + "XXXXX" + panInput.last() else panInput,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Self Assessment Tax",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "TAX DETAILS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    TaxDetailRow("Assessment Year", selectedAssessmentYear)
                                    TaxDetailRow("Financial Year", financialYear)
                                    TaxDetailRow("PAN", panInput)
                                    TaxDetailRow("Name", "PRIYANSHU SHARMA")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Enter Tax Amount",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "₹",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        androidx.compose.foundation.text.BasicTextField(
                                            value = taxAmountInput,
                                            onValueChange = { taxAmountInput = it },
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Minimum ₹100, Maximum ₹10,00,000",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Note",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Note: The Paid Tax Amount will be updated in Govt. records. Payments post 8:00 PM will be processed on next working day.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = { 
                                    if (taxAmountInput.isNotBlank() && (taxAmountInput.toDoubleOrNull() ?: 0.0) >= 100.0) {
                                        viewModel.initiatePayment(
                                            name = "Self Government Tax",
                                            upi = "gov.tax@upi",
                                            amount = taxAmountInput,
                                            category = "Tax"
                                        )
                                        showTaxFlow = false 
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "PAY TAX",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
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
fun TaxDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
    }
}


@Composable
fun TransferButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(72.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFEEF2FF), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PhonePePurple,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun UtilityButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(72.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFECFDF5), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF059669),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun InvestmentItem(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFFFF7ED), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFEA580C),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Simulated dynamic scrolling banner
@Composable
fun BannerSlider() {
    val items = listOf(
        BannerItem("Get Up To ₹100 Cashback - Check Credit Score To Earn More! Check Now", Color(0xFF4C0E4E), Color(0xFF8C1D6E)),
        BannerItem("Flat ₹50 Cashback on BESCOM bills using HDFC Bank", Color(0xFF3E146C), Color(0xFF8149C4)),
        BannerItem("Introducing PhonePe UPI Lite - Speed transfers without PIN!", Color(0xFF0D5C3A), Color(0xFF2E7D32)),
        BannerItem("Win scratch cards up to ₹1000 on Mobile Recharges today", Color(0xFF8C1D40), Color(0xFFD32F2F))
    )

    var currentItemIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentItemIndex = (currentItemIndex + 1) % items.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        items[currentItemIndex].colorStart,
                        items[currentItemIndex].colorEnd
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIMITED OFFER",
                    color = Color.Yellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = items[currentItemIndex].title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

data class BannerItem(
    val title: String,
    val colorStart: Color,
    val colorEnd: Color
)

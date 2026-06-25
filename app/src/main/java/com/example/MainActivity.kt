package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.PaymentRepository
import com.example.ui.PaymentViewModel
import com.example.ui.PaymentViewModelFactory
import com.example.ui.components.PaymentResultScreen
import com.example.ui.components.UpiPinDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PhonePePurple

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize Room Database, DAO and Repository
                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember { PaymentRepository(database.paymentDao()) }

                // Retrieve and bind PaymentViewModel
                val viewModel: PaymentViewModel = viewModel(
                    factory = PaymentViewModelFactory(application, repository)
                )

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Reactive state observers
                val pinVisible by viewModel.pinScreenVisible.collectAsState()
                val paymentStatus by viewModel.paymentStatus.collectAsState()
                val errorMessage by viewModel.paymentErrorMessage.collectAsState()
                val recipientName by viewModel.activeRecipientName.collectAsState()
                val activeAmount by viewModel.activeAmount.collectAsState()
                val transactionId by viewModel.activeTransactionId.collectAsState()
                val selectedBankId by viewModel.selectedBankId.collectAsState()
                val bankAccounts by viewModel.allBankAccounts.collectAsState()
                val activeCategory by viewModel.activeCategory.collectAsState()

                val fundingAccount = bankAccounts.find { it.id == selectedBankId }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Polished M3 Bottom Nav Bar - Hidden on Scanner viewfinder screen
                        if (currentRoute != "SCAN") {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 0.dp,
                                modifier = Modifier.drawBehind {
                                    drawLine(
                                        color = Color(0xFFF1F5F9), // slate-100
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                        strokeWidth = 2f
                                    )
                                }
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "HOME",
                                    onClick = {
                                        navController.navigate("HOME") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(imageVector = if (currentRoute == "HOME") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                                    label = { Text("Home", fontSize = 10.sp, fontWeight = if (currentRoute == "HOME") FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PhonePePurple,
                                        selectedTextColor = PhonePePurple,
                                        indicatorColor = Color(0xFFEEF2FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "SCAN",
                                    onClick = {
                                        navController.navigate("SCAN") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Filled.QrCodeScanner, contentDescription = "Scanner") },
                                    label = { Text("Scan", fontSize = 10.sp, fontWeight = if (currentRoute == "SCAN") FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PhonePePurple,
                                        selectedTextColor = PhonePePurple,
                                        indicatorColor = Color(0xFFEEF2FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "WEALTH",
                                    onClick = {
                                        navController.navigate("WEALTH") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(imageVector = if (currentRoute == "WEALTH") Icons.Filled.AccountBalance else Icons.Outlined.AccountBalance, contentDescription = "Banks") },
                                    label = { Text("Banks", fontSize = 10.sp, fontWeight = if (currentRoute == "WEALTH") FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PhonePePurple,
                                        selectedTextColor = PhonePePurple,
                                        indicatorColor = Color(0xFFEEF2FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "AI",
                                    onClick = {
                                        navController.navigate("AI") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = "AI Assistant") },
                                    label = { Text("AI Coach", fontSize = 10.sp, fontWeight = if (currentRoute == "AI") FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PhonePePurple,
                                        selectedTextColor = PhonePePurple,
                                        indicatorColor = Color(0xFFEEF2FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "HISTORY",
                                    onClick = {
                                        navController.navigate("HISTORY") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(imageVector = if (currentRoute == "HISTORY") Icons.Filled.History else Icons.Outlined.History, contentDescription = "History") },
                                    label = { Text("History", fontSize = 10.sp, fontWeight = if (currentRoute == "HISTORY") FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PhonePePurple,
                                        selectedTextColor = PhonePePurple,
                                        indicatorColor = Color(0xFFEEF2FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "HOME",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("HOME") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToScan = { navController.navigate("SCAN") },
                                onNavigateToWealth = { navController.navigate("WEALTH") },
                                onNavigateToHistory = { navController.navigate("HISTORY") }
                            )
                        }

                        composable("SCAN") {
                            ScannerScreen(
                                viewModel = viewModel,
                                onNavigateToHome = { navController.navigate("HOME") }
                            )
                        }

                        composable("WEALTH") {
                            WealthScreen(
                                viewModel = viewModel
                            )
                        }

                        composable("AI") {
                            AiCoachScreen(
                                viewModel = viewModel
                            )
                        }

                        composable("HISTORY") {
                            HistoryScreen(
                                viewModel = viewModel
                            )
                        }
                    }

                    // --- ROOT LEVEL UPI TRANSACTION OVERLAYS ---

                    // 1. PIN Keyboard Entry Dialog
                    if (pinVisible && paymentStatus == "INPUT") {
                        UpiPinDialog(
                            recipientName = recipientName,
                            recipientUpi = viewModel.activeRecipientUpi.value,
                            amount = activeAmount,
                            fundingAccount = fundingAccount,
                            onDismiss = { viewModel.clearActivePaymentState() },
                            onSubmit = { pin -> viewModel.executePayment(pin) }
                        )
                    }

                    // 2. Status Processing & Animated Invoice screen
                    if (paymentStatus != "IDLE" && paymentStatus != "INPUT") {
                        PaymentResultScreen(
                            status = paymentStatus,
                            errorMessage = errorMessage,
                            recipientName = recipientName,
                            amount = activeAmount,
                            transactionId = transactionId,
                            fundingBankName = fundingAccount?.bankName ?: "Linked UPI Node",
                            category = activeCategory,
                            onDone = { viewModel.clearActivePaymentState() },
                            onRetry = {
                                viewModel.paymentStatus.value = "INPUT"
                            }
                        )
                    }
                }
            }
        }
    }
}

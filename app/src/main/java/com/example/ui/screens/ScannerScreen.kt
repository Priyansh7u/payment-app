package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.PaymentViewModel
import com.example.ui.theme.PhonePeGreen
import com.example.ui.theme.PhonePePurple
import com.example.ui.theme.PhonePePurpleLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(
    viewModel: PaymentViewModel,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var flashEnabled by remember { mutableStateOf(false) }
    var qrInputText by remember { mutableStateOf("") }
    var showScanOverlay by remember { mutableStateOf(true) }
    var scanStatusMessage by remember { mutableStateOf("Align QR code inside the frame to scan") }
    var isBeeping by remember { mutableStateOf(false) }

    // Pulse animation for scan reticle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    // Simulate scanning trigger
    val triggerScanSim: (String) -> Unit = { content ->
        scope.launch {
            isBeeping = true
            scanStatusMessage = "QR Detected! Decoding..."
            delay(800) // simulated parse speed
            val parsed = viewModel.processScannedQR(content)
            isBeeping = false
            if (parsed) {
                scanStatusMessage = "Success! Routing to Secure Pin Screen..."
                delay(600)
                // Open payment dialogue
                viewModel.pinScreenVisible.value = true
                viewModel.paymentStatus.value = "INPUT"
            } else {
                scanStatusMessage = "Failed to parse. Invalid UPI QR format."
                delay(1500)
                scanStatusMessage = "Align QR code inside the frame to scan"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F14)) // Cinematic black
    ) {
        // Upper scanner visual frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )
                            } catch (e: Exception) {
                                Log.e("ScannerScreen", "Use case binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "No Camera",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Camera Permission Required",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "You can still complete payments below using our offline simulator QR presets!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Translucent scanning viewport overlay
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                // Moving laser scanner line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = laserYOffset.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF10B981), Color.Transparent)
                            )
                        )
                )

                // Corner bracket graphics
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Left
                    Box(modifier = Modifier.size(20.dp).align(Alignment.TopStart).border(3.dp, PhonePeGreen, RoundedCornerShape(topStart = 8.dp)))
                    // Top Right
                    Box(modifier = Modifier.size(20.dp).align(Alignment.TopEnd).border(3.dp, PhonePeGreen, RoundedCornerShape(topEnd = 8.dp)))
                    // Bottom Left
                    Box(modifier = Modifier.size(20.dp).align(Alignment.BottomStart).border(3.dp, PhonePeGreen, RoundedCornerShape(bottomStart = 8.dp)))
                    // Bottom Right
                    Box(modifier = Modifier.size(20.dp).align(Alignment.BottomEnd).border(3.dp, PhonePeGreen, RoundedCornerShape(bottomEnd = 8.dp)))
                }
            }

            // Header Row controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "Scan & Pay",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = { flashEnabled = !flashEnabled },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashEnabled) Color.Yellow else Color.White
                    )
                }
            }

            // Scan feedback alert
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isBeeping) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Green)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = scanStatusMessage,
                        color = if (scanStatusMessage.startsWith("Failed")) Color.Red else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Lower Control Panel with QR presets
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "SIMULATOR PRESETS (Click to simulate scanning)",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Simulated Preset 1: Personal UPI transfer to Rohan
                QrSimulatorItem(
                    title = "Scan Rohan's Personal QR",
                    description = "Extracts: rohan.sharma@paytm • Amount: ₹250",
                    icon = Icons.Default.Person,
                    onClick = {
                        triggerScanSim("upi://pay?pa=rohan.sharma@paytm&pn=Rohan%20Sharma&am=250")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated Preset 2: Store/Merchant
                QrSimulatorItem(
                    title = "Scan Organic Food Market",
                    description = "Extracts: organic.store@okaxis • Merchant pay",
                    icon = Icons.Default.Storefront,
                    onClick = {
                        triggerScanSim("upi://pay?pa=organic.store@okaxis&pn=Organic%20Supermarket&am=")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated Preset 3: Direct Bank Account scan
                QrSimulatorItem(
                    title = "Scan Bank Account QR Code",
                    description = "Extracts: Bank Acc 4820194830 • IFSC: ICIC0000010",
                    icon = Icons.Default.AccountBalance,
                    onClick = {
                        triggerScanSim("bank://acc=4820194830&ifsc=ICIC0000010&name=Karan%20Mehta")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFF232329))

                Spacer(modifier = Modifier.height(16.dp))

                // Direct manual scanning string
                Text(
                    text = "CUSTOM SCAN STRING INPUT",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = qrInputText,
                        onValueChange = { qrInputText = it },
                        placeholder = { Text("Paste UPI / Bank URL string here", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhonePePurpleLight,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (qrInputText.isNotBlank()) {
                                triggerScanSim(qrInputText)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("DETECT", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun QrSimulatorItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF23232C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PhonePePurple.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = PhonePePurpleLight, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = description, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

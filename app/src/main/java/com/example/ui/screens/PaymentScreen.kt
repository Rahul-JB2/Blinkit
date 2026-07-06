package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class PaymentScreenState {
    object Selection : PaymentScreenState()
    object UpiPinEntry : PaymentScreenState()
    object CardEntry : PaymentScreenState()
    object Processing : PaymentScreenState()
    object Success : PaymentScreenState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: GroceryViewModel,
    onNavigateToTracking: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grandTotal by viewModel.grandTotal.collectAsState()
    val defaultAddress by viewModel.selectedAddress.collectAsState()

    var currentScreenState by remember { mutableStateOf<PaymentScreenState>(PaymentScreenState.Selection) }
    var selectedMethod by remember { mutableStateOf("UPI") } // "UPI", "CARD", "COD"
    var selectedUpiApp by remember { mutableStateOf("Google Pay") } // "Google Pay", "PhonePe", "Paytm"

    // Card Details
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    // UPI PIN
    var upiPin by remember { mutableStateOf("") }

    // Processing steps simulation
    var processingStep by remember { mutableStateOf("Securing gateway connection...") }
    val scope = rememberCoroutineScope()

    // Confetti particles
    val particles = remember {
        List(60) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                speedY = Random.nextFloat() * 12f + 8f,
                speedX = Random.nextFloat() * 4f - 2f,
                color = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFFFFEB3B),
                    Color(0xFF2196F3),
                    Color(0xFFFF5722),
                    Color(0xFFE91E63)
                ).random(),
                size = Random.nextFloat() * 15f + 10f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 10f - 5f
            )
        }
    }

    var animateConfettiTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(currentScreenState) {
        if (currentScreenState is PaymentScreenState.Success) {
            animateConfettiTrigger = true
            // Frame rate loop for confetti
            while (animateConfettiTrigger) {
                particles.forEach { p ->
                    p.y += p.speedY * 0.016f
                    p.x += p.speedX * 0.016f
                    p.rotation += p.rotationSpeed
                    if (p.y > 1.2f) {
                        p.y = -0.1f
                        p.x = Random.nextFloat()
                    }
                }
                delay(16)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation  = 2.dp,
                shadowElevation = 2.dp,
                color           = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentScreenState is PaymentScreenState.Selection) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                    } else {
                        Spacer(Modifier.width(48.dp))
                    }
                    Row(
                        modifier          = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Https, contentDescription = null,
                            tint = BrandGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = "Secure Payment Gateway",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreenState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "PaymentScreenStateTransitions"
            ) { state ->
                when (state) {
                    is PaymentScreenState.Selection -> {
                        PaymentSelectionView(
                            grandTotal = grandTotal,
                            selectedMethod = selectedMethod,
                            onMethodSelected = { selectedMethod = it },
                            selectedUpiApp = selectedUpiApp,
                            onUpiAppSelected = { selectedUpiApp = it },
                            onProceed = {
                                when (selectedMethod) {
                                    "UPI" -> currentScreenState = PaymentScreenState.UpiPinEntry
                                    "CARD" -> currentScreenState = PaymentScreenState.CardEntry
                                    else -> {
                                        // Cash on delivery
                                        triggerPaymentFlow(
                                            viewModel = viewModel,
                                            method = "Cash on Delivery",
                                            onStepChanged = { processingStep = it },
                                            onFinished = { currentScreenState = PaymentScreenState.Success },
                                            scope = scope
                                        )
                                        currentScreenState = PaymentScreenState.Processing
                                    }
                                }
                            }
                        )
                    }

                    is PaymentScreenState.UpiPinEntry -> {
                        UpiPinPadView(
                            grandTotal = grandTotal,
                            upiPin = upiPin,
                            selectedUpiApp = selectedUpiApp,
                            onPinChanged = { upiPin = it },
                            onCancel = {
                                upiPin = ""
                                currentScreenState = PaymentScreenState.Selection
                            },
                            onConfirm = {
                                triggerPaymentFlow(
                                    viewModel = viewModel,
                                    method = "UPI ($selectedUpiApp)",
                                    onStepChanged = { processingStep = it },
                                    onFinished = { currentScreenState = PaymentScreenState.Success },
                                    scope = scope
                                )
                                currentScreenState = PaymentScreenState.Processing
                            }
                        )
                    }

                    is PaymentScreenState.CardEntry -> {
                        CardEntryView(
                            grandTotal = grandTotal,
                            cardNumber = cardNumber,
                            onCardNumberChange = { cardNumber = it },
                            cardName = cardName,
                            onCardNameChange = { cardName = it },
                            cardExpiry = cardExpiry,
                            onCardExpiryChange = { cardExpiry = it },
                            cardCvv = cardCvv,
                            onCardCvvChange = { cardCvv = it },
                            onCancel = { currentScreenState = PaymentScreenState.Selection },
                            onConfirm = {
                                triggerPaymentFlow(
                                    viewModel = viewModel,
                                    method = "Credit/Debit Card",
                                    onStepChanged = { processingStep = it },
                                    onFinished = { currentScreenState = PaymentScreenState.Success },
                                    scope = scope
                                )
                                currentScreenState = PaymentScreenState.Processing
                            }
                        )
                    }

                    is PaymentScreenState.Processing -> {
                        ProcessingGatewayView(
                            stepName = processingStep
                        )
                    }

                    is PaymentScreenState.Success -> {
                        PaymentSuccessView(
                            grandTotal = grandTotal,
                            addressLabel = defaultAddress?.label ?: "Home",
                            onNavigateToTracking = {
                                animateConfettiTrigger = false
                                onNavigateToTracking()
                            },
                            particles = particles,
                            animateTrigger = animateConfettiTrigger
                        )
                    }
                }
            }
        }
    }
}

// Processing Coroutine Flow
private fun triggerPaymentFlow(
    viewModel: GroceryViewModel,
    method: String,
    onStepChanged: (String) -> Unit,
    onFinished: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    scope.launch {
        onStepChanged("Establishing highly secure 256-bit SSL connection...")
        delay(1500)
        onStepChanged("Sending authorization payload to tokenization manager...")
        delay(1200)
        onStepChanged("Authenticating transaction details with your central bank...")
        delay(1200)
        onStepChanged("Payment successfully processed! Creating your order...")
        delay(800)

        // Mutate DB inside repository (creates order, empties cart, sets tracking active!)
        viewModel.placeOrder(paymentMethod = method)
        onFinished()
    }
}

@Composable
fun PaymentSelectionView(
    grandTotal: Double,
    selectedMethod: String,
    onMethodSelected: (String) -> Unit,
    selectedUpiApp: String,
    onUpiAppSelected: (String) -> Unit,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Bill Box
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Amount to Pay", fontSize = 12.sp, color = BrandGreen, fontWeight = FontWeight.Bold)
                        Text("₹${String.format("%.2f", grandTotal)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = BrandTextDark)
                    }
                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Safe", tint = BrandGreen, modifier = Modifier.size(32.dp))
                }
            }

            Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandTextDark)

            // Method 1: UPI
            PaymentMethodRow(
                title = "UPI Apps (Auto-detect)",
                icon = Icons.Default.QrCode,
                isSelected = selectedMethod == "UPI",
                onClick = { onMethodSelected("UPI") }
            ) {
                AnimatedVisibility(visible = selectedMethod == "UPI") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Google Pay", "PhonePe", "Paytm").forEach { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedUpiApp == app) SoftGreen else Color(0xFFFAFAF9))
                                    .border(
                                        1.dp,
                                        if (selectedUpiApp == app) BrandGreen else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onUpiAppSelected(app) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = app,
                                        tint = if (selectedUpiApp == app) BrandGreen else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = app,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandTextDark
                                    )
                                }
                                Icon(
                                    imageVector = if (selectedUpiApp == app) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Select UPI App",
                                    tint = if (selectedUpiApp == app) BrandGreen else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Method 2: CARD
            PaymentMethodRow(
                title = "Credit or Debit Card",
                icon = Icons.Default.CreditCard,
                isSelected = selectedMethod == "CARD",
                onClick = { onMethodSelected("CARD") }
            ) {
                if (selectedMethod == "CARD") {
                    Text(
                        text = "Supports Visa, Mastercard, RuPay & Diners.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Method 3: COD
            PaymentMethodRow(
                title = "Cash on Delivery (COD)",
                icon = Icons.Default.Payments,
                isSelected = selectedMethod == "COD",
                onClick = { onMethodSelected("COD") }
            ) {
                if (selectedMethod == "COD") {
                    Text(
                        text = "Pay using Cash/UPI to rider when grocery arrives.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Action Button
        Button(
            onClick = onProceed,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("payment_proceed_button")
        ) {
            Text(
                text = if (selectedMethod == "COD") "Place Order with COD" else "Pay ₹${grandTotal.toInt()} Securely",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun PaymentMethodRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    expandedContent: @Composable () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isSelected) BrandGreen else Color(0xFFE4E4E7)),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = title, tint = if (isSelected) BrandGreen else Color.Gray, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandTextDark)
                }
                Icon(
                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Selected state",
                    tint = if (isSelected) BrandGreen else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            expandedContent()
        }
    }
}

@Composable
fun UpiPinPadView(
    grandTotal: Double,
    upiPin: String,
    selectedUpiApp: String,
    onPinChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedUpiApp, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Icon(imageVector = Icons.Default.Lock, contentDescription = "NPCI Unified", tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }

        // Pin display block
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PAYING QUICKMART",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "₹${String.format("%.2f", grandTotal)}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ENTER 4-DIGIT UPI PIN",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PIN Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    val filled = i < upiPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (filled) Color.White else Color.White.copy(alpha = 0.2f))
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        // Keyboard
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("❌", "0", "✓")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    when (key) {
                                        "❌" -> {
                                            if (upiPin.isNotEmpty()) {
                                                onPinChanged(upiPin.dropLast(1))
                                            }
                                        }

                                        "✓" -> {
                                            if (upiPin.length == 4) {
                                                onConfirm()
                                            }
                                        }

                                        else -> {
                                            if (upiPin.length < 4) {
                                                onPinChanged(upiPin + key)
                                            }
                                        }
                                    }
                                }
                                .testTag("pin_key_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cancel Transaction",
                color = Color.Red.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onCancel() }
                    .align(Alignment.CenterHorizontally)
                    .testTag("cancel_upi_transaction")
            )
        }
    }
}

@Composable
fun CardEntryView(
    grandTotal: Double,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardName: String,
    onCardNameChange: (String) -> Unit,
    cardExpiry: String,
    onCardExpiryChange: (String) -> Unit,
    cardCvv: String,
    onCardCvvChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Elegant gloss Card render
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF3F3F46), Color(0xFF18181B))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("QUICKMART SECURE", color = BrandYellow, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = "Chip", tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    Text(
                        text = if (cardNumber.isEmpty()) "•••• •••• •••• ••••" else cardNumber.chunked(4).joinToString(" "),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CARD HOLDER", fontSize = 9.sp, color = Color.Gray)
                            Text(text = if (cardName.isEmpty()) "YOUR NAME" else cardName.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EXPIRES", fontSize = 9.sp, color = Color.Gray)
                            Text(text = if (cardExpiry.isEmpty()) "MM/YY" else cardExpiry, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text("Enter Card Credentials", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandTextDark)

            // Form inputs
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) onCardNumberChange(it) },
                label = { Text("16-Digit Card Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_number_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = cardName,
                onValueChange = onCardNameChange,
                label = { Text("Card Holder Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_name_input"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = cardExpiry,
                    onValueChange = { onCardExpiryChange(it) },
                    label = { Text("Expiry (MM/YY)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_expiry_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cardCvv,
                    onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) onCardCvvChange(it) },
                    label = { Text("CVV") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_cvv_input"),
                    singleLine = true
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text("Cancel", color = Color.Gray)
            }
            Button(
                onClick = {
                    if (cardNumber.length == 16 && cardExpiry.isNotEmpty() && cardCvv.length == 3) {
                        onConfirm()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                modifier = Modifier
                    .weight(1.3f)
                    .height(50.dp)
                    .testTag("confirm_card_payment_btn")
            ) {
                Text("Pay ₹${grandTotal.toInt()}")
            }
        }
    }
}

@Composable
fun ProcessingGatewayView(
    stepName: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = BrandGreen,
            strokeWidth = 5.dp,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "SECURE TRANSACTION",
            color = BrandGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stepName,
            fontSize = 15.sp,
            color = BrandTextDark,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = "PCI", tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("PCI-DSS Compliant Gateway • SSL Encrypted", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PaymentSuccessView(
    grandTotal: Double,
    addressLabel: String,
    onNavigateToTracking: () -> Unit,
    particles: List<ConfettiParticle>,
    animateTrigger: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Confetti Canvas Animation
        if (animateTrigger) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    rotate(p.rotation, Offset(p.x * size.width, p.y * size.height)) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(p.x * size.width - p.size / 2, p.y * size.height - p.size / 2),
                            size = Size(p.size, p.size * 0.6f)
                        )
                    }
                }
            }
        }

        // Info container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success Ring
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(SoftGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = BrandGreen,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Order Placed Successfully!",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = BrandGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your grocery payment of ₹${grandTotal.toInt()} is confirmed securely. Store accepted and packing has started!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Delivery", fontSize = 12.sp, color = Color.Gray)
                        Text("10 - 12 Mins", fontWeight = FontWeight.Bold, color = BrandGreen, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivering to", fontSize = 12.sp, color = Color.Gray)
                        Text(addressLabel, fontWeight = FontWeight.Bold, color = BrandTextDark, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToTracking,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("track_order_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Track Live Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.TwoWheeler, contentDescription = "Track")
                }
            }
        }
    }
}

class ConfettiParticle(
    var x: Float,
    var y: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val size: Float,
    var rotation: Float,
    val rotationSpeed: Float
)

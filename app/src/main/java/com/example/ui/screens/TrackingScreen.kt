package com.example.ui.screens

import android.graphics.PathMeasure
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidPath
import com.example.data.OrderEntity
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: GroceryViewModel,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeOrder by viewModel.activeOrder.collectAsState()

    // Pulse animation for markers
    var pulseValue by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            for (i in 0..100) {
                pulseValue = sin(i * Math.PI / 50).toFloat()
                delay(16)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Delivery Tracker", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (activeOrder == null) {
            NoActiveOrderView(
                onGoHome = onNavigateHome,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            val order = activeOrder!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Interactive Canvas Map (Takes 40% height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                        .background(Color(0xFFE2E8F0)) // slate light road background
                ) {
                    MapCanvas(
                        progress = order.trackingProgress,
                        pulse = pulseValue
                    )

                    // Overlay Floating ETA Badge
                    Surface(
                        color = BrandGreen,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "ETA", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (order.status) {
                                    "Placed" -> "Arriving in 12 Mins"
                                    "Confirmed" -> "Arriving in 11 Mins"
                                    "Packing" -> "Arriving in 10 Mins"
                                    "Out for Delivery" -> {
                                        val minsLeft = ((1.0f - order.trackingProgress) * 8 + 2).toInt()
                                        "Arriving in $minsLeft Mins"
                                    }
                                    else -> "Delivered!"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Delivery Status Info (Takes 60% height)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.4f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Rider Card Details
                        item {
                            RiderCard(
                                riderName = order.riderName,
                                riderPhone = order.riderPhone,
                                progress = order.trackingProgress,
                                orderStatus = order.status
                            )
                        }

                        // Order info divider
                        item {
                            HorizontalDivider()
                        }

                        // Timeline Steps Title
                        item {
                            Text(
                                text = "Delivery Milestones",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BrandTextDark
                            )
                        }

                        // Timeline
                        item {
                            DeliveryTimeline(currentStatus = order.status)
                        }

                        // Address summary box
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF4F4F5))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Drop address", tint = BrandGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Delivering to Address", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandTextDark)
                                    Text(order.addressLine, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                                }
                            }
                        }

                        // Exit button
                        item {
                            Button(
                                onClick = onNavigateHome,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("tracking_finish_button")
                            ) {
                                Text("Back to Shopping", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapCanvas(progress: Float, pulse: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Define stores and home positions
        val storeX = width * 0.18f
        val storeY = height * 0.35f

        val userX = width * 0.82f
        val userY = height * 0.65f

        // Draw basic streets grid in background
        val streetPaint = Color.White.copy(alpha = 0.5f)
        drawLine(streetPaint, Offset(0f, height * 0.2f), Offset(width, height * 0.2f), strokeWidth = 32f)
        drawLine(streetPaint, Offset(0f, height * 0.5f), Offset(width, height * 0.5f), strokeWidth = 32f)
        drawLine(streetPaint, Offset(0f, height * 0.8f), Offset(width, height * 0.8f), strokeWidth = 32f)

        drawLine(streetPaint, Offset(width * 0.3f, 0f), Offset(width * 0.3f, height), strokeWidth = 32f)
        drawLine(streetPaint, Offset(width * 0.7f, 0f), Offset(width * 0.7f, height), strokeWidth = 32f)

        // Bezier curved road connecting store to user
        val curvedPath = Path().apply {
            moveTo(storeX, storeY)
            cubicTo(
                width * 0.45f, height * 0.25f,
                width * 0.55f, height * 0.75f,
                userX, userY
            )
        }

        // Draw dotted roadmap
        drawPath(
            path = curvedPath,
            color = Color.LightGray,
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        )

        // Draw completed route path in solid green
        val androidPath = curvedPath.asAndroidPath()
        val pathMeasure = PathMeasure(androidPath, false)
        val length = pathMeasure.length
        val pos = FloatArray(2)
        val tan = FloatArray(2)

        if (progress > 0.01f) {
            val partialPath = Path()
            pathMeasure.getSegment(0f, length * progress, partialPath.asAndroidPath(), true)
            drawPath(
                path = partialPath,
                color = BrandGreen,
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )
        }

        // Resolve current rider coordinate
        pathMeasure.getPosTan(length * progress, pos, tan)
        val riderX = if (progress <= 0f) storeX else pos[0]
        val riderY = if (progress <= 0f) storeY else pos[1]

        // Pulsing background rings for store & rider
        val pulseRadiusStore = 24f + pulse * 14f
        drawCircle(
            color = BrandGreen.copy(alpha = 0.2f),
            radius = pulseRadiusStore,
            center = Offset(storeX, storeY)
        )

        val pulseRadiusUser = 24f + pulse * 14f
        drawCircle(
            color = Color.Red.copy(alpha = 0.2f),
            radius = pulseRadiusUser,
            center = Offset(userX, userY)
        )

        // Draw Store Marker
        drawCircle(color = BrandGreen, radius = 14f, center = Offset(storeX, storeY))
        drawCircle(color = Color.White, radius = 6f, center = Offset(storeX, storeY))

        // Draw User Delivery Pin Marker
        drawCircle(color = Color.Red, radius = 14f, center = Offset(userX, userY))
        drawCircle(color = Color.White, radius = 6f, center = Offset(userX, userY))

        // Draw rider dot
        if (progress < 1.0f) {
            val pulseRadiusRider = 20f + pulse * 10f
            drawCircle(
                color = BrandYellow.copy(alpha = 0.4f),
                radius = pulseRadiusRider,
                center = Offset(riderX, riderY)
            )
            drawCircle(color = BrandYellow, radius = 16f, center = Offset(riderX, riderY))
            drawCircle(color = BrandDark, radius = 8f, center = Offset(riderX, riderY))
        }
    }
}

@Composable
fun RiderCard(
    riderName: String,
    riderPhone: String,
    progress: Float,
    orderStatus: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SoftGreen)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BrandGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Rider Avatar", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = riderName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BrandTextDark
                )
                Text(
                    text = when (orderStatus) {
                        "Placed" -> "Assigning nearby rider..."
                        "Confirmed" -> "Rider heading to store"
                        "Packing" -> "Rider waiting at store"
                        "Out for Delivery" -> {
                            val distance = String.format("%.1f", (1.0f - progress) * 2.4f)
                            "Rider is $distance km away"
                        }
                        else -> "Delivered your basket!"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Contact actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { /* Simulate call */ }
                    .testTag("call_rider_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = BrandGreen, modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { /* Simulate chat */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat", tint = BrandGreen, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun DeliveryTimeline(currentStatus: String) {
    val statuses = listOf("Placed", "Confirmed", "Packing", "Out for Delivery", "Delivered")
    val currentIndex = statuses.indexOf(currentStatus).coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxWidth()) {
        statuses.forEachIndexed { index, status ->
            val completed = index <= currentIndex
            val isCurrent = index == currentIndex

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Line + Indicator Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (completed) BrandGreen else Color.LightGray
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completed) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }

                    // Vertical road connector line between status points
                    if (index < statuses.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .background(
                                    if (index < currentIndex) BrandGreen else Color.LightGray
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Status descriptive text
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (index < statuses.lastIndex) 16.dp else 0.dp)
                ) {
                    Text(
                        text = when (status) {
                            "Placed" -> "Order Placed"
                            "Confirmed" -> "Order Confirmed"
                            "Packing" -> "Items Packing"
                            "Out for Delivery" -> "Out for Delivery"
                            else -> "Delivered"
                        },
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (isCurrent) BrandGreen else BrandTextDark
                    )
                    Text(
                        text = when (status) {
                            "Placed" -> "We have received your cart list."
                            "Confirmed" -> "Store is processing and preparing invoices."
                            "Packing" -> "Fresh items are selected and sealed."
                            "Out for Delivery" -> "Rider picked up package and is heading your way."
                            else -> "Arrived safely! Enjoy fresh products."
                        },
                        fontSize = 11.sp,
                        color = if (isCurrent) BrandGreen.copy(alpha = 0.8f) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun NoActiveOrderView(onGoHome: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SentimentDissatisfied,
            contentDescription = "No active order",
            tint = Color.LightGray,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No active tracking orders!",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = BrandTextDark
        )
        Text(
            text = "Place an order from your checkout basket to track delivery in real-time.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGoHome,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Store")
        }
    }
}

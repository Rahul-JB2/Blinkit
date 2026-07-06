package com.example.ui.screens

import android.graphics.PathMeasure
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Size
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
    var zoomScale by remember { mutableStateOf(1.0f) }
    var isSatelliteMode by remember { mutableStateOf(false) }
    val firebaseListenerLogs by viewModel.firebaseListenerLogs.collectAsState()

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
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.Default.Close, contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text       = "Live Delivery Tracker",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f)
                    )
                    // Pulsing live indicator
                    Surface(color = BrandGreen, shape = RoundedCornerShape(50)) {
                        Text(
                            text     = "🟢 LIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color    = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
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
                        .background(if (isSatelliteMode) Color(0xFF131C16) else Color(0xFFE2E8F0))
                ) {
                    MapCanvas(
                        progress = order.trackingProgress,
                        pulse = pulseValue,
                        zoomScale = zoomScale,
                        isSatelliteMode = isSatelliteMode
                    )

                    // Floating Zoom Controls (+ / -) in Column
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.15f).coerceAtMost(1.8f) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(
                            onClick = { zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.6f) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Floating "Center on Rider" & "Layer Toggle"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isSatelliteMode = !isSatelliteMode },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = if (isSatelliteMode) Icons.Default.LightMode else Icons.Default.Layers,
                                contentDescription = "Map Layers",
                                tint = BrandGreen
                            )
                        }

                        IconButton(
                            onClick = { zoomScale = 1.25f }, // reset zoom & center
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Center Rider",
                                tint = BrandYellow
                            )
                        }
                    }

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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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

                        // Real-time Firebase Listener live console terminal logs
                        item {
                            LiveTerminalLogs(logs = firebaseListenerLogs)
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
fun MapCanvas(
    progress: Float,
    pulse: Float,
    zoomScale: Float,
    isSatelliteMode: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val mapCenterX = width / 2f
        val mapCenterY = height / 2f

        // Define stores and home positions (scaled by zoom)
        val baseStoreX = width * 0.18f
        val baseStoreY = height * 0.35f
        val baseUserX = width * 0.82f
        val baseUserY = height * 0.65f

        val storeX = mapCenterX + (baseStoreX - mapCenterX) * zoomScale
        val storeY = mapCenterY + (baseStoreY - mapCenterY) * zoomScale
        val userX = mapCenterX + (baseUserX - mapCenterX) * zoomScale
        val userY = mapCenterY + (baseUserY - mapCenterY) * zoomScale

        // Draw Map Background based on layer
        if (isSatelliteMode) {
            drawRect(color = Color(0xFF131C16)) // Dark forest/military green
            
            // Draw Lake (Water polygon)
            val waterPath = Path().apply {
                moveTo(width * 0.4f, height * 0.1f)
                lineTo(width * 0.6f, height * 0.12f)
                lineTo(width * 0.55f, height * 0.28f)
                lineTo(width * 0.38f, height * 0.25f)
                close()
            }
            drawPath(path = waterPath, color = Color(0xFF006B7F))
            
            // Draw Park Area
            drawRect(
                color = Color(0xFF0F4D2B),
                topLeft = Offset(width * 0.1f, height * 0.7f),
                size = Size(width * 0.25f, height * 0.22f)
            )
        } else {
            drawRect(color = Color(0xFFE2E8F0)) // Light gray-slate
            
            // Draw standard blue river
            val waterPath = Path().apply {
                moveTo(0f, height * 0.15f)
                cubicTo(width * 0.3f, height * 0.1f, width * 0.6f, height * 0.3f, width, height * 0.2f)
            }
            drawPath(path = waterPath, color = Color(0xFFBAE6FD), style = Stroke(width = 40f))
        }

        // Draw basic streets grid in background
        val streetPaint = if (isSatelliteMode) Color(0xFF334155) else Color.White
        drawLine(streetPaint, Offset(0f, height * 0.2f), Offset(width, height * 0.2f), strokeWidth = 28f)
        drawLine(streetPaint, Offset(0f, height * 0.5f), Offset(width, height * 0.5f), strokeWidth = 28f)
        drawLine(streetPaint, Offset(0f, height * 0.8f), Offset(width, height * 0.8f), strokeWidth = 28f)

        drawLine(streetPaint, Offset(width * 0.3f, 0f), Offset(width * 0.3f, height), strokeWidth = 28f)
        drawLine(streetPaint, Offset(width * 0.7f, 0f), Offset(width * 0.7f, height), strokeWidth = 28f)

        // Bezier curved road connecting store to user
        val curvedPath = Path().apply {
            moveTo(storeX, storeY)
            cubicTo(
                mapCenterX + (width * 0.45f - mapCenterX) * zoomScale, mapCenterY + (height * 0.25f - mapCenterY) * zoomScale,
                mapCenterX + (width * 0.55f - mapCenterX) * zoomScale, mapCenterY + (height * 0.75f - mapCenterY) * zoomScale,
                userX, userY
            )
        }

        // Draw dotted roadmap
        drawPath(
            path = curvedPath,
            color = if (isSatelliteMode) Color(0xFF475569) else Color.LightGray,
            style = Stroke(
                width = 12f,
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

        if (progress > 0.01f && length > 0) {
            val partialPath = Path()
            pathMeasure.getSegment(0f, length * progress, partialPath.asAndroidPath(), true)
            drawPath(
                path = partialPath,
                color = BrandGreen,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
        }

        // Resolve current rider coordinate
        pathMeasure.getPosTan(length * progress, pos, tan)
        val riderX = if (progress <= 0f) storeX else pos[0]
        val riderY = if (progress <= 0f) storeY else pos[1]

        // Pulsing background rings for store & rider
        val pulseRadiusStore = 24f + pulse * 14f
        drawCircle(
            color = BrandGreen.copy(alpha = 0.25f),
            radius = pulseRadiusStore,
            center = Offset(storeX, storeY)
        )

        val pulseRadiusUser = 24f + pulse * 14f
        drawCircle(
            color = Color.Red.copy(alpha = 0.25f),
            radius = pulseRadiusUser,
            center = Offset(userX, userY)
        )

        // Draw Store Marker
        drawCircle(color = BrandGreen, radius = 16f, center = Offset(storeX, storeY))
        drawCircle(color = Color.White, radius = 6f, center = Offset(storeX, storeY))

        // Draw User Delivery Pin Marker
        drawCircle(color = Color.Red, radius = 16f, center = Offset(userX, userY))
        drawCircle(color = Color.White, radius = 6f, center = Offset(userX, userY))

        // Draw rider dot
        if (progress < 1.0f) {
            val pulseRadiusRider = 22f + pulse * 12f
            drawCircle(
                color = BrandYellow.copy(alpha = 0.45f),
                radius = pulseRadiusRider,
                center = Offset(riderX, riderY)
            )
            drawCircle(color = BrandYellow, radius = 18f, center = Offset(riderX, riderY))
            drawCircle(color = BrandDark, radius = 8f, center = Offset(riderX, riderY))
        }
    }
}

@Composable
fun LiveTerminalLogs(logs: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isExpanded) Color(0xFF020617) else Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = "Firebase Connection Status",
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Firebase Live Connection Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                Surface(
                    color = Color.Green.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CONNECTED",
                            color = Color.Green,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Black)
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            Text(
                                text = log,
                                color = if (log.contains("Error") || log.contains("failed")) Color.Red else Color.Green,
                                fontSize = 9.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Latency: 24ms • REST sync fallback: active",
                    fontSize = 8.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to expand real-time Firebase listener logs",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
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
        Text("🛵", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No active tracking orders!",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
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

package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderEntity
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    viewModel           : GroceryViewModel,
    onNavigateBack      : () -> Unit,
    onNavigateToTracking: () -> Unit,
    modifier            : Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()

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
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text       = "Your Orders",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f)
                    )
                    // Order count badge
                    if (orders.isNotEmpty()) {
                        Surface(
                            color  = BrandGreenLight,
                            shape  = RoundedCornerShape(50)
                        ) {
                            Text(
                                text     = "${orders.size} orders",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color    = BrandGreenDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier       = modifier
    ) { innerPadding ->
        if (orders.isEmpty()) {
            EmptyOrdersView(
                onGoShop = onNavigateBack,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier        = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding  = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.orderId }) { order ->
                    OrderHistoryCard(
                        order   = order,
                        onTrack = {
                            viewModel.setTrackingOrder(order.orderId)
                            onNavigateToTracking()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order  : OrderEntity,
    onTrack: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateString    = remember(order.dateTime) { dateFormatter.format(Date(order.dateTime)) }
    val isDelivered   = order.status == "Delivered"

    Card(
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row — order id + date + status badge
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier         = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDelivered) BrandGreenLight else SoftYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint               = if (isDelivered) BrandGreen else Color(0xFFD97706),
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text       = "Order #${order.orderId.takeLast(8).uppercase()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 14.sp,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text     = dateString,
                                fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Surface(
                    color  = if (isDelivered) BrandGreenLight else SoftYellow,
                    shape  = RoundedCornerShape(50)
                ) {
                    Text(
                        text       = order.status.uppercase(),
                        color      = if (isDelivered) BrandGreen else Color(0xFFD97706),
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Items summary
            Surface(
                color  = MaterialTheme.colorScheme.surfaceVariant,
                shape  = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text     = order.itemsSummary,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // Footer — total + action
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Paid", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text       = "₹${order.totalAmount.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp,
                        color      = BrandGreen
                    )
                }
                if (!isDelivered) {
                    Button(
                        onClick          = onTrack,
                        colors           = ButtonDefaults.buttonColors(
                            containerColor = BrandYellow,
                            contentColor   = BrandDark
                        ),
                        shape            = RoundedCornerShape(12.dp),
                        contentPadding   = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier         = Modifier
                            .height(38.dp)
                            .testTag("track_order_history_${order.orderId}")
                    ) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Track Live", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                } else {
                    Surface(
                        color  = BrandGreenLight,
                        shape  = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null,
                                tint = BrandGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delivered", fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, color = BrandGreenDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyOrdersView(onGoShop: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Text("🛍️", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "No orders yet!",
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 20.sp,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = "Your completed and active orders will appear here. Start stocking your pantry today!",
            fontSize  = 13.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onGoShop,
            colors  = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape   = RoundedCornerShape(14.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Start Shopping", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
    }
}

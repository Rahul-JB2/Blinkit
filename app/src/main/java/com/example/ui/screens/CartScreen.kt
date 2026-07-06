package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.BorderStroke
import com.example.repository.CartItemWithProduct
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val deliveryFee by viewModel.deliveryFee.collectAsState()
    val handlingFee by viewModel.handlingFee.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()

    var showAddressSheet by remember { mutableStateOf(false) }

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text       = "Checkout basket",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f)
                    )
                    Surface(color = BrandGreenLight, shape = RoundedCornerShape(50)) {
                        Text(
                            text     = "${cartItems.size} items",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color    = BrandGreenDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Address Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { showAddressSheet = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Address",
                                    tint = BrandGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Delivering to ${selectedAddress?.label ?: "Add Address"}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = selectedAddress?.let { "${it.addressLine}, ${it.area}" } ?: "Setup your delivery location",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Text(
                                text = "CHANGE",
                                color = BrandGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Checkout Button
                        Button(
                            onClick = onNavigateToPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("checkout_pay_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "₹${String.format("%.2f", grandTotal)}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "TOTAL BILL",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Proceed to Payment",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = "Pay",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            EmptyCartView(
                onGoShop = onNavigateBack,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Free Delivery Progress Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (subtotal >= 99.0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Celebration, contentDescription = "Free delivery", tint = BrandGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Awesome! You unlocked FREE Delivery! 🚚💨", color = BrandGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                val remaining = 99.0 - subtotal
                                Text(
                                    text = "Add ₹${remaining.toInt()} more for FREE Delivery",
                                    color = BrandTextDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (subtotal / 99.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = BrandGreen,
                                    trackColor = Color.Gray.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }

                // Cart Items Section Header
                item {
                    Text(
                        text = "Review Items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // List of items
                items(cartItems, key = { it.product.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { viewModel.addToCart(item.product.id) },
                        onDecrease = { viewModel.decreaseCartQuantity(item.product.id) },
                        onDelete = { viewModel.removeFromCart(item.product.id) }
                    )
                }

                // Smart Recommendations Section
                item {
                    val allProducts by viewModel.allProducts.collectAsState(initial = emptyList())
                    val cartProductIds = cartItems.map { it.product.id }.toSet()
                    val recommended = allProducts.filter { it.id !in cartProductIds }.take(4)

                    if (recommended.isNotEmpty()) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Recommendations",
                                        tint = BrandYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Smart Recommendations",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Text(
                                    text = "Synced Offline",
                                    fontSize = 10.sp,
                                    color = BrandGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(recommended, key = { it.id }) { prod ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(12.dp),
                                        border = CardDefaults.outlinedCardBorder(),
                                        modifier = Modifier
                                            .width(140.dp)
                                            .clickable { viewModel.addToCart(prod.id) }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(80.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (MaterialTheme.colorScheme.background == BrandDark) Color(0xFF1E293B) else Color(0xFFFAFAF9)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(prod.imageUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = prod.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = prod.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(text = prod.weight, fontSize = 10.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "₹${prod.price.toInt()}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Surface(
                                                    onClick = { viewModel.addToCart(prod.id) },
                                                    color = SoftGreen,
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Text(
                                                        text = "+ ADD",
                                                        color = BrandGreen,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Coupon Input section
                item {
                    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
                    val discount by viewModel.discount.collectAsState()
                    var couponInput by remember { mutableStateOf("") }
                    var applyError by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Coupons & Discounts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrandTextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (appliedCoupon != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SoftGreen, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ConfirmationNumber, contentDescription = "Coupon", tint = BrandGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Code '$appliedCoupon' Applied! (-₹${discount.toInt()})", color = BrandGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "REMOVE",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.clickable { viewModel.removeCoupon() }
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = {
                                            couponInput = it
                                            applyError = false
                                        },
                                        placeholder = { Text("Enter coupon (e.g. AI3DPROMO)") },
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandGreen,
                                            unfocusedBorderColor = Color(0xFFE4E4E7)
                                        ),
                                        modifier = Modifier.weight(1.3f)
                                    )

                                    Button(
                                        onClick = {
                                            val success = viewModel.applyCoupon(couponInput)
                                            if (success) {
                                                couponInput = ""
                                                applyError = false
                                            } else {
                                                applyError = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("APPLY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                if (applyError) {
                                    Text(text = "Invalid code! Try spinning the rewards wheel or use 'AI3DPROMO'", color = Color.Red, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }
                }

                // Billing Details
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Bill Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BrandTextDark
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "Subtotal",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Item Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = "₹${String.format("%.2f", subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DeliveryDining,
                                        contentDescription = "Delivery",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Delivery Charges", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (deliveryFee == 0.0) {
                                    Text(
                                        text = "FREE",
                                        color = BrandGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(text = "₹${String.format("%.2f", deliveryFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Handling",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Handling & Packing Charge", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = "₹${String.format("%.2f", handlingFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }

                            val appliedCoupon by viewModel.appliedCoupon.collectAsState()
                            val discount by viewModel.discount.collectAsState()
                            if (appliedCoupon != null && discount > 0.0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ConfirmationNumber,
                                            contentDescription = "Discount",
                                            tint = BrandGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Promo Discount ($appliedCoupon)", fontSize = 13.sp, color = BrandGreen, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = "-₹${String.format("%.2f", discount)}", fontSize = 13.sp, color = BrandGreen, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Grand Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandTextDark)
                                Text(
                                    text = "₹${String.format("%.2f", grandTotal)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = BrandGreen
                                )
                            }
                        }
                    }
                }

                // Promo banner card
                item {
                    if (subtotal < 99.0) {
                        Surface(
                            color = SoftYellow,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandYellow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Percent,
                                    contentDescription = "Offer",
                                    tint = BrandGreen
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Add ₹${(99.0 - subtotal).toInt()} more to get FREE Delivery!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTextDark
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = SoftGreen,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Free delivery unlocked",
                                    tint = BrandGreen
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Hurray! You unlocked FREE Delivery!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddressSheet) {
        AddressSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showAddressSheet = false }
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItemWithProduct,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFAFAF9))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandTextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.product.weight,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${item.product.price.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = BrandTextDark
                    )
                    if (item.product.originalPrice > item.product.price) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${item.product.originalPrice.toInt()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Qty selector
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BrandGreen),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .fillMaxHeight()
                        .clickable { onDecrease() }
                        .testTag("cart_decrease_qty_${item.product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(
                    text = item.quantity.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .fillMaxHeight()
                        .clickable { onIncrease() }
                        .testTag("cart_increase_qty_${item.product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(onGoShop: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🛒", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your basket is empty!",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fill it with fresh fruits, delicious snacks, and daily essentials from QuickMart.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGoShop,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = "Start Shopping", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

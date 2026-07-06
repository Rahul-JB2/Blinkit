package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay
import com.example.data.AddressEntity
import com.example.data.ProductEntity
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GroceryViewModel,
    onNavigateToCart: () -> Unit,
    onNavigateToTracking: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val defaultAddress by viewModel.selectedAddress.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()

    var showAddressSheet by remember { mutableStateOf(false) }
    var showSpinWheel by remember { mutableStateOf(false) }
    var selectedDetailsProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val isDark by viewModel.isDarkTheme.collectAsState()

    // Dynamic Search Placeholders Cycle
    val searchPlaceholders = listOf(
        "Search \"fresh organic tomatoes\"...",
        "Search \"cold-pressed orange juice\"...",
        "Search \"salted amul butter\"...",
        "Search \"whole wheat brown bread\"...",
        "Search \"monsoon crunch potato chips\"..."
    )
    var currentPlaceholderIdx by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentPlaceholderIdx = (currentPlaceholderIdx + 1) % searchPlaceholders.size
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showAddressSheet = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Delivery Address",
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Delivery in 10 Mins",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change Address",
                                tint = BrandGreen
                            )
                        }
                        Text(
                            text = defaultAddress?.let { "${it.label} - ${it.addressLine}" } ?: "Add a Delivery Address",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 240.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_switch_button")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Switch Theme",
                            tint = if (isDark) BrandYellow else BrandGreen
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.testTag("admin_dashboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Admin Operations",
                            tint = BrandGreen
                        )
                    }
                    IconButton(
                        onClick = { showSpinWheel = true },
                        modifier = Modifier.testTag("loyalty_rewards_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Spin & Win Game",
                            tint = BrandYellow
                        )
                    }
                    IconButton(
                        onClick = onNavigateToOrders,
                        modifier = Modifier.testTag("orders_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Order History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // High visibility floating cart banner if cart is not empty
            if (cartItems.isNotEmpty()) {
                val totalQty = cartItems.sumOf { it.quantity }
                val totalPrice = viewModel.grandTotal.collectAsState().value
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = BrandGreen,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .clickable { onNavigateToCart() }
                            .testTag("floating_cart_bar"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$totalQty ${if (totalQty == 1) "item" else "items"}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${String.format("%.2f", totalPrice)}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "View Cart",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Go to cart",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToChat,
                containerColor = BrandGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(bottom = if (cartItems.isNotEmpty()) 80.dp else 0.dp)
                    .testTag("ai_chatbot_fab")
            ) {
                Icon(Icons.Default.Android, contentDescription = "AI Assistant")
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Assistant", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(searchPlaceholders[currentPlaceholderIdx]) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_bar_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            // Active order notification banner
            AnimatedVisibility(
                visible = activeOrder != null && activeOrder?.status != "Delivered",
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeOrder?.let { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onNavigateToTracking() }
                            .testTag("active_order_tracking_banner"),
                        colors = CardDefaults.cardColors(containerColor = SoftYellow),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = "Rider tracking",
                                    tint = BrandGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Tracking Active Order (${order.status})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BrandTextDark
                                    )
                                    Text(
                                        text = "Rider: ${order.riderName} is on the way",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "TRACK ➔",
                                color = BrandGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Hero Banner
            PromotionalBanner(modifier = Modifier.padding(top = 8.dp))

            // Categories Selector
            Text(
                text = "Browse Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val allCategoriesList = listOf("All") + viewModel.categories
            val selectedIndex = allCategoriesList.indexOf(selectedCategory).coerceAtLeast(0)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(allCategoriesList) { index, category ->
                    CategoryChip(
                        categoryName = category,
                        isSelected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        index = index,
                        selectedIndex = selectedIndex
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Products Grid
            if (products.isEmpty()) {
                EmptyProductsView(
                    query = searchQuery,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("products_grid"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        val cartQty = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                        ProductCard(
                            product = product,
                            quantityInCart = cartQty,
                            onAddToCart = { viewModel.addToCart(product.id) },
                            onDecreaseQty = { viewModel.decreaseCartQuantity(product.id) },
                            onProductClick = { selectedDetailsProduct = product },
                            modifier = Modifier.testTag("product_card_${product.id}")
                        )
                    }
                }
            }
        }
    }

    // Address Bottom Sheet / Dialog for simplicity
    if (showAddressSheet) {
        AddressSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showAddressSheet = false }
        )
    }

    // Daily Rewards Spin Wheel Dialog
    if (showSpinWheel) {
        SpinWheelDialog(
            viewModel = viewModel,
            onDismiss = { showSpinWheel = false }
        )
    }

    // Interactive Product Details Dialog
    selectedDetailsProduct?.let { product ->
        ProductDetailsDialog(
            product = product,
            onDismiss = { selectedDetailsProduct = null },
            onAddToCart = { viewModel.addToCart(product.id) }
        )
    }
}

@Composable
fun PromotionalBanner(modifier: Modifier = Modifier) {
    var activeSlide by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            activeSlide = (activeSlide + 1) % 3
        }
    }

    val gradientColors = when (activeSlide) {
        0 -> listOf(BrandGreen, Color(0xFF004D20)) // Emerald to forest deep
        1 -> listOf(Color(0xFF6366F1), Color(0xFF312E81)) // Indigo to deep navy
        else -> listOf(Color(0xFFF59E0B), Color(0xFFB45309)) // Amber to burnt bronze
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(colors = gradientColors))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Surface(
                    color = BrandYellow,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = if (activeSlide == 0) "10 MINS DELIVERY" else if (activeSlide == 1) "MEGA BOGO OFFER" else "CRUNCH FESTIVAL",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = if (activeSlide == 0) "Grab Fresh Groceries" else if (activeSlide == 1) "Breakfast Festival" else "Snacks & Munchies",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (activeSlide == 0) "Up to 30% OFF on fresh vegetables" else if (activeSlide == 1) "Buy 1 Get 1 FREE on Milk & Eggs" else "Flat 20% OFF using AI3DPROMO",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (activeSlide == 0) Icons.Default.ShoppingBag else if (activeSlide == 1) Icons.Default.BreakfastDining else Icons.Default.Fastfood,
                    contentDescription = "Offer Banner Icon",
                    tint = BrandYellow.copy(alpha = 0.9f),
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    categoryName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    index: Int,
    selectedIndex: Int
) {
    val relativeOffset = index - selectedIndex
    val scale = if (isSelected) 1.12f else 0.95f
    val rotationY = relativeOffset * 12f // 3D cylinder rotating effect!
    
    Surface(
        onClick = onClick,
        color = if (isSelected) BrandGreen else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BrandYellow else BorderColor
        ),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 8.dp else 2.dp,
        modifier = Modifier
            .testTag("category_chip_$categoryName")
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.rotationY = rotationY
                this.cameraDistance = 8f * density
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val emoji = when (categoryName) {
                "All" -> "🛒"
                "Fruits & Vegetables" -> "🍎"
                "Dairy, Bread & Eggs" -> "🥛"
                "Munchies & Snacks" -> "🍿"
                else -> "📦"
            }
            Text(text = emoji, fontSize = 15.sp)
            Text(
                text = categoryName,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    quantityInCart: Int,
    onAddToCart: () -> Unit,
    onDecreaseQty: () -> Unit,
    onProductClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onProductClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (MaterialTheme.colorScheme.background == BrandDark) Color(0xFF1E293B) else Color(0xFFFAFAF9)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = null // fall back to standard container or vector if needed
                )

                // Optional discount badge
                if (product.originalPrice > product.price) {
                    val discount = ((product.originalPrice - product.price) / product.originalPrice * 100).toInt()
                    Surface(
                        color = BrandGreen,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "$discount% OFF",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info Area
            Text(
                text = product.weight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price and Add button area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${product.price.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (product.originalPrice > product.price) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "₹${product.originalPrice.toInt()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }

                // Add or Quantity selector button
                if (quantityInCart == 0) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = BrandGreen
                        ),
                        border = BorderStroke(1.dp, BrandGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("add_to_cart_button_${product.id}")
                    ) {
                        Text(
                            text = "ADD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandGreen),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .fillMaxHeight()
                                .clickable { onDecreaseQty() }
                                .testTag("decrease_qty_button_${product.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            text = quantityInCart.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .fillMaxHeight()
                                .clickable { onAddToCart() }
                                .testTag("increase_qty_button_${product.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProductsView(query: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = "No products found",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isEmpty()) "No products available" else "No match for \"$query\"",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = BrandTextDark
        )
        Text(
            text = "Try checking another category or refining search.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSelectionDialog(
    viewModel: GroceryViewModel,
    onDismiss: () -> Unit
) {
    val addresses by viewModel.addresses.collectAsState()
    var label by remember { mutableStateOf("") }
    var addressLine by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delivery Address", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showAddForm) {
                    Text("Add New Address", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label (e.g. Home, Work)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = addressLine,
                        onValueChange = { addressLine = it },
                        label = { Text("Flat/House No, Building") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Area, Locality") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    if (addresses.isEmpty()) {
                        Text("No saved addresses.", fontSize = 13.sp)
                    } else {
                        addresses.forEach { address ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectDefaultAddress(address.id)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (address.isDefault) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Select",
                                    tint = if (address.isDefault) BrandGreen else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = address.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${address.addressLine}, ${address.area}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (showAddForm) {
                Button(
                    onClick = {
                        if (label.isNotEmpty() && addressLine.isNotEmpty() && area.isNotEmpty()) {
                            viewModel.addAddress(label, addressLine, area)
                            showAddForm = false
                            label = ""
                            addressLine = ""
                            area = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("Save")
                }
            } else {
                Button(
                    onClick = { showAddForm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("Add New")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (showAddForm) showAddForm = false else onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}

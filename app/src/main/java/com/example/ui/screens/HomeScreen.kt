package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import com.example.data.AddressEntity
import com.example.data.OrderEntity
import com.example.data.ProductEntity
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel

// ─────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────

private fun categoryEmoji(name: String) = when {
    name.contains("Fruit", true) || name.contains("Veg", true) -> "🥦"
    name.contains("Dairy", true) || name.contains("Bread", true) || name.contains("Egg", true) -> "🥛"
    name.contains("Snack", true) || name.contains("Munch", true) -> "🍿"
    name.contains("Meat", true) || name.contains("Fish", true) -> "🥩"
    name.contains("Bak", true) -> "🥐"
    name.contains("Bev", true) || name.contains("Drink", true) -> "🧃"
    name.contains("Care", true) -> "🧴"
    name.contains("Staple", true) || name.contains("Rice", true) || name.contains("Grain", true) -> "🌾"
    else -> "📦"
}

private fun categoryBg(name: String) = when {
    name.contains("Fruit", true) || name.contains("Veg", true) -> CatColorFruits
    name.contains("Dairy", true) || name.contains("Bread", true) -> CatColorDairy
    name.contains("Snack", true) || name.contains("Munch", true) -> CatColorSnacks
    name.contains("Meat", true) || name.contains("Fish", true) -> CatColorMeat
    name.contains("Bak", true) -> CatColorBakery
    name.contains("Bev", true) -> CatColorBeverage
    name.contains("Care", true) -> CatColorCare
    else -> CatColorStaples
}

// ─────────────────────────────────────────────────────────────
//  HomeScreen
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val searchQuery     by viewModel.searchQuery.collectAsState()
    val products        by viewModel.filteredProducts.collectAsState()
    val cartItems       by viewModel.cartItems.collectAsState()
    val defaultAddress  by viewModel.selectedAddress.collectAsState()
    val activeOrder     by viewModel.activeOrder.collectAsState()
    val isDark          by viewModel.isDarkTheme.collectAsState()
    val grandTotal      by viewModel.grandTotal.collectAsState()

    var showAddressSheet        by remember { mutableStateOf(false) }
    var showSpinWheel           by remember { mutableStateOf(false) }
    var selectedDetailsProduct  by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedTab             by remember { mutableIntStateOf(0) }

    val gridState          = rememberLazyGridState()
    val searchFocusRequester = remember { FocusRequester() }

    val searchPlaceholders = listOf(
        "Search \"fresh organic tomatoes\"...",
        "Search \"cold-pressed orange juice\"...",
        "Search \"salted amul butter\"...",
        "Search \"whole wheat brown bread\"...",
        "Search \"monsoon crunch potato chips\"..."
    )
    var placeholderIdx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) { delay(3000); placeholderIdx = (placeholderIdx + 1) % searchPlaceholders.size }
    }

    // Tab navigation side-effects
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            1 -> gridState.animateScrollToItem(3) // categories section
            2 -> { gridState.animateScrollToItem(0); searchFocusRequester.requestFocus() }
        }
    }

    val cartCount = cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            HomeTopBar(
                defaultAddress  = defaultAddress,
                isDark          = isDark,
                onAddressClick  = { showAddressSheet = true },
                onToggleTheme   = { viewModel.toggleTheme() },
                onAdminClick    = onNavigateToAdmin,
                onSpinClick     = { showSpinWheel = true },
                onOrdersClick   = onNavigateToOrders
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = cartItems.isNotEmpty(),
                    enter   = slideInVertically { it } + fadeIn(),
                    exit    = slideOutVertically { it } + fadeOut()
                ) {
                    FloatingCartBar(
                        itemCount = cartCount,
                        total     = grandTotal,
                        onClick   = onNavigateToCart
                    )
                }
                HomeBottomNav(
                    selectedTab = selectedTab,
                    cartCount   = cartCount,
                    onTabSelected = { tab ->
                        if (tab == 3) onNavigateToCart() else selectedTab = tab
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = onNavigateToChat,
                containerColor   = BrandGreen,
                contentColor     = Color.White,
                shape            = CircleShape,
                modifier         = Modifier
                    .padding(bottom = if (cartItems.isNotEmpty()) 72.dp else 0.dp)
                    .testTag("ai_chatbot_fab")
            ) {
                Icon(Icons.Default.Android, contentDescription = "AI Assistant")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyVerticalGrid(
            columns        = GridCells.Fixed(2),
            state          = gridState,
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Search bar ──────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeSearchBar(
                    query         = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    placeholder   = searchPlaceholders[placeholderIdx],
                    focusRequester = searchFocusRequester
                )
            }

            // ── Active order banner ─────────────────────────────────────
            if (activeOrder != null && activeOrder?.status != "Delivered") {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ActiveOrderBanner(
                        order   = activeOrder!!,
                        onClick = onNavigateToTracking
                    )
                }
            }

            // ── Promo banner carousel ───────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                PromoBannerCarousel(modifier = Modifier.padding(top = 4.dp))
            }

            // ── Shop by Category ────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryGridSection(
                    categories       = viewModel.categories,
                    selectedCategory = selectedCategory,
                    onCategoryClick  = { viewModel.selectCategory(it) }
                )
            }

            // ── Bestsellers header ──────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier            = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = "Bestsellers",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("🔥", fontSize = 18.sp)
                    }
                    TextButton(onClick = { /* See all */ }) {
                        Text(
                            text     = "See All",
                            color    = BrandGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint   = BrandGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Products ────────────────────────────────────────────────
            if (products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyProductsView(query = searchQuery)
                }
            } else {
                items(products, key = { it.id }) { product ->
                    val cartQty = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                    ProductCard(
                        product       = product,
                        quantityInCart = cartQty,
                        onAddToCart   = { viewModel.addToCart(product.id) },
                        onDecreaseQty = { viewModel.decreaseCartQuantity(product.id) },
                        onProductClick = { selectedDetailsProduct = product },
                        modifier      = Modifier
                            .padding(
                                start  = 8.dp,
                                end    = 8.dp,
                                bottom = 4.dp
                            )
                            .testTag("product_card_${product.id}")
                    )
                }
            }
        }
    }

    if (showAddressSheet) {
        AddressSelectionDialog(viewModel = viewModel, onDismiss = { showAddressSheet = false })
    }
    if (showSpinWheel) {
        SpinWheelDialog(viewModel = viewModel, onDismiss = { showSpinWheel = false })
    }
    selectedDetailsProduct?.let { product ->
        ProductDetailsDialog(
            product      = product,
            onDismiss    = { selectedDetailsProduct = null },
            onAddToCart  = { viewModel.addToCart(product.id) }
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Top App Bar
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    defaultAddress : AddressEntity?,
    isDark         : Boolean,
    onAddressClick : () -> Unit,
    onToggleTheme  : () -> Unit,
    onAdminClick   : () -> Unit,
    onSpinClick    : () -> Unit,
    onOrdersClick  : () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: address block
            Row(
                modifier          = Modifier
                    .weight(1f)
                    .clickable(onClick = onAddressClick),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector    = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint           = BrandGreen,
                    modifier       = Modifier
                        .size(20.dp)
                        .padding(top = 3.dp)
                )
                Spacer(Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = "Delivery in 10 Mins",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = BrandGreen
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint   = BrandGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text     = defaultAddress?.let { "${it.label} – ${it.addressLine}" }
                                        ?: "Add a delivery address",
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 220.dp)
                    )
                }
            }

            // Right: action icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleTheme, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle theme",
                        tint   = if (isDark) BrandYellow else BrandGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onSpinClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("loyalty_rewards_button")) {
                    Icon(Icons.Default.Casino, contentDescription = "Spin & Win",
                        tint = BrandYellow, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onOrdersClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("orders_history_button")) {
                    Icon(Icons.Default.History, contentDescription = "Orders",
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onAdminClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("admin_dashboard_button")) {
                    Icon(Icons.Default.Settings, contentDescription = "Admin",
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }

                // Avatar circle
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier          = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BrandGreenLight),
                    contentAlignment  = Alignment.Center
                ) {
                    Text(
                        text       = "U",
                        color      = BrandGreen,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Search Bar
// ─────────────────────────────────────────────────────────────

@Composable
private fun HomeSearchBar(
    query         : String,
    onQueryChange : (String) -> Unit,
    placeholder   : String,
    focusRequester: FocusRequester
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value          = query,
            onValueChange  = onQueryChange,
            placeholder    = {
                Text(
                    text     = placeholder,
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon    = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = BrandGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon   = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier       = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("search_bar_input"),
            shape          = RoundedCornerShape(50.dp),
            singleLine     = true,
            colors         = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = BrandGreen,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Active Order Banner
// ─────────────────────────────────────────────────────────────

@Composable
private fun ActiveOrderBanner(
    order  : OrderEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
            .testTag("active_order_tracking_banner"),
        colors = CardDefaults.cardColors(containerColor = SoftYellow),
        shape  = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TwoWheeler, contentDescription = null,
                    tint = BrandGreen, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Tracking Active Order (${order.status})",
                        fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandTextDark)
                    Text("Rider: ${order.riderName} is on the way",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("TRACK ➔", color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Promo Banner Carousel
// ─────────────────────────────────────────────────────────────

private data class PromoBanner(
    val badge    : String,
    val headline : String,
    val sub      : String,
    val gradient : List<Color>
)

private val promoBanners = listOf(
    PromoBanner(
        badge    = "⚡ 10 MINS DELIVERY",
        headline = "Grab Fresh Groceries",
        sub      = "Up to 30% OFF on farm-fresh vegetables",
        gradient = listOf(Color(0xFF108A43), Color(0xFF064D26))
    ),
    PromoBanner(
        badge    = "🎉 MEGA BOGO",
        headline = "Breakfast Festival",
        sub      = "Buy 1 Get 1 FREE on Milk & Eggs",
        gradient = listOf(Color(0xFF5C35CC), Color(0xFF2E1A6E))
    ),
    PromoBanner(
        badge    = "🍿 CRUNCH FEST",
        headline = "Snacks & Munchies",
        sub      = "Flat 20% OFF – use code SNACK20",
        gradient = listOf(Color(0xFFD97706), Color(0xFF92400E))
    )
)

@Composable
private fun PromoBannerCarousel(modifier: Modifier = Modifier) {
    var activeSlide by remember { mutableIntStateOf(0) }
    val lazyState = rememberLazyListState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            activeSlide = (activeSlide + 1) % promoBanners.size
            lazyState.animateScrollToItem(activeSlide)
        }
    }

    Column(modifier = modifier) {
        LazyRow(
            state          = lazyState,
            modifier       = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(promoBanners.size) { idx ->
                val banner = promoBanners[idx]
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(148.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(banner.gradient))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier             = Modifier.fillMaxSize(),
                        verticalArrangement  = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color  = Color.White.copy(alpha = 0.18f),
                            shape  = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text       = banner.badge,
                                color      = Color.White,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                text       = banner.headline,
                                color      = Color.White,
                                fontSize   = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 24.sp
                            )
                            Text(
                                text     = banner.sub,
                                color    = Color.White.copy(alpha = 0.88f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Dot indicators
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(promoBanners.size) { idx ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (idx == activeSlide) 20.dp else 6.dp, 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (idx == activeSlide) BrandGreen else BorderColor)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Category Grid Section (4 columns)
// ─────────────────────────────────────────────────────────────

@Composable
private fun CategoryGridSection(
    categories       : List<String>,
    selectedCategory : String,
    onCategoryClick  : (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = "Shop by Category",
                fontSize   = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = { onCategoryClick("All") }) {
                Text("See All", color = BrandGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Layout 4 items per row
        val allCats = categories
        val chunked = allCats.chunked(4)
        chunked.forEachIndexed { rowIdx, rowItems ->
            if (rowIdx > 0) Spacer(Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { cat ->
                    CategoryTile(
                        name      = cat,
                        isSelected = selectedCategory == cat,
                        onClick   = { onCategoryClick(cat) },
                        modifier  = Modifier.weight(1f)
                    )
                }
                // Fill empty slots in last row
                repeat(4 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        // Also show "All" quick-filter chip
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding        = PaddingValues(0.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == "All",
                    onClick  = { onCategoryClick("All") },
                    label    = { Text("🛒 All", fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandGreen,
                        selectedLabelColor     = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled            = true,
                        selected           = selectedCategory == "All",
                        selectedBorderColor = BrandGreen,
                        borderColor        = BorderColor
                    ),
                    modifier = Modifier.testTag("category_chip_All")
                )
            }
            items(categories.size) { idx ->
                val cat = categories[idx]
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick  = { onCategoryClick(cat) },
                    label    = {
                        Text(
                            text     = "${categoryEmoji(cat)} $cat",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandGreen,
                        selectedLabelColor     = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled            = true,
                        selected           = selectedCategory == cat,
                        selectedBorderColor = BrandGreen,
                        borderColor        = BorderColor
                    ),
                    modifier = Modifier.testTag("category_chip_$cat")
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(
    name      : String,
    isSelected: Boolean,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    val bg = if (isSelected) BrandGreenLight else categoryBg(name)
    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(categoryEmoji(name), fontSize = 26.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text       = name,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color      = if (isSelected) BrandGreenDark else MaterialTheme.colorScheme.onSurface,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis,
            lineHeight = 13.sp,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Product Card
// ─────────────────────────────────────────────────────────────

@Composable
fun ProductCard(
    product        : ProductEntity,
    quantityInCart : Int,
    onAddToCart    : () -> Unit,
    onDecreaseQty  : () -> Unit,
    onProductClick : () -> Unit,
    modifier       : Modifier = Modifier
) {
    Card(
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape    = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(248.dp)
            .clickable { onProductClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Image
            Box(
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment  = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                if (product.originalPrice > product.price) {
                    val pct = ((product.originalPrice - product.price) / product.originalPrice * 100).toInt()
                    Surface(
                        color    = BrandGreen,
                        shape    = RoundedCornerShape(topStart = 12.dp, bottomEnd = 10.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text       = "$pct% OFF",
                            color      = Color.White,
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier   = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text     = product.weight,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text       = product.name,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 17.sp,
                modifier   = Modifier.weight(1f)
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = "₹${product.price.toInt()}",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.originalPrice > product.price) {
                        Text(
                            text            = "₹${product.originalPrice.toInt()}",
                            fontSize        = 11.sp,
                            color           = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration  = TextDecoration.LineThrough
                        )
                    }
                }

                if (quantityInCart == 0) {
                    OutlinedButton(
                        onClick          = onAddToCart,
                        shape            = RoundedCornerShape(10.dp),
                        colors           = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor   = BrandGreen
                        ),
                        border           = androidx.compose.foundation.BorderStroke(1.5.dp, BrandGreen),
                        contentPadding   = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                        modifier         = Modifier
                            .height(32.dp)
                            .testTag("add_to_cart_button_${product.id}")
                    ) {
                        Text("ADD", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                } else {
                    Row(
                        modifier          = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandGreen),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier         = Modifier
                                .width(32.dp)
                                .fillMaxHeight()
                                .clickable { onDecreaseQty() }
                                .testTag("decrease_qty_button_${product.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Text(
                            text       = quantityInCart.toString(),
                            color      = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 13.sp,
                            modifier   = Modifier.padding(horizontal = 4.dp)
                        )
                        Box(
                            modifier         = Modifier
                                .width(32.dp)
                                .fillMaxHeight()
                                .clickable { onAddToCart() }
                                .testTag("increase_qty_button_${product.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Floating Cart Bar
// ─────────────────────────────────────────────────────────────

@Composable
private fun FloatingCartBar(
    itemCount : Int,
    total     : Double,
    onClick   : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .testTag("floating_cart_bar"),
            color    = BrandGreen
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Icon(Icons.Default.ShoppingBasket, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(26.dp))
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .offset(x = 4.dp, y = (-4).dp),
                            shape = CircleShape,
                            color = BrandYellow
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text     = itemCount.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color    = Color.Black
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text       = "$itemCount ${if (itemCount == 1) "item" else "items"} · ₹${String.format("%.2f", total)}",
                            color      = Color.White,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text     = "Free delivery applied 🎉",
                            color    = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View Cart", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    Icon(Icons.Default.ChevronRight, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Bottom Navigation Bar
// ─────────────────────────────────────────────────────────────

@Composable
fun HomeBottomNav(
    selectedTab   : Int,
    cartCount     : Int,
    onTabSelected : (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("Home",       Icons.Default.Home,           Icons.Default.Home),
            Triple("Categories", Icons.Default.GridView,       Icons.Default.GridView),
            Triple("Search",     Icons.Default.Search,         Icons.Default.Search),
            Triple("Cart",       Icons.Default.ShoppingCart,   Icons.Default.ShoppingCart)
        )
        items.forEachIndexed { idx, (label, icon, _) ->
            NavigationBarItem(
                selected  = selectedTab == idx,
                onClick   = { onTabSelected(idx) },
                icon      = {
                    if (idx == 3 && cartCount > 0) {
                        BadgedBox(badge = {
                            Badge(containerColor = BrandGreen) {
                                Text(
                                    text     = cartCount.toString(),
                                    fontSize = 9.sp,
                                    color    = Color.White
                                )
                            }
                        }) {
                            Icon(icon, contentDescription = label)
                        }
                    } else {
                        Icon(icon, contentDescription = label)
                    }
                },
                label     = { Text(label, fontSize = 10.sp) },
                colors    = NavigationBarItemDefaults.colors(
                    selectedIconColor      = BrandGreen,
                    selectedTextColor      = BrandGreen,
                    indicatorColor         = BrandGreenLight,
                    unselectedIconColor    = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Empty State
// ─────────────────────────────────────────────────────────────

@Composable
fun EmptyProductsView(query: String, modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier
            .fillMaxWidth()
            .height(260.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Text("😔", fontSize = 52.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text       = if (query.isEmpty()) "No products available" else "No match for \"$query\"",
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 16.sp,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text     = "Try a different category or search term.",
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Address Selection Dialog  (unchanged logic, kept intact)
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSelectionDialog(
    viewModel : GroceryViewModel,
    onDismiss : () -> Unit
) {
    val addresses by viewModel.addresses.collectAsState()
    var label       by remember { mutableStateOf("") }
    var addressLine by remember { mutableStateOf("") }
    var area        by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delivery Address", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
        text  = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showAddForm) {
                    Text("Add New Address", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandGreen)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = label, onValueChange = { label = it },
                        label = { Text("Label (e.g. Home, Work)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(value = addressLine, onValueChange = { addressLine = it },
                        label = { Text("Flat/House No, Building") },
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(value = area, onValueChange = { area = it },
                        label = { Text("Area, Locality") },
                        modifier = Modifier.fillMaxWidth())
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
                                    imageVector = if (address.isDefault) Icons.Default.RadioButtonChecked
                                                  else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (address.isDefault) BrandGreen else Color.Gray
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(address.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${address.addressLine}, ${address.area}",
                                        fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (showAddForm) {
                        if (label.isNotEmpty() && addressLine.isNotEmpty() && area.isNotEmpty()) {
                            viewModel.addAddress(label, addressLine, area)
                            showAddForm = false
                            label = ""; addressLine = ""; area = ""
                        }
                    } else {
                        showAddForm = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Text(if (showAddForm) "Save" else "Add New")
            }
        },
        dismissButton = {
            TextButton(onClick = { if (showAddForm) showAddForm = false else onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

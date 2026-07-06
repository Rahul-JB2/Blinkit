package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel

// Category display data
private data class CatItem(
    val name        : String,
    val emoji       : String,
    val bg          : Color,
    val description : String
)

private val ALL_CATS = listOf(
    CatItem("Fruits & Vegetables", "🥦", CatColorFruits,   "Fresh from the farm daily"),
    CatItem("Dairy, Bread & Eggs", "🥛", CatColorDairy,   "Chilled & fresh always"),
    CatItem("Munchies & Snacks",   "🍿", CatColorSnacks,  "Crispy, crunchy treats"),
    CatItem("Cold Drinks & Juices","🧃", CatColorBeverage,"Cool off anytime"),
    CatItem("Instant Foods",       "🍜", CatColorStaples, "Quick & easy meals"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel           : GroceryViewModel,
    onNavigateBack      : () -> Unit,
    onNavigateToSearch  : () -> Unit,
    onNavigateToCart    : () -> Unit,
    modifier            : Modifier = Modifier
) {
    val cartItems       by viewModel.cartItems.collectAsState()
    val cartCount       = cartItems.sumOf { it.quantity }
    val allProducts     by viewModel.allProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredBySearch = remember(allProducts, searchQuery, selectedCategory) {
        allProducts.filter { p ->
            val matchCat = selectedCategory == "All" || p.category == selectedCategory
            val matchQ   = searchQuery.isEmpty() || p.name.contains(searchQuery, ignoreCase = true)
            matchCat && matchQ
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
                    modifier              = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text       = "All Categories",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick  = onNavigateToCart,
                        modifier = Modifier.testTag("categories_cart_button")
                    ) {
                        BadgedBox(badge = {
                            if (cartCount > 0) Badge(containerColor = BrandGreen) {
                                Text(cartCount.toString(), fontSize = 9.sp, color = Color.White)
                            }
                        }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart",
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        bottomBar = {
            HomeBottomNav(
                selectedTab   = 1,
                cartCount     = cartCount,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> onNavigateBack()
                        2 -> onNavigateToSearch()
                        3 -> onNavigateToCart()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier       = modifier
    ) { innerPadding ->
        LazyVerticalGrid(
            columns        = GridCells.Fixed(2),
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Search bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Search in all categories…", fontSize = 13.sp) },
                    leadingIcon   = {
                        Icon(Icons.Default.Search, contentDescription = null,
                            tint = BrandGreen, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon  = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape         = RoundedCornerShape(50.dp),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor    = BrandGreen,
                        unfocusedBorderColor  = BorderColor,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Category header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text       = "Browse by Category",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Category cards – full-width, horizontal
            items(
                items = ALL_CATS,
                span  = { GridItemSpan(maxLineSpan) }
            ) { cat ->
                val isSelected = selectedCategory == cat.name
                Card(
                    onClick           = { viewModel.selectCategory(cat.name) },
                    colors            = CardDefaults.cardColors(
                        containerColor = if (isSelected) BrandGreenLight else MaterialTheme.colorScheme.surface
                    ),
                    shape             = RoundedCornerShape(16.dp),
                    elevation         = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp),
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("cat_card_${cat.name}")
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji icon box
                        Box(
                            modifier         = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(cat.bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat.emoji, fontSize = 26.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text       = cat.name,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = if (isSelected) BrandGreenDark else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text     = cat.description,
                                fontSize = 12.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector        = if (isSelected) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint               = if (isSelected) BrandGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Products for selected category
            if (selectedCategory != "All") {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text       = if (searchQuery.isEmpty()) selectedCategory else "Results for \"$searchQuery\"",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onBackground,
                            modifier   = Modifier.weight(1f),
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Surface(
                            color  = BrandGreenLight,
                            shape  = RoundedCornerShape(50)
                        ) {
                            Text(
                                text     = "${filteredBySearch.size} items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color    = BrandGreenDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (filteredBySearch.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyProductsView(query = searchQuery, modifier = Modifier.height(200.dp))
                    }
                } else {
                    items(filteredBySearch, key = { it.id }) { product ->
                        val cartQty = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                        ProductCard(
                            product        = product,
                            quantityInCart = cartQty,
                            onAddToCart    = { viewModel.addToCart(product.id) },
                            onDecreaseQty  = { viewModel.decreaseCartQuantity(product.id) },
                            onProductClick = { },
                            modifier       = Modifier.padding(horizontal = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            } else {
                // "All" selected — show all products
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text       = "All Products",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onBackground,
                        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(filteredBySearch, key = { it.id }) { product ->
                    val cartQty = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                    ProductCard(
                        product        = product,
                        quantityInCart = cartQty,
                        onAddToCart    = { viewModel.addToCart(product.id) },
                        onDecreaseQty  = { viewModel.decreaseCartQuantity(product.id) },
                        onProductClick = { },
                        modifier       = Modifier.padding(horizontal = 8.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel

private val RECENT_SEARCHES = listOf(
    "Fresh Milk", "Bananas", "Bread", "Eggs", "Orange Juice", "Chips"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel              : GroceryViewModel,
    onNavigateBack         : () -> Unit,
    onNavigateToCategories : () -> Unit,
    onNavigateToCart       : () -> Unit,
    modifier               : Modifier = Modifier
) {
    val cartItems    by viewModel.cartItems.collectAsState()
    val allProducts  by viewModel.allProducts.collectAsState()
    val cartCount    = cartItems.sumOf { it.quantity }

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val results = remember(allProducts, query) {
        if (query.length < 2) emptyList()
        else allProducts.filter { it.name.contains(query, ignoreCase = true) }
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    OutlinedTextField(
                        value         = query,
                        onValueChange = { query = it },
                        placeholder   = { Text("Search groceries, brands…", fontSize = 13.sp) },
                        leadingIcon   = {
                            Icon(Icons.Default.Search, contentDescription = null,
                                tint = BrandGreen, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon  = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear",
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier      = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("search_screen_input"),
                        shape         = RoundedCornerShape(50.dp),
                        singleLine    = true,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor    = BrandGreen,
                            unfocusedBorderColor  = BorderColor,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* already filtering live */ })
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
        },
        bottomBar = {
            HomeBottomNav(
                selectedTab   = 2,
                cartCount     = cartCount,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> onNavigateBack()
                        1 -> onNavigateToCategories()
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
            // Empty query — show recent + popular
            if (query.length < 2) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text       = "Recent Searches",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(RECENT_SEARCHES) { term ->
                                SuggestionChip(
                                    onClick = { query = term },
                                    label   = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.History, contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(term, fontSize = 12.sp)
                                        }
                                    },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text       = "Popular Right Now 🔥",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onBackground,
                        modifier   = Modifier.padding(horizontal = 16.dp, bottom = 4.dp)
                    )
                }

                items(allProducts.take(6), key = { it.id }) { product ->
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
            } else {
                // Results header
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text       = "Results for \"$query\"",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(color = BrandGreenLight, shape = RoundedCornerShape(50)) {
                            Text(
                                text     = "${results.size} found",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color    = BrandGreenDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (results.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            horizontalAlignment   = Alignment.CenterHorizontally,
                            verticalArrangement   = Arrangement.Center
                        ) {
                            Text("🔍", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text       = "No results for \"$query\"",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 16.sp,
                                color      = MaterialTheme.colorScheme.onBackground,
                                textAlign  = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text     = "Try a different spelling or browse categories.",
                                fontSize = 13.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    items(results, key = { it.id }) { product ->
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
}

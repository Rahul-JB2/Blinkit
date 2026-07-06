package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: GroceryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var showAddProductForm by remember { mutableStateOf(false) }
    var newProdName by remember { mutableStateOf("") }
    var newProdCategory by remember { mutableStateOf("Fruits & Vegetables") }
    var newProdPrice by remember { mutableStateOf("") }
    var newProdWeight by remember { mutableStateOf("250g") }
    var newProdStock by remember { mutableStateOf("20") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A) // Dark slate premium background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Panel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Gross Sales",
                        value = "₹42,890.00",
                        icon = Icons.Default.Payments,
                        tint = BrandGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Orders Done",
                        value = "${orders.size + 14}",
                        icon = Icons.Default.TrendingUp,
                        tint = SecondaryGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sales Analytics Chart (Canvas)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sales Volume (Last 7 Days)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val points = listOf(
                                Offset(0f, 100f),
                                Offset(0.16f, 80f),
                                Offset(0.33f, 40f),
                                Offset(0.5f, 60f),
                                Offset(0.66f, 30f),
                                Offset(0.83f, 90f),
                                Offset(1f, 10f)
                            )

                            val width = size.width
                            val height = size.height

                            val path = Path()
                            val fillPath = Path()

                            points.forEachIndexed { idx, point ->
                                val x = point.x * width
                                val y = point.y * height / 100f * 0.8f + (height * 0.1f)
                                if (idx == 0) {
                                    path.moveTo(x, y)
                                    fillPath.moveTo(x, height)
                                    fillPath.lineTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                    fillPath.lineTo(x, y)
                                    if (idx == points.size - 1) {
                                        fillPath.lineTo(x, height)
                                        fillPath.close()
                                    }
                                }
                            }

                            // Draw gradient backing
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(BrandGreen.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )

                            // Draw curve stroke
                            drawPath(
                                path = path,
                                color = BrandGreen,
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // Draw points
                            points.forEach { point ->
                                val x = point.x * width
                                val y = point.y * height / 100f * 0.8f + (height * 0.1f)
                                drawCircle(
                                    color = BrandYellow,
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                Text(text = day, color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Interactive Order Simulator Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Live Order Operations",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                color = if (activeOrder != null) BrandGreen.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (activeOrder != null) "1 Active" else "No Active Order",
                                    color = if (activeOrder != null) BrandGreen else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (activeOrder != null) {
                            activeOrder?.let { order ->
                                Text(
                                    text = "Order ID: ${order.orderId}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Current Status: ${order.status}",
                                    color = BrandYellow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Items: ${order.itemsSummary}",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Visual progress slider representation
                                LinearProgressIndicator(
                                    progress = { order.trackingProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = BrandGreen,
                                    trackColor = Color.Gray.copy(alpha = 0.3f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.advanceActiveOrderTracking() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FastForward, contentDescription = "Speed")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Advance Tracker Stage ➔", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "Place an order in the app first, then come here to manually speed up/step through active courier transit delivery states instantly!",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Products list & Quick inventory adder
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Products & Stock Levels",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { showAddProductForm = !showAddProductForm }) {
                        Icon(
                            imageVector = if (showAddProductForm) Icons.Default.Close else Icons.Default.AddCircle,
                            contentDescription = "Add Product",
                            tint = BrandGreen
                        )
                    }
                }
            }

            // Expandable Add Product Form
            item {
                AnimatedVisibility(visible = showAddProductForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Create Premium Product",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            OutlinedTextField(
                                value = newProdName,
                                onValueChange = { newProdName = it },
                                label = { Text("Product Name") },
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGreen,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Dropdown categories selection (simple Row selection for simplicity)
                            Column {
                                Text("Category", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Fruits & Vegetables", "Dairy, Bread & Eggs", "Munchies & Snacks").forEach { cat ->
                                        Surface(
                                            color = if (newProdCategory == cat) BrandGreen else Color(0xFF334155),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .clickable { newProdCategory = cat }
                                                .weight(1f)
                                        ) {
                                            Text(
                                                text = cat.take(12) + "..",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = newProdPrice,
                                    onValueChange = { newProdPrice = it },
                                    label = { Text("Price (₹)") },
                                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandGreen,
                                        unfocusedBorderColor = Color.Gray
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newProdWeight,
                                    onValueChange = { newProdWeight = it },
                                    label = { Text("Weight") },
                                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandGreen,
                                        unfocusedBorderColor = Color.Gray
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newProdStock,
                                    onValueChange = { newProdStock = it },
                                    label = { Text("Stock") },
                                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandGreen,
                                        unfocusedBorderColor = Color.Gray
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Button(
                                onClick = {
                                    val price = newProdPrice.toDoubleOrNull() ?: 20.0
                                    val stock = newProdStock.toIntOrNull() ?: 15
                                    if (newProdName.isNotEmpty()) {
                                        viewModel.addNewProduct(
                                            name = newProdName,
                                            category = newProdCategory,
                                            price = price,
                                            originalPrice = price * 1.25,
                                            weight = newProdWeight,
                                            stock = stock
                                        )
                                        newProdName = ""
                                        newProdPrice = ""
                                        showAddProductForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add Product to 3D Catalog")
                            }
                        }
                    }
                }
            }

            // Products stocks list
            items(products) { product ->
                AdminProductRow(
                    product = product,
                    onStockChange = { newStock -> viewModel.updateProductStock(product.id, newStock) }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = Color.Gray, fontSize = 11.sp)
                Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AdminProductRow(
    product: ProductEntity,
    onStockChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(text = product.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${product.category} • ${product.weight}", color = Color.Gray, fontSize = 11.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = if (product.stock > 5) BrandGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Stock: ${product.stock}",
                        color = if (product.stock > 5) BrandGreen else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Button(
                    onClick = { onStockChange(product.stock + 10) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("+10 Stock", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}

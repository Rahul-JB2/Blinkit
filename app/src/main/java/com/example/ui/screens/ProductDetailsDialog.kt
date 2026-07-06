package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import com.example.data.ProductEntity
import com.example.ui.theme.*

@Composable
fun ProductDetailsDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp) // Spacing for bottom CTA
                ) {
                    // Holographic 3D product asset viewer
                    ThreeDProductAssetViewer(product = product)

                    Column(modifier = Modifier.padding(18.dp)) {
                        // Category and ratings
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = product.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = BrandYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("4.8 (120 reviews)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandTextDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandTextDark
                        )
                        Text(
                            text = product.weight,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Price and discount
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${product.price.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGreen
                            )
                            if (product.originalPrice > product.price) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹${product.originalPrice.toInt()}",
                                    fontSize = 15.sp,
                                    color = Color.Gray,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = BrandYellow,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "SAVE ₹${(product.originalPrice - product.price).toInt()}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Description",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Glassmorphic Nutrition stats table
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Organic Nutrition Insights (Per 100g)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = BrandGreen
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    NutritionStat(label = "Energy", value = "45 Cal")
                                    NutritionStat(label = "Carbs", value = "9.2g")
                                    NutritionStat(label = "Proteins", value = "1.1g")
                                    NutritionStat(label = "Fats", value = "0.2g")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Frequently Bought Together Bundle suggestion
                        Text(
                            text = "Frequently Bought Together",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF9)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data("https://images.unsplash.com/photo-1595855759920-86582396756a?w=100")
                                                .build(),
                                            contentDescription = "Tomato Bundle",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Double Fresh Bundle Deal", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandTextDark)
                                        Text("Add fresh tomato bundle, get 10% off", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Text(
                                    text = "+ Add Bundle",
                                    color = BrandGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .clickable { onAddToCart() }
                                        .padding(6.dp)
                                )
                            }
                        }
                    }
                }

                // Close button top-right
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandTextDark, modifier = Modifier.size(18.dp))
                }

                // Bottom Add to Cart CTA
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(72.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                onAddToCart()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add item to Basket • ₹${product.price.toInt()}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 10.sp)
        Text(text = value, fontWeight = FontWeight.Black, fontSize = 12.sp, color = BrandTextDark)
    }
}

@Composable
fun ThreeDProductAssetViewer(
    product: ProductEntity,
    modifier: Modifier = Modifier
) {
    var rotationY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Automatic slow spin fallback when not dragging
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            while (isActive) {
                delay(30)
                rotationY = (rotationY + 1f) % 360f
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(SoftGreen, Color.White)
                )
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        rotationY = (rotationY + dragAmount.x * 0.5f) % 360f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Holographic pedestal (drawn on Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height * 0.75f
            
            // Draw holographic rings with 3D perspective ellipse
            for (i in 1..3) {
                val radiusX = 110.dp.toPx() * (1f - i * 0.15f)
                val radiusY = 28.dp.toPx() * (1f - i * 0.15f)
                drawOval(
                    color = BrandGreen.copy(alpha = 0.25f / i),
                    topLeft = Offset(centerX - radiusX, centerY - radiusY),
                    size = Size(radiusX * 2, radiusY * 2),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Draw glowing vertical laser grid pillars projecting upwards
            val numPillars = 8
            for (i in 0 until numPillars) {
                val angle = (i * (360f / numPillars) + rotationY) * (Math.PI / 180f).toFloat()
                val radiusX = 110.dp.toPx() * 0.7f
                val radiusY = 28.dp.toPx() * 0.7f
                val px = centerX + radiusX * Math.cos(angle.toDouble()).toFloat()
                val py = centerY + radiusY * Math.sin(angle.toDouble()).toFloat()

                // Draw vertical laser stream
                drawLine(
                    color = BrandYellow.copy(alpha = 0.35f),
                    start = Offset(px, py),
                    end = Offset(px, py - 110.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw rotating core node
                drawCircle(
                    color = BrandGreen,
                    radius = 3.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Help Overlay Text
        Text(
            text = "◀ DRAG HORIZONTALLY TO ROTATE 3D MODEL ▶",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = BrandGreen.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

        // Localized Inventory/Firestore sync indicator badge
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("FIRESTORE LIVE SYNC ACTIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }

        // The product image with real-time 3D rotation transform!
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer {
                    this.rotationY = rotationY
                    this.cameraDistance = 12f * density
                    this.scaleX = 0.92f
                    this.scaleY = 0.92f
                }
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

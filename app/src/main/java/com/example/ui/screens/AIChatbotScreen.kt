package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ProductEntity
import com.example.ui.theme.*
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.GroceryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatbotScreen(
    viewModel: GroceryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isListening by remember { mutableStateOf(false) }

    // Auto scroll to bottom when message log changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Android, contentDescription = "AI Assistant", tint = BrandGreen)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Green-Grocer AI", fontWeight = FontWeight.Black, fontSize = 15.sp)
                            Text("Online • Powered by AI", color = BrandGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                // Inline suggest tags
                if (messages.isNotEmpty()) {
                    val lastMsg = messages.last()
                    if (lastMsg.sender == "AI" && lastMsg.suggestions.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(lastMsg.suggestions) { suggestion ->
                                Surface(
                                    color = SoftGreen,
                                    shape = RoundedCornerShape(20.dp),
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier.clickable {
                                        viewModel.sendChatMessage(suggestion)
                                    }
                                ) {
                                    Text(
                                        text = suggestion,
                                        color = BrandGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Input bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            isListening = true
                            scope.launch {
                                delay(2500)
                                isListening = false
                                val voicePrompts = listOf(
                                    "Fruits List",
                                    "Get 20% Off coupon",
                                    "Add Milk",
                                    "Suggest some organic vegetables"
                                )
                                viewModel.sendChatMessage(voicePrompts.random())
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = SoftYellow,
                            contentColor = Color(0xFFF7C11E)
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Order")
                    }

                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("Ask: 'Do you have ripe bananas?'", fontSize = 13.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            unfocusedBorderColor = Color(0xFFE4E4E7)
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (inputQuery.trim().isNotEmpty()) {
                                viewModel.sendChatMessage(inputQuery)
                                inputQuery = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = BrandGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isAi = message.sender == "AI"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Column(
                            horizontalAlignment = if (isAi) Alignment.Start else Alignment.End,
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Surface(
                                color = if (isAi) Color.White else BrandGreen,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isAi) 4.dp else 16.dp,
                                    bottomEnd = if (isAi) 16.dp else 4.dp
                                ),
                                shadowElevation = 1.dp
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = message.text,
                                        color = if (isAi) BrandTextDark else Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )

                                    // Display integrated product grids if present
                                    if (isAi && message.productsToAdd.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        message.productsToAdd.forEach { prod ->
                                            EmbeddedChatProductCard(
                                                product = prod,
                                                onAdd = { viewModel.addToCart(prod.id) }
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                            Text(
                                text = if (isAi) "AI Assistant" else "Me",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
                            )
                        }
                    }
                }
            }

            // Microphone Voice Assistant Overlay
            AnimatedVisibility(
                visible = isListening,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .width(280.dp)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AI Voice Ordering",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandTextDark
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Breathing glowing mic
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(BrandGreen, SoftGreen)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Mic",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Speak now...",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = BrandGreen
                            )
                            Text(
                                text = "Try: 'Show fruits list' or 'Add full milk'",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmbeddedChatProductCard(
    product: ProductEntity,
    onAdd: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = BrandTextDark
                )
                Text(
                    text = "${product.weight} • ₹${product.price.toInt()}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("ADD", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White)
            }
        }
    }
}

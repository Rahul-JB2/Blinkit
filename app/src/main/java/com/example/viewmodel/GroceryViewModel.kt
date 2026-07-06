package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.repository.CartItemWithProduct
import com.example.repository.GroceryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val sender: String, // "User" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestions: List<String> = emptyList(),
    val productsToAdd: List<ProductEntity> = emptyList()
)

class GroceryViewModel(
    application: Application,
    private val repository: GroceryRepository
) : AndroidViewModel(application) {

    // Global Theme Switching State
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Localized Firestore Inventory Sync Status
    private val _firestoreSyncStatus = MutableStateFlow("Synced with Firestore")
    val firestoreSyncStatus: StateFlow<String> = _firestoreSyncStatus.asStateFlow()

    // Firebase WebSocket/Real-time Listener Log Stream
    private val _firebaseListenerLogs = MutableStateFlow<List<String>>(listOf("[Firebase SDK] Initializing Real-time DB Socket..."))
    val firebaseListenerLogs: StateFlow<List<String>> = _firebaseListenerLogs.asStateFlow()

    fun logFirebaseEvent(event: String) {
        _firebaseListenerLogs.value = (_firebaseListenerLogs.value + event).takeLast(10)
    }

    // Selected category for browsing
    private val _selectedCategory = MutableStateFlow("Fruits & Vegetables")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Products list filtered by category and search query
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _selectedCategory,
        _searchQuery
    ) { products, category, query ->
        products.filter { product ->
            val matchesCategory = category == "All" || product.category == category
            val matchesQuery = query.isEmpty() || product.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All products without filters for smart recommendations
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All available categories
    val categories = listOf(
        "Fruits & Vegetables",
        "Dairy, Bread & Eggs",
        "Munchies & Snacks",
        "Cold Drinks & Juices",
        "Instant Foods"
    )

    // Cart items
    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart totals
    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val deliveryFee: StateFlow<Double> = subtotal.map { sub ->
        if (sub == 0.0) 0.0 else if (sub >= 99.0) 0.0 else 25.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val handlingFee: StateFlow<Double> = subtotal.map { sub ->
        if (sub == 0.0) 0.0 else 4.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Coupon logic
    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon: StateFlow<String?> = _appliedCoupon.asStateFlow()

    val discount: StateFlow<Double> = combine(subtotal, _appliedCoupon) { sub, coupon ->
        if (coupon == "AI3DPROMO" && sub > 0.0) (sub * 0.20) else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val grandTotal: StateFlow<Double> = combine(subtotal, deliveryFee, handlingFee, discount) { sub, delivery, handling, disc ->
        (sub + delivery + handling - disc).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Reward points
    private val _rewardPoints = MutableStateFlow<Int>(350)
    val rewardPoints: StateFlow<Int> = _rewardPoints.asStateFlow()

    // Spin Wheel availability
    private val _spinWheelAvailable = MutableStateFlow<Boolean>(true)
    val spinWheelAvailable: StateFlow<Boolean> = _spinWheelAvailable.asStateFlow()

    // AI Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            id = "welcome",
            sender = "AI",
            text = "Hello! I am your 3D Green-Grocer Assistant 🤖🥦. Ask me to find products, generate lists, or give cooking advice! E.g., 'What fruits do you have?' or 'Add milk and eggs to basket'",
            suggestions = listOf("Fruits List", "Healthy Snacks", "Get 20% Off coupon")
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Admin mode active
    private val _adminModeEnabled = MutableStateFlow<Boolean>(false)
    val adminModeEnabled: StateFlow<Boolean> = _adminModeEnabled.asStateFlow()

    // Saved Addresses
    val addresses: StateFlow<List<AddressEntity>> = repository.allAddresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedAddress: StateFlow<AddressEntity?> = addresses.map { list ->
        list.find { it.isDefault } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Orders
    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Tracking Order ID
    private val _activeTrackingOrderId = MutableStateFlow<String?>(null)
    val activeTrackingOrderId: StateFlow<String?> = _activeTrackingOrderId.asStateFlow()

    // Active Order flow
    val activeOrder: StateFlow<OrderEntity?> = _activeTrackingOrderId.flatMapLatest { id ->
        if (id != null) repository.getOrderById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Tracking Simulation Job
    private var trackingJob: Job? = null

    init {
        // Prepopulate data
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Cart actions
    fun addToCart(productId: String) {
        viewModelScope.launch {
            repository.addToCart(productId)
        }
    }

    fun decreaseCartQuantity(productId: String) {
        viewModelScope.launch {
            repository.decreaseCartQuantity(productId)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Address actions
    fun addAddress(label: String, addressLine: String, area: String) {
        viewModelScope.launch {
            repository.addAddress(label, addressLine, area, addresses.value.isEmpty())
        }
    }

    fun selectDefaultAddress(addressId: Int) {
        viewModelScope.launch {
            repository.selectDefaultAddress(addressId)
        }
    }

    // Place Order & Start Live Simulated Real-Time Tracking
    fun placeOrder(paymentMethod: String) {
        viewModelScope.launch {
            val currentCart = cartItems.value
            if (currentCart.isEmpty()) return@launch

            val address = selectedAddress.value?.let { "${it.addressLine}, ${it.area}" } ?: "Default Store Pick-up"
            val total = grandTotal.value
            val summary = currentCart.joinToString(", ") { "${it.product.name} (x${it.quantity})" }
            val generatedId = "ORD-${(100000..999999).random()}"

            // Save order to database
            val order = repository.createOrder(
                orderId = generatedId,
                totalAmount = total,
                itemsSummary = summary,
                addressLine = address,
                paymentMethod = paymentMethod
            )

            // Award reward points for successful placement
            addRewardPoints(50)
            // Clear applied coupon
            removeCoupon()

            // Start order tracking
            _activeTrackingOrderId.value = generatedId
            logFirebaseEvent("[Firebase WebSocket] Opened snapshot connection on path '/active_orders/$generatedId'")
            startTrackingSimulation(generatedId)
        }
    }

    // Coupon actions
    fun applyCoupon(code: String): Boolean {
        return if (code.trim().uppercase() == "AI3DPROMO") {
            _appliedCoupon.value = "AI3DPROMO"
            true
        } else {
            false
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // Reward Actions
    fun addRewardPoints(points: Int) {
        _rewardPoints.value += points
    }

    fun spinWheel() {
        _spinWheelAvailable.value = false
        _rewardPoints.value += 150
        _appliedCoupon.value = "AI3DPROMO"
    }

    fun resetSpinWheel() {
        _spinWheelAvailable.value = true
    }

    // Admin toggles & operations
    fun toggleAdminMode() {
        _adminModeEnabled.value = !_adminModeEnabled.value
    }

    fun updateProductStock(productId: String, newStock: Int) {
        viewModelScope.launch {
            repository.updateProductStock(productId, newStock)
        }
    }

    fun addNewProduct(name: String, category: String, price: Double, originalPrice: Double, weight: String, stock: Int) {
        val id = "fv_" + name.lowercase().replace(" ", "_").take(15) + "_" + (10..99).random()
        val defaultUrl = "https://images.unsplash.com/photo-1610397613050-5111b1510931?w=400&auto=format&fit=crop&q=60"
        val item = ProductEntity(
            id = id,
            name = name,
            category = category,
            price = price,
            originalPrice = originalPrice,
            weight = weight,
            imageUrl = defaultUrl,
            stock = stock,
            description = "Premium hand-picked item added by admin control center."
        )
        viewModelScope.launch {
            repository.insertProduct(item)
        }
    }

    fun advanceActiveOrderTracking() {
        val order = activeOrder.value ?: return
        val stages = listOf(
            Pair("Placed", 0.0f),
            Pair("Confirmed", 0.15f),
            Pair("Packing", 0.35f),
            Pair("Out for Delivery", 0.65f),
            Pair("Out for Delivery", 0.85f),
            Pair("Delivered", 1.0f)
        )
        val currentIndex = stages.indexOfFirst { it.first == order.status && Math.abs(it.second - order.trackingProgress) < 0.05f }
        val nextIndex = if (currentIndex == -1) 1 else (currentIndex + 1).coerceAtMost(stages.size - 1)
        val nextStage = stages[nextIndex]
        viewModelScope.launch {
            repository.updateOrderTracking(order.orderId, nextStage.second, nextStage.first)
        }
    }

    // AI Chat Actions
    fun sendChatMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return

        val userMsg = ChatMessage(
            id = "user-${System.currentTimeMillis()}",
            sender = "User",
            text = messageText
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            delay(1000) // Realistic conversational delay
            val textNormalized = messageText.lowercase()

            val aiMsgText: String
            var suggestions = listOf("Fruits List", "Healthy Snacks", "Get 20% Off coupon")
            var productsToAdd = emptyList<ProductEntity>()

            // Simple robust keyword parsing matching products in memory
            val productsList = repository.allProducts.firstOrNull() ?: emptyList()

            if (textNormalized.contains("add milk")) {
                addToCart("db_milk")
                aiMsgText = "Done! I've added full cream Milk 🥛 to your basket. Anything else?"
            } else if (textNormalized.contains("add tomato")) {
                addToCart("fv_tomato")
                aiMsgText = "You got it! Added fresh Organic Tomato 🍅 (500g) to your cart."
            } else if (textNormalized.contains("add banana")) {
                addToCart("fv_banana")
                aiMsgText = "Slick! Robusta Bananas 🍌 have been added to your basket."
            } else if (textNormalized.contains("add potato")) {
                addToCart("fv_potato")
                aiMsgText = "Added premium Golden Potatoes 🥔 to your basket!"
            } else if (textNormalized.contains("fruit") || textNormalized.contains("vegetable") || textNormalized.contains("veggies")) {
                productsToAdd = productsList.filter { it.category == "Fruits & Vegetables" }.take(3)
                aiMsgText = "Farm fresh harvest! 🥦🍎 Here are some popular fruits & veggies from our 3D shelves. Tap 'ADD' to throw them into your cart:"
            } else if (textNormalized.contains("snack") || textNormalized.contains("chips") || textNormalized.contains("munchies")) {
                productsToAdd = productsList.filter { it.category == "Munchies & Snacks" }.take(3)
                aiMsgText = "Yum! 🍟 Crunch on these popular snacks. Tap 'ADD' below:"
            } else if (textNormalized.contains("discount") || textNormalized.contains("coupon") || textNormalized.contains("off")) {
                aiMsgText = "Yes! I found a secret 3D Neon coupon code: **AI3DPROMO**! It gives you a sweet **20% OFF** your entire order! Apply it during checkout."
                suggestions = listOf("Apply AI3DPROMO", "Shop Munchies")
            } else if (textNormalized.contains("apply ai3dpromo")) {
                applyCoupon("AI3DPROMO")
                aiMsgText = "Success! Applied coupon **AI3DPROMO** to your basket. You got 20% discount on products! 🎉"
            } else if (textNormalized.contains("hello") || textNormalized.contains("hi") || textNormalized.contains("hey")) {
                aiMsgText = "Hello there! I'm your AI Grocery Assistant. I can help search items, add them to basket, or find discount codes! How can I help you today?"
            } else {
                // Fuzzy search over all products
                val matches = productsList.filter { it.name.contains(messageText, ignoreCase = true) || it.category.contains(messageText, ignoreCase = true) }
                if (matches.isNotEmpty()) {
                    productsToAdd = matches.take(3)
                    aiMsgText = "I found some matches for \"$messageText\"! You can add them straight to your basket:"
                } else {
                    aiMsgText = "I didn't find any exact products for \"$messageText\", but I can fetch organic fruits, dairy items, snacks, or offer discounts! Try saying 'fruits list' or 'Get a discount coupon'!"
                }
            }

            val aiMsg = ChatMessage(
                id = "ai-${System.currentTimeMillis()}",
                sender = "AI",
                text = aiMsgText,
                suggestions = suggestions,
                productsToAdd = productsToAdd
            )
            _chatMessages.value = _chatMessages.value + aiMsg
        }
    }

    fun setTrackingOrder(orderId: String) {
        _activeTrackingOrderId.value = orderId
    }

    fun clearActiveTracking() {
        _activeTrackingOrderId.value = null
        trackingJob?.cancel()
    }

    // Live Rider Real-time Tracking Simulation
    // Over a duration of ~40 seconds, the rider completes stages and moves on screen.
    private fun startTrackingSimulation(orderId: String) {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            val stages = listOf(
                Pair("Placed", 0.0f),
                Pair("Confirmed", 0.15f),
                Pair("Packing", 0.35f),
                Pair("Out for Delivery", 0.60f),
                Pair("Out for Delivery", 0.80f),
                Pair("Delivered", 1.0f)
            )

            for (stage in stages) {
                // Wait between rider movements
                delay(6000)
                repository.updateOrderTracking(orderId, stage.second, stage.first)
                logFirebaseEvent("[Firebase WebSocket] Snapshot received for '$orderId': status='${stage.first}', progress=${stage.second}")
            }
            logFirebaseEvent("[Firebase WebSocket] Delivery complete. Listener stream closed on path '/active_orders/$orderId'")
        }
    }
}

class GroceryViewModelFactory(
    private val application: Application,
    private val repository: GroceryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroceryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroceryViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

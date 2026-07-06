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

class GroceryViewModel(
    application: Application,
    private val repository: GroceryRepository
) : AndroidViewModel(application) {

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

    val grandTotal: StateFlow<Double> = combine(subtotal, deliveryFee, handlingFee) { sub, delivery, handling ->
        sub + delivery + handling
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

            // Start order tracking
            _activeTrackingOrderId.value = generatedId
            startTrackingSimulation(generatedId)
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
            }
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

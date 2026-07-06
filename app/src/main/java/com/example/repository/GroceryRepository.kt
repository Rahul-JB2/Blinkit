package com.example.repository

import com.example.data.*
import kotlinx.coroutines.flow.*

data class CartItemWithProduct(
    val product: ProductEntity,
    val quantity: Int
)

class GroceryRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val cartDao = database.cartDao()
    private val addressDao = database.addressDao()
    private val orderDao = database.orderDao()

    // Flow of all products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    // Flow of cart items mapped with products
    val cartItems: Flow<List<CartItemWithProduct>> = combine(
        cartDao.getCartItems(),
        productDao.getAllProducts()
    ) { cartList, productList ->
        cartList.mapNotNull { cartItem ->
            val product = productList.find { it.id == cartItem.productId }
            if (product != null) {
                CartItemWithProduct(product, cartItem.quantity)
            } else {
                null
            }
        }
    }

    // Flow of saved addresses
    val allAddresses: Flow<List<AddressEntity>> = addressDao.getAllAddresses()

    // Flow of past orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrderById(orderId: String): Flow<OrderEntity?> = orderDao.getOrderById(orderId)

    // Prepopulate products if they don't exist
    suspend fun prepopulateDatabaseIfEmpty() {
        // Prepopulate basic products
        val defaultProducts = listOf(
            // Fruits & Vegetables
            ProductEntity(
                id = "fv_tomato",
                name = "Fresh Organic Tomato",
                category = "Fruits & Vegetables",
                price = 39.0,
                originalPrice = 49.0,
                weight = "500g",
                imageUrl = "https://images.unsplash.com/photo-1595855759920-86582396756a?w=400&auto=format&fit=crop&q=60",
                stock = 15,
                description = "Farm-fresh ripe tomatoes, rich in vitamins. Perfect for curries, salads, and soups."
            ),
            ProductEntity(
                id = "fv_onion",
                name = "Red Onion",
                category = "Fruits & Vegetables",
                price = 28.0,
                originalPrice = 35.0,
                weight = "1kg",
                imageUrl = "https://images.unsplash.com/photo-1508747703725-719777637510?w=400&auto=format&fit=crop&q=60",
                stock = 25,
                description = "Crisp, pungent onions sourced directly from farmers. Essential kitchen staple."
            ),
            ProductEntity(
                id = "fv_potato",
                name = "Golden Potato",
                category = "Fruits & Vegetables",
                price = 32.0,
                originalPrice = 40.0,
                weight = "1kg",
                imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400&auto=format&fit=crop&q=60",
                stock = 30,
                description = "Nutritious golden potatoes. Highly versatile, ideal for frying, mashing, or baking."
            ),
            ProductEntity(
                id = "fv_banana",
                name = "Premium Robusta Banana",
                category = "Fruits & Vegetables",
                price = 45.0,
                originalPrice = 55.0,
                weight = "6 pcs",
                imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&auto=format&fit=crop&q=60",
                stock = 12,
                description = "Sweet, fully ripe bananas. Instant energy booster and great for healthy snacking."
            ),

            // Dairy, Bread & Eggs
            ProductEntity(
                id = "db_milk",
                name = "Full Cream Fresh Milk",
                category = "Dairy, Bread & Eggs",
                price = 33.0,
                originalPrice = 35.0,
                weight = "500ml",
                imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400&auto=format&fit=crop&q=60",
                stock = 20,
                description = "Pasteurized full cream milk, rich in calcium and vitamin D. Essential daily nutrient."
            ),
            ProductEntity(
                id = "db_butter",
                name = "Salted Amul Butter",
                category = "Dairy, Bread & Eggs",
                price = 56.0,
                originalPrice = 58.0,
                weight = "100g",
                imageUrl = "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=400&auto=format&fit=crop&q=60",
                stock = 18,
                description = "The classic salted butter. Rich, creamy, and perfect for spreading on toasted bread."
            ),
            ProductEntity(
                id = "db_bread",
                name = "100% Whole Wheat Bread",
                category = "Dairy, Bread & Eggs",
                price = 45.0,
                originalPrice = 50.0,
                weight = "400g",
                imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&auto=format&fit=crop&q=60",
                stock = 10,
                description = "High fiber brown bread baked with 100% whole grains. Clean, soft, and wholesome."
            ),
            ProductEntity(
                id = "db_eggs",
                name = "Farm Fresh Brown Eggs",
                category = "Dairy, Bread & Eggs",
                price = 65.0,
                originalPrice = 75.0,
                weight = "6 pcs",
                imageUrl = "https://images.unsplash.com/photo-1506976785307-8732e854ad03?w=400&auto=format&fit=crop&q=60",
                stock = 14,
                description = "Protein-rich eggs from free-range healthy hens. Perfect for breakfast scrambles."
            ),

            // Munchies & Snacks
            ProductEntity(
                id = "ms_chips",
                name = "Lay's Spanish Tomato Tango",
                category = "Munchies & Snacks",
                price = 20.0,
                originalPrice = 20.0,
                weight = "50g",
                imageUrl = "https://images.unsplash.com/photo-1566478431375-7f44cc13486c?w=400&auto=format&fit=crop&q=60",
                stock = 40,
                description = "Crispy potato chips seasoned with tangy sweet tomatoes and aromatic herbs."
            ),
            ProductEntity(
                id = "ms_cookies",
                name = "Double Chocolate Chip Cookies",
                category = "Munchies & Snacks",
                price = 79.0,
                originalPrice = 99.0,
                weight = "150g",
                imageUrl = "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&auto=format&fit=crop&q=60",
                stock = 15,
                description = "Decadent bakery-style cookies loaded with rich semi-sweet chocolate chips."
            ),
            ProductEntity(
                id = "ms_peanuts",
                name = "Salted Roasted Peanuts",
                category = "Munchies & Snacks",
                price = 40.0,
                originalPrice = 50.0,
                weight = "200g",
                imageUrl = "https://images.unsplash.com/photo-1569562211093-4ed0d0758f12?w=400&auto=format&fit=crop&q=60",
                stock = 22,
                description = "Crunchy roasted peanuts with a pinch of sea salt. The perfect evening snack companion."
            ),

            // Cold Drinks & Juices
            ProductEntity(
                id = "cd_cola",
                name = "Coca-Cola Zero Sugar",
                category = "Cold Drinks & Juices",
                price = 40.0,
                originalPrice = 40.0,
                weight = "300ml can",
                imageUrl = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&auto=format&fit=crop&q=60",
                stock = 50,
                description = "Crisp, cold, and bubbly zero-sugar Coca-Cola soda can. Serve chilled."
            ),
            ProductEntity(
                id = "cd_coconut",
                name = "Natural Tender Coconut Water",
                category = "Cold Drinks & Juices",
                price = 55.0,
                originalPrice = 60.0,
                weight = "200ml",
                imageUrl = "https://images.unsplash.com/photo-1526401485004-46910ecc8e51?w=400&auto=format&fit=crop&q=60",
                stock = 18,
                description = "100% natural, refreshing tender coconut water. Loaded with electrolytes."
            ),
            ProductEntity(
                id = "cd_juice",
                name = "Fresh Pressed Orange Juice",
                category = "Cold Drinks & Juices",
                price = 110.0,
                originalPrice = 135.0,
                weight = "250ml",
                imageUrl = "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400&auto=format&fit=crop&q=60",
                stock = 8,
                description = "Cold-pressed sweet citrus orange juice with absolutely no added sugar or preservatives."
            ),

            // Instant Foods
            ProductEntity(
                id = "if_noodles",
                name = "Maggi 2-Min Masala Noodles",
                category = "Instant Foods",
                price = 14.0,
                originalPrice = 14.0,
                weight = "70g",
                imageUrl = "https://images.unsplash.com/photo-1612927601601-6638404737ce?w=400&auto=format&fit=crop&q=60",
                stock = 100,
                description = "India's favorite 2-minute snack. Wholesome instant noodles blended with aromatic spices."
            )
        )
        productDao.insertAll(defaultProducts)

        // Prepopulate default addresses if empty
        val existingAddresses = addressDao.getAllAddresses().first()
        if (existingAddresses.isEmpty()) {
            addressDao.insertAddress(
                AddressEntity(
                    label = "Home",
                    addressLine = "Apt 902, Tower 4, Royal Palms Residences",
                    area = "Indiranagar, Bengaluru",
                    isDefault = true
                )
            )
            addressDao.insertAddress(
                AddressEntity(
                    label = "Office",
                    addressLine = "6th Floor, WeWork Galaxy, Residency Road",
                    area = "CBD, Bengaluru",
                    isDefault = false
                )
            )
        }
    }

    // Products
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(category)

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        productDao.searchProducts(query)

    // Cart
    suspend fun addToCart(productId: String) {
        val currentItems = cartDao.getCartItems().first()
        val existing = currentItems.find { it.productId == productId }
        if (existing != null) {
            cartDao.updateCartItem(CartItemEntity(productId, existing.quantity + 1))
        } else {
            cartDao.insertCartItem(CartItemEntity(productId, 1))
        }
    }

    suspend fun decreaseCartQuantity(productId: String) {
        val currentItems = cartDao.getCartItems().first()
        val existing = currentItems.find { it.productId == productId }
        if (existing != null) {
            if (existing.quantity > 1) {
                cartDao.updateCartItem(CartItemEntity(productId, existing.quantity - 1))
            } else {
                cartDao.deleteByProductId(productId)
            }
        }
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.deleteByProductId(productId)
    }

    suspend fun updateProductStock(productId: String, stock: Int) {
        val products = allProducts.firstOrNull() ?: emptyList()
        val product = products.find { it.id == productId }
        if (product != null) {
            productDao.insertAll(listOf(product.copy(stock = stock)))
        }
    }

    suspend fun insertProduct(product: ProductEntity) {
        productDao.insertAll(listOf(product))
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Address
    suspend fun addAddress(label: String, addressLine: String, area: String, isDefault: Boolean) {
        val address = AddressEntity(
            label = label,
            addressLine = addressLine,
            area = area,
            isDefault = isDefault
        )
        addressDao.insertAddress(address)
    }

    suspend fun deleteAddress(address: AddressEntity) {
        addressDao.deleteAddress(address)
    }

    suspend fun selectDefaultAddress(addressId: Int) {
        addressDao.setDefaultAddress(addressId)
    }

    // Orders
    suspend fun createOrder(
        orderId: String,
        totalAmount: Double,
        itemsSummary: String,
        addressLine: String,
        paymentMethod: String
    ): OrderEntity {
        val order = OrderEntity(
            orderId = orderId,
            dateTime = System.currentTimeMillis(),
            totalAmount = totalAmount,
            itemsSummary = itemsSummary,
            status = "Placed",
            addressLine = addressLine,
            paymentMethod = paymentMethod,
            paymentStatus = "Success"
        )
        orderDao.insertOrder(order)
        cartDao.clearCart() // Clean the cart after ordering
        return order
    }

    suspend fun updateOrderTracking(orderId: String, progress: Float, status: String) {
        orderDao.updateOrderTracking(orderId, progress, status)
    }
}

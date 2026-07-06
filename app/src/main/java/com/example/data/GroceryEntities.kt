package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val originalPrice: Double,
    val weight: String,
    val imageUrl: String,
    val stock: Int,
    val description: String = "Freshly sourced high-quality item, delivered in minutes."
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String, // "Home", "Office", "Other"
    val addressLine: String,
    val area: String,
    val isDefault: Boolean
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val dateTime: Long,
    val totalAmount: Double,
    val itemsSummary: String,
    val status: String, // "Placed", "Confirmed", "Packing", "Out for Delivery", "Delivered"
    val addressLine: String,
    val paymentMethod: String,
    val paymentStatus: String, // "Success", "Pending", "Failed"
    val trackingProgress: Float = 0.0f, // 0.0 to 1.0 representing rider distance
    val riderName: String = "Ramesh Kumar",
    val riderPhone: String = "+91 98765 43210"
)

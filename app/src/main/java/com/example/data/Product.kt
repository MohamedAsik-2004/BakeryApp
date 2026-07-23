package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String, // "Tea", "Coffee", "Cold Drinks" / "Beverages", "Snacks", "Bakery"
    val description: String,
    val imageUrl: String,
    val isActive: Boolean = true,
    val tag: String = "", // "BEST SELLER", "RELAXING", "CLASSIC", "DETOX", "LIMITED"
    val barcode: String = "",
    val sku: String = "",
    val subcategory: String = "",
    val discountPrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val stockQuantity: Int = 100,
    val lowStockAlert: Int = 10,
    val unit: String = "pcs",
    val favorite: Boolean = false,
    val multipleImages: String = ""
)

package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TeaDao {

    // Product Queries
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    // Cart Queries
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItemByProductId(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Order Records Queries
    @Query("SELECT * FROM order_records ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderRecord)

    @Query("DELETE FROM order_records")
    suspend fun deleteAllOrders()

    @Query("DELETE FROM order_records WHERE timestamp >= :startTime")
    suspend fun deleteOrdersSince(startTime: Long)

    // Business Profile Queries
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getBusinessProfile(): Flow<BusinessProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBusinessProfile(profile: BusinessProfile)

    @Query("DELETE FROM business_profile")
    suspend fun deleteBusinessProfile()
}

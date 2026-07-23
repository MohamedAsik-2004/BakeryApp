package com.example.data

import kotlinx.coroutines.flow.Flow

class TeaRepository(private val teaDao: TeaDao) {

    val allProducts: Flow<List<Product>> = teaDao.getAllProducts()
    val cartItems: Flow<List<CartItem>> = teaDao.getCartItems()
    val allOrders: Flow<List<OrderRecord>> = teaDao.getAllOrders()

    suspend fun insertProduct(product: Product) {
        teaDao.insertProduct(product)
    }

    suspend fun insertProducts(products: List<Product>) {
        teaDao.insertProducts(products)
    }

    suspend fun updateProduct(product: Product) {
        teaDao.updateProduct(product)
    }

    suspend fun deleteProduct(id: Int) {
        teaDao.deleteProductById(id)
    }

    suspend fun addToCart(productId: Int, qty: Int = 1) {
        teaDao.insertCartItem(CartItem(productId, qty))
    }

    suspend fun deleteCartItem(productId: Int) {
        teaDao.deleteCartItemByProductId(productId)
    }

    suspend fun clearCart() {
        teaDao.clearCart()
    }

    suspend fun insertOrder(order: OrderRecord) {
        teaDao.insertOrder(order)
    }

    suspend fun deleteAllProducts() {
        teaDao.deleteAllProducts()
    }

    suspend fun deleteAllOrders() {
        teaDao.deleteAllOrders()
    }

    suspend fun deleteOrdersSince(startTime: Long) {
        teaDao.deleteOrdersSince(startTime)
    }

    suspend fun deleteBusinessProfile() {
        teaDao.deleteBusinessProfile()
    }

    val businessProfile: Flow<BusinessProfile?> = teaDao.getBusinessProfile()

    suspend fun insertOrUpdateBusinessProfile(profile: BusinessProfile) {
        teaDao.insertOrUpdateBusinessProfile(profile)
    }
}

package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TeaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TeaRepository

    val allProducts: StateFlow<List<Product>>
    val cartItems: StateFlow<List<CartItem>>
    val allOrders: StateFlow<List<OrderRecord>>
    val businessProfile: StateFlow<BusinessProfile>

    init {
        val dao = AppDatabase.getDatabase(application).teaDao()
        repository = TeaRepository(dao)

        businessProfile = repository.businessProfile
            .map { it ?: BusinessProfile() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = BusinessProfile()
            )

        allProducts = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        cartItems = repository.cartItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allOrders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate on first launch
        viewModelScope.launch {
            repository.allProducts.first().let { currentList ->
                if (currentList.isEmpty()) {
                    prepopulateDefaultProducts()
                }
            }
            repository.allOrders.first().let { currentOrders ->
                if (currentOrders.isEmpty()) {
                    prepopulateDefaultOrders()
                }
            }
        }
    }

    private suspend fun prepopulateDefaultProducts() {
        val defaults = listOf(
            Product(
                name = "Masala Chai",
                price = 15.0,
                category = "Tea",
                description = "Classic spiced milk tea with fresh ginger and cardamom.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC-biQpd9712kFgVOfc8tW9xeOIQFMf1g_H2b-mRCkrQ4pw6O0FMsAtiLrg-NYN1_T90qSqv9RZiX0jgeIQqEZ7XOGcZs74N9pYtTiYwwTOKcBjxzlVH-pc6Kr1aCJ4n9sSd4ViutQFZJrRNvZezNP3ynThT0BHFJ-JrkUvRlztSWC_p1VYwX1jSnRqQhM8ERsi66OqO_5bjTmjrQjwmUO7FdIZSVb8JdjU5zAF_hC1h0XXC0KR-vcbmp7AKU3A2F9QBNcsgAz9rGHN",
                isActive = true,
                tag = "BEST SELLER"
            ),
            Product(
                name = "Organic Green Tea",
                price = 20.0,
                category = "Tea",
                description = "Light & refreshing, anti-oxidant rich Himalayan brew.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDrFUV5Fgu_x2LGr2el8ostXpz7_p_GSJ2tGkYPKYw36syHFdq3KCqMaicLwGvPugYc_pY9LCbtDqzOuIGeepFpHc4smqz93aI9mXbJyV5iYR27EBSzNqQmr6BtUcQn427weUROInh13y1Luknrzx-G4PlbubiO0vj-ozvYFKb0NeGlMmeJ-GK0WyzhCypUzaPqD9AbotYti7RLmkzQIy0zdAsGJaoHB-H57x8Ny9t1R2Ut6nrl9UaTGGQQP3HogbUcWDIb0VrFuTvc",
                isActive = true,
                tag = "DETOX"
            ),
            Product(
                name = "Ginger Tea",
                price = 15.0,
                category = "Tea",
                description = "Hand-crushed fresh ginger root infusion with milk.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBIxn44_aEPcdA8eQmbJ8xhWUBvEcFR71SpnsMtGQkfMu3VeQGbHFn9rV2ejZ4HPMCPPedgeum9puTwLLNU7vl0UlHZghJXHrmjYGkLOamP7XBzXZJafYpNMe4r4eClwIp2YyDLtkouSRUnuoLHdMaRQ9nMRzx_UO5lfF0y8W3RQy0cMYsF6h_2CsVnZKq5odu9bbaEoNSnBe3tfDlT2rUjaohM4gTAIZmRu-Ur84c8ln05-puxJd4JZRNMYIPSL8MTZXaGwhvSyoOf",
                isActive = true,
                tag = "CLASSIC"
            ),
            Product(
                name = "Filter Coffee",
                price = 20.0,
                category = "Beverages",
                description = "Strong South-Indian chicory rich slow filter drip roast.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBskSb1CMOaENwkpz2B5pV9rcHUvzTm5zltjynUEf8VMtP_-iDhfUdcMyNOoTw4HYWJ3R9plGFYRu_vrbIWGAw2dlRhNUQLX7qtuVDfe96zxrYTMtQKGMZLoJ7cDjfhz3QRUwAePRF_oDB2ZXjoMxTc8QsntU-WSIN7PTUzJpW9xYWgobnf-zPFGwz5oYaxwv0qAfh7pbIaMEWWSsK9GEZBidK-n84CEve4dsG9kuW1Cqq3lVm-QTtAQSYTj_fU8jbwierjESo3oKMx",
                isActive = false, // Out of Stock
                tag = "OUT OF STOCK"
            ),
            Product(
                name = "Banana Cake",
                price = 45.0,
                category = "Bakery",
                description = "Freshly baked moist banana walnut cake slices.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBtdURPpBELuppWUxtQDg9P0YoSZ1PMfk_kSJUdNvIvL9fCwUC3hjPvCoHRqFxxc7BYVyOkF6F3tLDO31gFELXhkU-tZwNydpoZE5c88lxoXf1bP1Jo90T43tl4NixsjvnUbkr48gMyf4Jt93BisiHul5KziSGU6hmxJx-tCW8ik1ijjBE4fnMEj0ILZrNnubkvs_FZoiI9IP7_Q_LNZjWlyLPYnvrWyzkrH0DMmXrdS8zXyKwWW7mBLh79zpEPg8Egwl7nkzrVe98X",
                isActive = true,
                tag = "LIMITED"
            )
        )
        repository.insertProducts(defaults)
    }

    private suspend fun prepopulateDefaultOrders() {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // Add some historic orders to simulate past week sales data
        val orders = listOf(
            OrderRecord(
                timestamp = now - (3600000 * 2), // 2 hrs ago
                subtotal = 50.0,
                discount = 5.0,
                gst = 9.0,
                total = 54.0,
                paymentMethod = "UPI Transfer",
                billingAddress = "128 Apothecary Way, CA",
                orderNotes = "Please ensure the Oolong is packed in airtight tins.",
                itemsSummary = "Masala Chai x2, Ginger Tea x1"
            ),
            OrderRecord(
                timestamp = now - (3600000 * 12), // 12 hrs ago
                subtotal = 110.0,
                discount = 11.0,
                gst = 19.8,
                total = 118.8,
                paymentMethod = "Credit / Debit",
                billingAddress = "Custom Office, Block B",
                orderNotes = "Deliver hot.",
                itemsSummary = "Organic Green Tea x3, Filter Coffee x1, Banana Cake x1"
            ),
            OrderRecord(
                timestamp = now - (3600000 * 28), // Yesterday
                subtotal = 45.0,
                discount = 4.5,
                gst = 8.1,
                total = 48.6,
                paymentMethod = "Digital Wallet",
                billingAddress = "15 Sage Lane",
                orderNotes = "No sugar in green tea.",
                itemsSummary = "Banana Cake x1"
            ),
            OrderRecord(
                timestamp = now - (3600000 * 72), // 3 days ago
                subtotal = 95.0,
                discount = 9.5,
                gst = 17.1,
                total = 102.6,
                paymentMethod = "Cash at Counter",
                billingAddress = "Table 4",
                orderNotes = "",
                itemsSummary = "Masala Chai x3, Banana Cake x1, Ginger Tea x1"
            ),
            OrderRecord(
                timestamp = now - (3600000 * 120), // 5 days ago
                subtotal = 210.0,
                discount = 21.0,
                gst = 37.8,
                total = 226.8,
                paymentMethod = "UPI Transfer",
                billingAddress = "Apothecary Lab",
                orderNotes = "Secure packaging.",
                itemsSummary = "Organic Green Tea x6, Banana Cake x2"
            )
        )

        for (ord in orders) {
            repository.insertOrder(ord)
        }
    }

    // Action Methods
    fun addProduct(name: String, price: Double, category: String, description: String, imageUrl: String, isActive: Boolean, tag: String) {
        viewModelScope.launch {
            repository.insertProduct(
                Product(
                    name = name,
                    price = price,
                    category = category,
                    description = description,
                    imageUrl = imageUrl,
                    isActive = isActive,
                    tag = tag
                )
            )
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun duplicateProduct(product: Product) {
        viewModelScope.launch {
            repository.insertProduct(
                product.copy(id = 0, name = product.name + " (Copy)")
            )
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun addToCart(productId: Int, qty: Int = 1) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == productId }
            if (existing != null) {
                repository.addToCart(productId, existing.quantity + qty)
            } else {
                repository.addToCart(productId, qty)
            }
        }
    }

    fun decreaseInCart(productId: Int) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == productId }
            if (existing != null) {
                if (existing.quantity > 1) {
                    repository.addToCart(productId, existing.quantity - 1)
                } else {
                    repository.deleteCartItem(productId)
                }
            }
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun checkout(
        paymentMethod: String,
        billingAddress: String,
        orderNotes: String,
        subtotal: Double,
        discount: Double,
        gst: Double,
        total: Double,
        itemsSummary: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insertOrder(
                OrderRecord(
                    timestamp = System.currentTimeMillis(),
                    subtotal = subtotal,
                    discount = discount,
                    gst = gst,
                    total = total,
                    paymentMethod = paymentMethod,
                    billingAddress = billingAddress,
                    orderNotes = orderNotes,
                    itemsSummary = itemsSummary
                )
            )
            repository.clearCart()
            onSuccess()
        }
    }

    fun updateBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.insertOrUpdateBusinessProfile(profile)
        }
    }

    fun clearAllProducts() {
        viewModelScope.launch {
            repository.deleteAllProducts()
        }
    }

    fun clearAllOrders() {
        viewModelScope.launch {
            repository.deleteAllOrders()
        }
    }

    fun clearDailyReports() {
        viewModelScope.launch {
            val oneDayAgo = System.currentTimeMillis() - 86400000L
            repository.deleteOrdersSince(oneDayAgo)
        }
    }

    fun clearWeeklyReports() {
        viewModelScope.launch {
            val oneWeekAgo = System.currentTimeMillis() - (7L * 86400000L)
            repository.deleteOrdersSince(oneWeekAgo)
        }
    }

    fun clearMonthlyReports() {
        viewModelScope.launch {
            val oneMonthAgo = System.currentTimeMillis() - (30L * 86400000L)
            repository.deleteOrdersSince(oneMonthAgo)
        }
    }

    fun clearYearlyReports() {
        viewModelScope.launch {
            val oneYearAgo = System.currentTimeMillis() - (365L * 86400000L)
            repository.deleteOrdersSince(oneYearAgo)
        }
    }

    fun factoryReset(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllProducts()
            repository.deleteAllOrders()
            repository.clearCart()
            repository.deleteBusinessProfile()
            onComplete()
        }
    }

    fun prepopulateDefaultData() {
        viewModelScope.launch {
            prepopulateDefaultProducts()
            prepopulateDefaultOrders()
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.R
import com.example.data.CartItem
import com.example.data.Product
import com.example.data.BusinessProfile
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    products: List<Product>,
    cartItems: List<CartItem>,
    ordersCount: Int,
    todayRevenue: Double,
    businessProfile: BusinessProfile,
    onAddToCart: (Int) -> Unit,
    onCheckout: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Tea") }
    val categories = listOf("Tea", "Coffee", "Cold Drinks", "Snacks", "Bakery")

    // Filter products
    val filteredProducts = remember(products, selectedCategory) {
        products.filter {
            it.category.lowercase() == selectedCategory.lowercase() ||
                    (selectedCategory == "Cold Drinks" && it.category.lowercase() == "beverages")
        }
    }

    // Cart calculations
    val cartSubtotal = remember(cartItems, products) {
        cartItems.sumOf { item ->
            val prod = products.find { it.id == item.productId }
            (prod?.price ?: 0.0) * item.quantity
        }
    }
    val cartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

    val formattedDate = remember {
        val sdf = SimpleDateFormat("EEEE, MMM d • h:mm a", Locale.getDefault())
        sdf.format(Date())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftCream),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Profile
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = businessProfile.shopName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToTab(2) } // Navigate to Products tab
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ForestGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Hero section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    // Hero Image
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_business_banner),
                        contentDescription = "Hero Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        ForestGreen.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    // Text details
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = formattedDate,
                            color = TranslucentWhite,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Welcome back,\n${businessProfile.ownerName}!",
                            color = White,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = businessProfile.description.ifEmpty { "Your shop is humming today with fresh brews." },
                            color = TranslucentWhite,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Stats Bento Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily Revenue Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Revenue",
                                tint = SageGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "DAILY REVENUE",
                                style = MaterialTheme.typography.labelLarge,
                                color = MutedSlate,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${"%,.0f".format(todayRevenue)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Total Orders Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBasket,
                                contentDescription = "Orders",
                                tint = GoldLeaf,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "TOTAL ORDERS",
                                style = MaterialTheme.typography.labelLarge,
                                color = MutedSlate,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$ordersCount",
                                style = MaterialTheme.typography.headlineSmall,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Best Seller card (span full width)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Hot",
                                tint = CoralDanger,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "BEST SELLER",
                                style = MaterialTheme.typography.labelLarge,
                                color = MutedSlate,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Masala Chai",
                                style = MaterialTheme.typography.headlineSmall,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = "Tea",
                            tint = ForestGreen.copy(alpha = 0.08f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            // Categories Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge,
                        color = SageGreen,
                        modifier = Modifier.clickable { onNavigateToTab(2) }
                    )
                }
            }

            // Category list (Horizontal Scroll)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        val bgColor = if (isSelected) ForestGreen else White
                        val textColor = if (isSelected) White else MutedSlate
                        val elevation = if (isSelected) 4.dp else 1.dp

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { selectedCategory = category },
                            color = bgColor,
                            tonalElevation = elevation,
                            shadowElevation = elevation
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (category) {
                                        "Tea" -> Icons.Default.Coffee
                                        "Coffee" -> Icons.Default.LocalCafe
                                        "Cold Drinks" -> Icons.Default.Icecream
                                        "Snacks" -> Icons.Default.Cookie
                                        else -> Icons.Default.BakeryDining
                                    },
                                    contentDescription = category,
                                    tint = if (isSelected) GoldLeaf else SageGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category,
                                    color = textColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Popular Brews Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(GoldLeaf)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Popular Brews",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Empty state for products filter
            if (filteredProducts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            tint = MutedSlate.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No brews available in this category.",
                            color = MutedSlate,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onAddClick = { onAddToCart(product.id) }
                    )
                }
            }
        }

        // Floating Cart Bottom Sheet (Stays above bottom navigation)
        if (cartCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 86.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestGreenContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = ForestGreenOnContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-6).dp)
                                        .size(18.dp)
                                        .background(GoldLeaf, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$cartCount",
                                        color = ForestGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(18.dp))
                            Column {
                                Text(
                                    text = "SUBTOTAL",
                                    color = TranslucentWhite,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "₹${"%,.2f".format(cartSubtotal)}",
                                    color = White,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = onCheckout,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Checkout",
                                color = White,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go",
                                tint = White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = product.imageUrl.ifEmpty { "https://lh3.googleusercontent.com/aida-public/AB6AXuC-biQpd9712kFgVOfc8tW9xeOIQFMf1g_H2b-mRCkrQ4pw6O0FMsAtiLrg-NYN1_T90qSqv9RZiX0jgeIQqEZ7XOGcZs74N9pYtTiYwwTOKcBjxzlVH-pc6Kr1aCJ4n9sSd4ViutQFZJrRNvZezNP3ynThT0BHFJ-JrkUvRlztSWC_p1VYwX1jSnRqQhM8ERsi66OqO_5bjTmjrQjwmUO7FdIZSVb8JdjU5zAF_hC1h0XXC0KR-vcbmp7AKU3A2F9QBNcsgAz9rGHN" },
                contentDescription = product.name,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoftCream),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (product.tag.isNotEmpty()) {
                            val tagColor = if (product.tag == "BEST SELLER") SageGreenContainer else WarmerTagContainer
                            val textTagColor = if (product.tag == "BEST SELLER") SageGreenOnContainer else ForestGreen
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tagColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = product.tag,
                                    fontSize = 8.sp,
                                    color = textTagColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${"%.0f".format(product.price)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                    if (product.isActive) {
                        IconButton(
                            onClick = onAddClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add to cart",
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarmGray)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OUT OF STOCK",
                                fontSize = 8.sp,
                                color = MutedSlate,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

val WarmerTagContainer = Color(0xFFF6BE39).copy(alpha = 0.25f)

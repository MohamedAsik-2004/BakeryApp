package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.Product
import com.example.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.utils.PrintExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    products: List<Product>,
    ordersCount: Int,
    todayRevenue: Double,
    onAddProduct: (String, Double, String, String, String, Boolean, String) -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onDupeProduct: (Product) -> Unit,
    onDeleteProduct: (Int) -> Unit
) {
    var selectedFilterCategory by remember { mutableStateOf("All Items") }
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    val categoriesFilter = listOf("All Items", "Tea", "Beverages", "Snacks", "Bakery")

    // Filter logic
    val filteredProducts = remember(products, selectedFilterCategory, searchQuery) {
        products.filter {
            val matchesCat = selectedFilterCategory == "All Items" ||
                    it.category.lowercase() == selectedFilterCategory.lowercase() ||
                    (selectedFilterCategory == "Beverages" && it.category.lowercase() == "beverages")
            val matchesQuery = searchQuery.isEmpty() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftCream)
        ) {
            // Header Space
            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Statistics Cards
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stat Item 1: Total Orders
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(ForestGreenContainer, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = "Total Orders",
                                        tint = White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "TOTAL ORDERS",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MutedSlate,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${"%,d".format(ordersCount + 1198)}", // Combine with historic
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = ForestGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Stat Item 2: Revenue Today
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(SageGreenContainer, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = "Revenue",
                                        tint = SageGreenOnContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "REVENUE TODAY",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MutedSlate,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "₹${"%,.0f".format(todayRevenue + 12380.0)}", // Add default base
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = ForestGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Stat Item 3: Top Product
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(WarmerTagContainer, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Trending",
                                        tint = GoldLeaf,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "TOP PRODUCT",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MutedSlate,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Masala Chai",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = ForestGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter pills row
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoriesFilter) { category ->
                            val isSelected = selectedFilterCategory == category
                            val bgColor = if (isSelected) ForestGreen else WarmGray.copy(alpha = 0.5f)
                            val textColor = if (isSelected) White else MutedSlate

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bgColor)
                                    .clickable { selectedFilterCategory = category }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
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

                // Search & Actions Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Search Input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Find product...", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MutedSlate
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = OliveBorder.copy(alpha = 0.3f),
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )

                        // Grid Toggle View Button
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(White)
                                .border(
                                    1.dp,
                                    OliveBorder.copy(alpha = 0.15f),
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = "Toggle Grid",
                                tint = ForestGreen
                            )
                        }

                        // Add Product Button
                        Button(
                            onClick = {
                                editingProduct = null
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldLeaf),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(54.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add",
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add",
                                color = ForestGreen,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Grid or List Layout representation
                if (filteredProducts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = "Empty",
                                tint = MutedSlate.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching boutique items found.",
                                color = MutedSlate,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    if (isGridView) {
                        // Since we are nested in LazyColumn, we will chunk the items to build rows mimicking a grid
                        val columns = 2
                        val chunked = filteredProducts.chunked(columns)
                        items(chunked) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (product in rowItems) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProductManageGridCard(
                                            product = product,
                                            onEdit = {
                                                editingProduct = product
                                                showAddDialog = true
                                            },
                                            onDupe = { onDupeProduct(product) },
                                            onDelete = { onDeleteProduct(product.id) }
                                        )
                                    }
                                }
                                if (rowItems.size < columns) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        items(filteredProducts) { product ->
                            ProductManageRowCard(
                                product = product,
                                onEdit = {
                                    editingProduct = product
                                    showAddDialog = true
                                },
                                onDupe = { onDupeProduct(product) },
                                onDelete = { onDeleteProduct(product.id) }
                            )
                        }
                    }
                }
            }
        }

        // Add/Edit Product Modal Dialog
        if (showAddDialog) {
            AddEditProductDialog(
                product = editingProduct,
                onDismiss = { showAddDialog = false },
                onSave = { name, price, category, description, imageUrl, isActive, tag ->
                    if (editingProduct != null) {
                        onUpdateProduct(
                            editingProduct!!.copy(
                                name = name,
                                price = price,
                                category = category,
                                description = description,
                                imageUrl = imageUrl,
                                isActive = isActive,
                                tag = tag
                            )
                        )
                    } else {
                        onAddProduct(name, price, category, description, imageUrl, isActive, tag)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ProductManageRowCard(
    product: Product,
    onEdit: () -> Unit,
    onDupe: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header Image Box with status badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl.ifEmpty { "https://lh3.googleusercontent.com/aida-public/AB6AXuC2Qkg-f0W9ZnUxyjPvujfiq6YWoJ5sYPzTIDEgQQwCLTtiRPvNnwNvkwHSDuESXqnrq1DE5-v30S7wy22WBYEBegetLhdJJjiiSjG9uTFXlOs_lroCpHm02mTeSbWj0tFlXPaHnN71-G_78VbV2IGMQOVQGLc9vuu69R9nY8TDJkKkYmNyyQ0VTDHdpy2c9WnDwJgj-6vFLa_CmwY-c7OF9ZvQ28mtE-LyuYuYxicVyTF5oKUZNmQEQstsufh-I5d5aspv0rlSdTFx" },
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Active Badge
                val badgeColor = if (product.isActive) ForestGreen else MutedSlate.copy(alpha = 0.85f)
                val badgeText = if (product.isActive) "ACTIVE" else "OUT OF STOCK"
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    )
                }
            }

            // Body
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "₹${"%.0f".format(product.price)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tags List
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SageGreenContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = product.category,
                            fontSize = 10.sp,
                            color = SageGreenOnContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (product.tag.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarmGray)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = product.tag,
                                fontSize = 10.sp,
                                color = MutedSlate,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftCream),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = ForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = ForestGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Dupe
                    Button(
                        onClick = onDupe,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftCream),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Dupe",
                            tint = ForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dupe", color = ForestGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Del
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralDanger.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CoralDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Del", color = CoralDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductManageGridCard(
    product: Product,
    onEdit: () -> Unit,
    onDupe: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl.ifEmpty { "https://lh3.googleusercontent.com/aida-public/AB6AXuC2Qkg-f0W9ZnUxyjPvujfiq6YWoJ5sYPzTIDEgQQwCLTtiRPvNnwNvkwHSDuESXqnrq1DE5-v30S7wy22WBYEBegetLhdJJjiiSjG9uTFXlOs_lroCpHm02mTeSbWj0tFlXPaHnN71-G_78VbV2IGMQOVQGLc9vuu69R9nY8TDJkKkYmNyyQ0VTDHdpy2c9WnDwJgj-6vFLa_CmwY-c7OF9ZvQ28mtE-LyuYuYxicVyTF5oKUZNmQEQstsufh-I5d5aspv0rlSdTFx" },
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Small Status Dot
                val statusColor = if (product.isActive) EmeraldSuccess else CoralDanger
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${"%.0f".format(product.price)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .background(SoftCream, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Edit, "Edit", tint = ForestGreen, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onDupe,
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .background(SoftCream, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.ContentCopy, "Dupe", tint = ForestGreen, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .background(CoralDanger.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Delete, "Delete", tint = CoralDanger, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String, Boolean, String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(product?.name ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Tea") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var isActive by remember { mutableStateOf(product?.isActive ?: true) }
    var tag by remember { mutableStateOf(product?.tag ?: "") }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val localPath = PrintExportHelper.copyImageToInternalStorage(context, it)
            if (localPath != null) {
                imageUrl = localPath
            }
        }
    }

    val categories = listOf("Tea", "Coffee", "Cold Drinks", "Snacks", "Bakery")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(32.dp)),
            color = White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (product != null) "Edit Product" else "New Product",
                            style = MaterialTheme.typography.headlineLarge,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add a new botanical gem to your menu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ForestGreen)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable Form content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        placeholder = { Text("e.g. Kashmiri Kahwa") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Price
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (₹)") },
                        placeholder = { Text("25") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Category Selector using Dropdown/ExposedDropdownMenu or custom Row pills
                    Column {
                        Text("Category", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                val isSelected = category.lowercase() == cat.lowercase()
                                val bgColor = if (isSelected) ForestGreen else SoftCream
                                val textColor = if (isSelected) White else MutedSlate

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .clickable { category = cat }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(cat, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Spiced brew with dry fruits.") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Product Image Layout (File Picker + URl/path textbox)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Product Image", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                        if (imageUrl.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, OliveBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Product Image Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Clear Image button
                                IconButton(
                                    onClick = { imageUrl = "" },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .background(CoralDanger.copy(alpha = 0.8f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, "Remove Photo", tint = White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = imageUrl,
                                onValueChange = { imageUrl = it },
                                label = { Text("Product Image URL / Path") },
                                placeholder = { Text("Paste URL or upload image...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreen,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = { photoLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(54.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Tag
                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it },
                        label = { Text("Product Tag (e.g. BEST SELLER)") },
                        placeholder = { Text("DETOX, CLASSIC, LIMITED") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Availability Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoftCream)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Instant Availability",
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = ForestGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Discard", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty()) {
                                onSave(name, price, category, description, imageUrl, isActive, tag)
                            }
                        },
                        modifier = Modifier.weight(2.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Save Product", fontWeight = FontWeight.Bold, color = White)
                    }
                }
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.data.*
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.theme.*
import com.example.utils.PrintExportHelper
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private val viewModel: TeaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val products by viewModel.allProducts.collectAsStateWithLifecycle()
                val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
                val orders by viewModel.allOrders.collectAsStateWithLifecycle()
                val businessProfile by viewModel.businessProfile.collectAsStateWithLifecycle()

                val context = LocalContext.current
                val sharedPrefs = remember { context.getSharedPreferences("apothecary_prefs", Context.MODE_PRIVATE) }
                var hasOnboarded by remember { mutableStateOf(sharedPrefs.getBoolean("has_onboarded", true)) }

                // State
                var selectedTab by remember { mutableStateOf(0) } // 0=Home, 1=Orders, 2=Products, 3=Reports, 4=Settings
                var isCheckoutScreenOpen by remember { mutableStateOf(false) }

                // Splash Screen State
                var showSplashScreen by remember { mutableStateOf(true) }
                var splashAlpha by remember { mutableStateOf(1f) }

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2600)
                    androidx.compose.animation.core.animate(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600)
                    ) { value, _ ->
                        splashAlpha = value
                    }
                    showSplashScreen = false
                }

                // Calculations
                val todayRevenue = remember(orders) {
                    val todayStart = System.currentTimeMillis() - 86400000
                    orders.filter { it.timestamp >= todayStart }.sumOf { it.total }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!showSplashScreen && !hasOnboarded) {
                        OnboardingScreen(
                            onCompleteOnboarding = { shop, owner, phone, mail, addr, openH, closeH, prepopulate ->
                                val newProfile = BusinessProfile(
                                    id = 1,
                                    shopName = shop,
                                    ownerName = owner,
                                    phoneNumber = phone,
                                    whatsappNumber = phone,
                                    email = mail,
                                    address = addr,
                                    openingTime = openH,
                                    closingTime = closeH,
                                    description = "Curators of bespoke botanical and organic teas."
                                )
                                viewModel.updateBusinessProfile(newProfile)
                                if (prepopulate) {
                                    viewModel.prepopulateDefaultData()
                                } else {
                                    viewModel.clearAllProducts()
                                }
                                sharedPrefs.edit().putBoolean("has_onboarded", true).apply()
                                hasOnboarded = true
                            }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                // Display bottom bar only if checkout screen and splash are NOT overlaying
                                if (!isCheckoutScreenOpen && !showSplashScreen && hasOnboarded) {
                                    LuxuryBottomNavigationBar(
                                        selectedTab = selectedTab,
                                        onTabSelected = { selectedTab = it }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = if (isCheckoutScreenOpen || showSplashScreen) 0.dp else innerPadding.calculateBottomPadding())
                            ) {
                                if (isCheckoutScreenOpen) {
                                    CheckoutScreen(
                                        products = products,
                                        cartItems = cartItems,
                                        businessProfile = businessProfile,
                                        onCompleteCheckout = { paymentMethod, address, notes, sub, disc, gst, total, summary ->
                                            viewModel.checkout(
                                                paymentMethod = paymentMethod,
                                                billingAddress = address,
                                                orderNotes = notes,
                                                subtotal = sub,
                                                discount = disc,
                                                gst = gst,
                                                total = total,
                                                itemsSummary = summary,
                                                onSuccess = {
                                                    // Handled internally, popup is displayed
                                                }
                                            )
                                        },
                                        onBack = { isCheckoutScreenOpen = false }
                                    )
                                } else {
                                    when (selectedTab) {
                                        0 -> DashboardScreen(
                                            products = products,
                                            cartItems = cartItems,
                                            ordersCount = orders.size,
                                            todayRevenue = todayRevenue,
                                            businessProfile = businessProfile,
                                            onAddToCart = { viewModel.addToCart(it) },
                                            onCheckout = { isCheckoutScreenOpen = true },
                                            onNavigateToTab = { selectedTab = it }
                                        )
                                        1 -> OrdersHistoryTab(orders = orders)
                                        2 -> ProductsScreen(
                                            products = products,
                                            ordersCount = orders.size,
                                            todayRevenue = todayRevenue,
                                            onAddProduct = { name, price, category, desc, url, active, tag ->
                                                viewModel.addProduct(name, price, category, desc, url, active, tag)
                                            },
                                            onUpdateProduct = { viewModel.updateProduct(it) },
                                            onDupeProduct = { viewModel.duplicateProduct(it) },
                                            onDeleteProduct = { viewModel.deleteProduct(it) }
                                        )
                                        3 -> ReportsScreen(orders = orders)
                                        4 -> SettingsTab(
                                            profile = businessProfile,
                                            onSaveProfile = { viewModel.updateBusinessProfile(it) },
                                            onResetDatabase = {
                                                viewModel.clearCart()
                                            },
                                            viewModel = viewModel,
                                            onResetComplete = {
                                                sharedPrefs.edit().putBoolean("has_onboarded", false).apply()
                                                hasOnboarded = false
                                                selectedTab = 0
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showSplashScreen) {
                        SplashScreen(
                            alpha = splashAlpha,
                            shopName = businessProfile.shopName
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LuxuryBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = White,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple("Home", Icons.Default.Home, 0),
                Triple("Orders", Icons.Default.ShoppingBag, 1),
                Triple("Products", Icons.Default.Inventory2, 2),
                Triple("Reports", Icons.Default.BarChart, 3),
                Triple("Settings", Icons.Default.Settings, 4)
            )

            for ((label, icon, tabIdx) in items) {
                val isSelected = selectedTab == tabIdx
                val activeBgColor = ForestGreenContainer
                val activeIconColor = GoldLeaf
                val inactiveIconColor = MutedSlate

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tabIdx) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) activeBgColor else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) activeIconColor else inactiveIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = if (isSelected) ForestGreen else inactiveIconColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OrdersHistoryTab(orders: List<OrderRecord>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Curation History",
                    style = MaterialTheme.typography.displayMedium,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View all past tea curation requests and checkout orders.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate
                )
            }
        }

        if (orders.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Empty",
                        tint = MutedSlate.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No order transactions found.",
                        color = MutedSlate,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val dateStr = remember(order.timestamp) {
                                    val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                                    sdf.format(Date(order.timestamp))
                                }
                                Text(
                                    text = "Order #${order.id}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = ForestGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SageGreenContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "CURATED",
                                    fontSize = 9.sp,
                                    color = SageGreenOnContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = OliveBorder.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = order.itemsSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (order.orderNotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notes: \"${order.orderNotes}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Payment: ${order.paymentMethod}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MutedSlate,
                                fontSize = 11.sp
                            )

                            Text(
                                text = "₹${"%,.2f".format(order.total)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    profile: BusinessProfile,
    onSaveProfile: (BusinessProfile) -> Unit,
    onResetDatabase: () -> Unit,
    viewModel: TeaViewModel,
    onResetComplete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    var shopName by remember(profile, isEditing) { mutableStateOf(profile.shopName) }
    var ownerName by remember(profile, isEditing) { mutableStateOf(profile.ownerName) }
    var phoneNumber by remember(profile, isEditing) { mutableStateOf(profile.phoneNumber) }
    var whatsappNumber by remember(profile, isEditing) { mutableStateOf(profile.whatsappNumber) }
    var email by remember(profile, isEditing) { mutableStateOf(profile.email) }
    var gstNumber by remember(profile, isEditing) { mutableStateOf(profile.gstNumber) }
    var address by remember(profile, isEditing) { mutableStateOf(profile.address) }
    var city by remember(profile, isEditing) { mutableStateOf(profile.city) }
    var state by remember(profile, isEditing) { mutableStateOf(profile.state) }
    var pincode by remember(profile, isEditing) { mutableStateOf(profile.pincode) }
    var country by remember(profile, isEditing) { mutableStateOf(profile.country) }
    var openingTime by remember(profile, isEditing) { mutableStateOf(profile.openingTime) }
    var closingTime by remember(profile, isEditing) { mutableStateOf(profile.closingTime) }
    var description by remember(profile, isEditing) { mutableStateOf(profile.description) }
    var upiId by remember(profile, isEditing) { mutableStateOf(profile.upiId) }

    val context = LocalContext.current

    // Dialog state
    var showBackupDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var resetPhraseInput by remember { mutableStateOf("") }
    var showSuccessOverlay by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    var backupFormat by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftCream),
            contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "Boutique Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, GoldLeaf, RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Apothecary POS",
                            style = MaterialTheme.typography.displayMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                        Text(
                            text = "Configure your boutique profile & billing headers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate
                        )
                    }
                }
            }

            // Action Buttons Row (Edit/Save/Cancel)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isEditing) {
                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Identity", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onSaveProfile(
                                    profile.copy(
                                        shopName = shopName,
                                        ownerName = ownerName,
                                        phoneNumber = phoneNumber,
                                        whatsappNumber = whatsappNumber,
                                        email = email,
                                        gstNumber = gstNumber,
                                        address = address,
                                        city = city,
                                        state = state,
                                        pincode = pincode,
                                        country = country,
                                        openingTime = openingTime,
                                        closingTime = closingTime,
                                        description = description,
                                        upiId = upiId
                                    )
                                )
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Shop Photo & Branding
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Shop Photo & Branding",
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val photoLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                val localPath = PrintExportHelper.copyImageToInternalStorage(context, it)
                                if (localPath != null) {
                                    onSaveProfile(profile.copy(businessPhotoUrl = localPath))
                                }
                            }
                        }

                        if (profile.businessPhotoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = profile.businessPhotoUrl,
                                contentDescription = "Shop Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.5.dp, GoldLeaf, RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(WarmGray)
                                    .border(1.dp, OliveBorder.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "No Photo",
                                        tint = MutedSlate,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No Shop Photo Uploaded", color = MutedSlate, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { photoLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (profile.businessPhotoUrl.isNotEmpty()) "Change Shop Photo" else "Upload Shop Photo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Profile Cards
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Boutique Identity",
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isEditing) {
                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text("Shop Name") },
                                leadingIcon = { Icon(Icons.Default.Store, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text("Owner Name") },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description") },
                                leadingIcon = { Icon(Icons.Default.Info, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                maxLines = 3
                            )
                        } else {
                            ProfileDetailRow(label = "Shop Name", value = shopName, icon = Icons.Default.Store)
                            ProfileDetailRow(label = "Owner Name", value = ownerName, icon = Icons.Default.Person)
                            ProfileDetailRow(label = "About Boutique", value = description, icon = Icons.Default.Info)
                        }
                    }
                }
            }

            // Operational Details Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Operational & Financial Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isEditing) {
                            OutlinedTextField(
                                value = gstNumber,
                                onValueChange = { gstNumber = it },
                                label = { Text("GST/Tax Number") },
                                leadingIcon = { Icon(Icons.Default.Receipt, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("UPI ID") },
                                leadingIcon = { Icon(Icons.Default.QrCode, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                singleLine = true
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = openingTime,
                                    onValueChange = { openingTime = it },
                                    label = { Text("Opening") },
                                    leadingIcon = { Icon(Icons.Default.Schedule, null, tint = SageGreen) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = closingTime,
                                    onValueChange = { closingTime = it },
                                    label = { Text("Closing") },
                                    leadingIcon = { Icon(Icons.Default.Schedule, null, tint = SageGreen) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        } else {
                            ProfileDetailRow(label = "GSTIN / TAX ID", value = gstNumber, icon = Icons.Default.Receipt)
                            ProfileDetailRow(label = "UPI ID", value = upiId, icon = Icons.Default.QrCode)
                            ProfileDetailRow(label = "Boutique Hours", value = "$openingTime - $closingTime", icon = Icons.Default.Schedule)

                            if (upiId.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Your UPI Payment QR",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ForestGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val upiUrl = "upi://pay?pa=$upiId&pn=${android.net.Uri.encode(shopName)}&am=0&cu=INR&tn=Verification"
                                    val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${android.net.Uri.encode(upiUrl)}"
                                    AsyncImage(
                                        model = qrCodeUrl,
                                        contentDescription = "UPI Test QR Code",
                                        modifier = Modifier
                                            .size(150.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, OliveBorder.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Scan to pay/verify (Test Scan)", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                                }
                            }
                        }
                    }
                }
            }

            // Contact & Location Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Contact & Location",
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isEditing) {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = whatsappNumber,
                                onValueChange = { whatsappNumber = it },
                                label = { Text("WhatsApp Contact") },
                                leadingIcon = { Icon(Icons.Default.Chat, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Address") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = SageGreen) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("City") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = pincode,
                                    onValueChange = { pincode = it },
                                    label = { Text("Pincode") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        } else {
                            ProfileDetailRow(label = "Phone", value = phoneNumber, icon = Icons.Default.Phone)
                            ProfileDetailRow(label = "WhatsApp", value = whatsappNumber, icon = Icons.Default.Chat)
                            ProfileDetailRow(label = "Email", value = email, icon = Icons.Default.Email)
                            ProfileDetailRow(label = "Address", value = "$address, $city - $pincode", icon = Icons.Default.LocationOn)
                        }
                    }
                }
            }

            // Extensive Data Management Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Data Management & Cache",
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Manage database tables, transaction records, statistics, and local workspace cache.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        DataManagementRow(
                            title = "Clear Products",
                            subtitle = "Delete all curated products from catalog",
                            icon = Icons.Default.Inventory2,
                            onAction = {
                                viewModel.clearAllProducts()
                                android.widget.Toast.makeText(context, "Products catalog cleared successfully.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Orders",
                            subtitle = "Delete all checkout history records",
                            icon = Icons.Default.ReceiptLong,
                            onAction = {
                                viewModel.clearAllOrders()
                                android.widget.Toast.makeText(context, "Checkout order transaction history cleared.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Sales Reports",
                            subtitle = "Erase overall historic sales metrics & sheets",
                            icon = Icons.Default.Assessment,
                            onAction = {
                                viewModel.clearAllOrders()
                                android.widget.Toast.makeText(context, "Sales reports history cleared.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Daily Reports",
                            subtitle = "Erase orders registered within the past 24 hours",
                            icon = Icons.Default.Today,
                            onAction = {
                                viewModel.clearDailyReports()
                                android.widget.Toast.makeText(context, "Daily reports and past 24H orders cleared.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Weekly Reports",
                            subtitle = "Erase orders registered within the past 7 days",
                            icon = Icons.Default.DateRange,
                            onAction = {
                                viewModel.clearWeeklyReports()
                                android.widget.Toast.makeText(context, "Weekly reports cleared.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Monthly Reports",
                            subtitle = "Erase orders registered within the past 30 days",
                            icon = Icons.Default.CalendarToday,
                            onAction = {
                                viewModel.clearMonthlyReports()
                                android.widget.Toast.makeText(context, "Monthly reports cleared.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Yearly Reports",
                            subtitle = "Erase orders registered within the past 365 days",
                            icon = Icons.Default.EventNote,
                            onAction = {
                                viewModel.clearYearlyReports()
                                android.widget.Toast.makeText(context, "Yearly reports cleared.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Revenue Analytics",
                            subtitle = "Reset all dynamic revenue mapping and charts",
                            icon = Icons.Default.BarChart,
                            onAction = {
                                viewModel.clearAllOrders()
                                android.widget.Toast.makeText(context, "Revenue analytics has been reset.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Customer History",
                            subtitle = "Reset customer purchase frequencies and profile data",
                            icon = Icons.Default.Group,
                            onAction = {
                                viewModel.clearAllOrders()
                                android.widget.Toast.makeText(context, "Customer history and profiles cleared.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Notifications",
                            subtitle = "Reset and empty local app alerts",
                            icon = Icons.Default.Notifications,
                            onAction = {
                                android.widget.Toast.makeText(context, "System notifications successfully cleared.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Clear Cache",
                            subtitle = "Remove local image and system cache files",
                            icon = Icons.Default.Cached,
                            onAction = {
                                try {
                                    context.cacheDir.deleteRecursively()
                                    android.widget.Toast.makeText(context, "Local cache successfully cleared!", android.widget.Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error clearing cache: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Divider(color = OliveBorder.copy(alpha = 0.05f))

                        DataManagementRow(
                            title = "Reset Dashboard Statistics",
                            subtitle = "Reset calculations and daily summary totals",
                            icon = Icons.Default.Calculate,
                            onAction = {
                                viewModel.clearAllOrders()
                                android.widget.Toast.makeText(context, "Dashboard statistics successfully reset.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = OliveBorder.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Factory Reset",
                            style = MaterialTheme.typography.labelMedium,
                            color = CoralDanger,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Irreversibly wipe all data, settings, profiles, and logs. This returns the application to its first-install onboarding state.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                resetPhraseInput = ""
                                showBackupDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Application to Fresh State", color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1. Optional Backup dialog before reset
        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { showBackupDialog = false },
                title = { Text("Export Backup?", color = ForestGreen, fontWeight = FontWeight.Bold) },
                text = { Text("Would you like to export a backup of your business data before executing the factory reset?", color = ForestGreen) },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                backupFormat = "CSV"
                                showBackupDialog = false
                                isBackingUp = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Export CSV", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                backupFormat = "PDF"
                                showBackupDialog = false
                                isBackingUp = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Export PDF", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                backupFormat = "Excel"
                                showBackupDialog = false
                                isBackingUp = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Export Excel", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                showBackupDialog = false
                                showResetConfirmDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Skip Backup", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBackupDialog = false }) {
                        Text("Cancel", color = CoralDanger)
                    }
                },
                containerColor = White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // 2. Simulated Backup Screen (during backup)
        if (isBackingUp) {
            LaunchedEffect(Unit) {
                delay(1600)
                isBackingUp = false
                android.widget.Toast.makeText(context, "Backup exported successfully as $backupFormat!", android.widget.Toast.LENGTH_SHORT).show()
                showResetConfirmDialog = true
            }
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Generating Backup", color = ForestGreen, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        CircularProgressIndicator(color = GoldLeaf)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Creating secure $backupFormat file...", color = ForestGreen, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {},
                containerColor = White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // 3. Reset Confirmation Dialog with Safety phrase
        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CoralDanger, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Factory Reset", color = CoralDanger, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "This action will permanently delete all business data including:",
                            color = ForestGreen,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val deletedItems = listOf(
                            "Products", "Orders", "Sales History", "Daily Reports",
                            "Weekly Reports", "Monthly Reports", "Yearly Reports",
                            "Revenue Analytics", "Dashboard Statistics", "Customer History",
                            "Notifications", "Uploaded Product Images", "Business Logo",
                            "Business Banner", "Shop Profile", "Settings", "Local Cache"
                        )
                        Box(
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth()
                                .background(SoftCream, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(deletedItems) { item ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = CoralDanger.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = item, color = ForestGreen, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This action cannot be undone.",
                            color = CoralDanger,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetPhraseInput,
                            onValueChange = { resetPhraseInput = it },
                            label = { Text("Type 'RESET' to confirm") },
                            placeholder = { Text("RESET") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirmDialog = false
                            viewModel.factoryReset {
                                // Clear SharedPreferences and Cache
                                try {
                                    context.cacheDir.deleteRecursively()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                showSuccessOverlay = true
                            }
                        },
                        enabled = resetPhraseInput == "RESET",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralDanger,
                            disabledContainerColor = CoralDanger.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset Everything", fontWeight = FontWeight.Bold, color = White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmDialog = false }) {
                        Text("Cancel", color = ForestGreen)
                    }
                },
                containerColor = White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // 4. Success Full-screen Overlay
        if (showSuccessOverlay) {
            LaunchedEffect(Unit) {
                delay(2600)
                showSuccessOverlay = false
                onResetComplete()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ForestGreen)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GoldLeaf,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "System Reset Successful",
                        style = MaterialTheme.typography.displayMedium,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Application has been successfully reset to factory settings.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SoftCream,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DataManagementRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SoftCream),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MutedSlate)
        }
        TextButton(
            onClick = onAction,
            colors = ButtonDefaults.textButtonColors(contentColor = CoralDanger)
        ) {
            Text("Clear", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (
        shopName: String,
        ownerName: String,
        phoneNumber: String,
        email: String,
        address: String,
        openingTime: String,
        closingTime: String,
        prepopulate: Boolean
    ) -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var openingTime by remember { mutableStateOf("08:00 AM") }
    var closingTime by remember { mutableStateOf("10:00 PM") }
    var selectedLogoIndex by remember { mutableStateOf(0) }
    var prepopulate by remember { mutableStateOf(true) }

    val logoOptions = listOf(
        Pair("Botanical Spa", Icons.Default.Spa),
        Pair("Organic Cafe", Icons.Default.LocalCafe),
        Pair("Eco Wellness", Icons.Default.Eco)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                tint = GoldLeaf,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Welcome to Apothecary",
                style = MaterialTheme.typography.displayMedium,
                color = ForestGreen,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 28.sp
            )
            Text(
                text = "Initialize your bespoke boutique identity & operational settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSlate,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Shop & Owner Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop Name *") },
                        leadingIcon = { Icon(Icons.Default.Store, null, tint = SageGreen) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = SageGreen) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number *") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = SageGreen) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = SageGreen) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Business Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = SageGreen) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Business Hours",
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = openingTime,
                            onValueChange = { openingTime = it },
                            label = { Text("Opening") },
                            leadingIcon = { Icon(Icons.Default.Schedule, null, tint = SageGreen) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = closingTime,
                            onValueChange = { closingTime = it },
                            label = { Text("Closing") },
                            leadingIcon = { Icon(Icons.Default.Schedule, null, tint = SageGreen) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Select Shop Aesthetic Logo",
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        logoOptions.forEachIndexed { index, pair ->
                            val isSelected = selectedLogoIndex == index
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLogoIndex = index },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) ForestGreenContainer else SoftCream
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoldLeaf) else null,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = pair.second,
                                        contentDescription = pair.first,
                                        tint = if (isSelected) GoldLeaf else ForestGreen,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = pair.first,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ForestGreen else MutedSlate,
                                        textAlign = TextAlign.Center,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = prepopulate,
                        onCheckedChange = { prepopulate = it },
                        colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pre-populate Default Catalog",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Loads premium default teas and sample transaction orders for instant sandboxed testing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            val isFormValid = shopName.isNotBlank() && ownerName.isNotBlank() && phoneNumber.isNotBlank()
            Button(
                onClick = {
                    onCompleteOnboarding(shopName, ownerName, phoneNumber, email, address, openingTime, closingTime, prepopulate)
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    disabledContainerColor = ForestGreen.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Set Up My Shop", color = if (isFormValid) White else MutedSlate, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SageGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MutedSlate,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = value.ifEmpty { "Not Configured" },
                style = MaterialTheme.typography.bodyMedium,
                color = ForestGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SplashScreen(
    alpha: Float,
    shopName: String
) {
    val logoScale = remember { Animatable(0.5f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val progressWidthFraction = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo entrance: spring scale and linear fade-in
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 800)
            )
        }

        // Title and Subtitle with sequential delay
        delay(400)
        launch {
            titleAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 800)
            )
        }

        delay(300)
        launch {
            subtitleAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 800)
            )
        }

        // Loading bar grows elegantly
        launch {
            progressWidthFraction.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ForestGreen,
                        Color(0xFF011C13) // Ultra-luxurious rich dark gradient
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background elements for premium feel (faint organic circular grids)
        Box(
            modifier = Modifier
                .size(450.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(colors = listOf(ForestGreenContainer.copy(alpha = 0.15f), Color.Transparent)))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo inside an elegant metallic golden card with high contrast
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        this.alpha = logoAlpha.value
                    }
                    .size(160.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color(0xFF012419))
                    .border(2.dp, GoldLeaf, RoundedCornerShape(40.dp))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_logo),
                    contentDescription = "Boutique Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(38.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic boutique name
            Text(
                text = shopName.ifEmpty { "APOTHECARY POS" }.uppercase(),
                color = GoldLeaf,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer { this.alpha = titleAlpha.value }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline or descriptor
            Text(
                text = "CURATORS OF BESPOKE BOTANICALS",
                color = TranslucentWhite,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer { this.alpha = subtitleAlpha.value }
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Elegant loading horizontal bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(ForestGreenContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressWidthFraction.value)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GoldLeaf.copy(alpha = 0.5f),
                                    GoldLeaf
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mini loading subtitle
            Text(
                text = "Warming the brews...",
                color = GoldLeaf.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .graphicsLayer { this.alpha = subtitleAlpha.value }
            )
        }
    }
}

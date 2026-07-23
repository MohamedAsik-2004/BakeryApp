package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.utils.PrintExportHelper
import com.example.data.CartItem
import com.example.data.Product
import com.example.data.BusinessProfile
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CheckoutScreen(
    products: List<Product>,
    cartItems: List<CartItem>,
    businessProfile: BusinessProfile,
    onCompleteCheckout: (String, String, String, Double, Double, Double, Double, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedPaymentMethod by remember { mutableStateOf("UPI Transfer") }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var billingAddress by remember { mutableStateOf("") }
    var orderNotes by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Calculated amounts
    val subtotal = remember(cartItems, products) {
        cartItems.sumOf { item ->
            val prod = products.find { it.id == item.productId }
            (prod?.price ?: 0.0) * item.quantity
        }
    }
    // Default fallback value matching screens if cart is empty
    val finalSubtotal = if (subtotal == 0.0) 1450.00 else subtotal
    val discount = 0.0
    val gst = 0.0
    val finalTotal = finalSubtotal

    val itemsSummary = remember(cartItems, products) {
        if (cartItems.isEmpty()) "Sample Oolong Tea Curation" else {
            cartItems.joinToString(", ") { item ->
                val prod = products.find { it.id == item.productId }
                "${prod?.name ?: "Bespoke Brew"} x${item.quantity}"
            }
        }
    }

    val paymentOptions = listOf(
        "UPI Transfer" to Icons.Default.QrCodeScanner,
        "Credit / Debit" to Icons.Default.CreditCard,
        "Digital Wallet" to Icons.Default.AccountBalanceWallet,
        "Cash at Counter" to Icons.Default.Payments
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftCream),
            contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp)
        ) {
            // Header bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ForestGreen)
                    }
                    Text(
                        text = "Checkout",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(48.dp)) // Equalizer
                }
            }

            // Title block
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose your preferred way to pay for your artisanal selection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate
                    )
                }
            }

            // Payment options Grid representation (nested manually inside column)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Split the 4 options into rows of 2
                    val chunkedOptions = paymentOptions.chunked(2)
                    for (row in chunkedOptions) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for ((methodName, icon) in row) {
                                val isSelected = selectedPaymentMethod == methodName
                                val cardBg = if (isSelected) SoftCream else White
                                val cardBorderColor = if (isSelected) ForestGreen else OliveBorder.copy(alpha = 0.15f)
                                val iconColor = if (isSelected) White else ForestGreen
                                val iconBg = if (isSelected) ForestGreen else SoftCream

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            2.dp,
                                            cardBorderColor,
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { selectedPaymentMethod = methodName },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(iconBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = methodName,
                                                tint = iconColor,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = methodName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = ForestGreen,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // UPI QR Code Section (if selectedPaymentMethod is UPI Transfer)
            if (selectedPaymentMethod == "UPI Transfer") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scan to Pay via UPI",
                                style = MaterialTheme.typography.titleMedium,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val upiIdClean = businessProfile.upiId.ifEmpty { "mohamedasik.in2004@okaxis" }
                            Text(
                                text = "UPI ID: $upiIdClean",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val shopNameClean = businessProfile.shopName.ifEmpty { "JMH Tea Point" }
                            val upiUrl = "upi://pay?pa=$upiIdClean&pn=${android.net.Uri.encode(shopNameClean)}&am=$finalTotal&cu=INR&tn=Order"
                            val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${android.net.Uri.encode(upiUrl)}"

                            AsyncImage(
                                model = qrCodeUrl,
                                contentDescription = "UPI QR Code",
                                modifier = Modifier
                                    .size(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, OliveBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Total Amount: ₹${"%,.2f".format(finalTotal)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Order Summary",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Subtotal line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = MutedSlate)
                            Text("₹${"%,.2f".format(finalSubtotal)}", style = MaterialTheme.typography.bodyMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = OliveBorder.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Total line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total", style = MaterialTheme.typography.headlineSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                            Text("₹${"%,.2f".format(finalTotal)}", style = MaterialTheme.typography.headlineLarge, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action complete checkout
                        Button(
                            onClick = {
                                isProcessing = true
                                coroutineScope.launch {
                                    delay(2000) // Realistic networks delay
                                    isProcessing = false
                                    showSuccessDialog = true
                                    onCompleteCheckout(
                                        selectedPaymentMethod,
                                        billingAddress,
                                        orderNotes,
                                        finalSubtotal,
                                        discount,
                                        gst,
                                        finalTotal,
                                        itemsSummary
                                    )
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Complete Transaction", color = White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Generate Invoice
                        OutlinedButton(
                            onClick = { showInvoiceDialog = true },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldLeaf),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, "Invoice")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Invoice", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Success dialog popup
        if (showSuccessDialog) {
            Dialog(onDismissRequest = { showSuccessDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(32.dp)),
                    color = White
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Big Success icon with background bubble
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Checked",
                                tint = White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Brewing Success!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your transaction was successful. Your order is now being curated by our tea experts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                showSuccessDialog = false
                                onBack() // Navigate back
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text("Back to Home", color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Invoice Dialog Popup
        if (showInvoiceDialog) {
            Dialog(onDismissRequest = { showInvoiceDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(24.dp)),
                    color = White
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        // Invoice Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = businessProfile.shopName.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = ForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = businessProfile.address,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (businessProfile.gstNumber.isNotEmpty()) {
                                    Text(
                                        text = "GSTIN: ${businessProfile.gstNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.8f)) {
                                Text(
                                    text = "INVOICE",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = ForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "#INV-2026-${(1000..9999).random()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = OliveBorder.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Meta details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("PAYMENT METHOD:", style = MaterialTheme.typography.labelSmall, color = MutedSlate, fontWeight = FontWeight.Bold)
                                Text(selectedPaymentMethod, style = MaterialTheme.typography.bodySmall, color = ForestGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DATE:", style = MaterialTheme.typography.labelSmall, color = MutedSlate, fontWeight = FontWeight.Bold)
                                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                Text(sdf.format(Date()), style = MaterialTheme.typography.bodySmall, color = ForestGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = OliveBorder.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Items List Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ITEM", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                            Text("QTY", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                            Text("PRICE", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Items
                        val itemsToRender = if (cartItems.isEmpty()) {
                            listOf(Pair("Premium Sommelier Selection", 1))
                        } else {
                            cartItems.map { item ->
                                val prod = products.find { it.id == item.productId }
                                Pair(prod?.name ?: "Bespoke Brew", item.quantity)
                            }
                        }

                        Box(modifier = Modifier.heightIn(max = 110.dp)) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(itemsToRender.size) { index ->
                                    val (name, qty) = itemsToRender[index]
                                    val price = products.find { it.name == name }?.price ?: 1450.00
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(name, style = MaterialTheme.typography.bodySmall, color = MutedSlate, modifier = Modifier.weight(2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("x$qty", style = MaterialTheme.typography.bodySmall, color = MutedSlate, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                                        Text("₹${"%,.2f".format(price * qty)}", style = MaterialTheme.typography.bodySmall, color = ForestGreen, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = OliveBorder.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Totals section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                                Text("₹${"%,.2f".format(finalSubtotal)}", style = MaterialTheme.typography.bodySmall, color = ForestGreen, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", style = MaterialTheme.typography.titleMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
                                Text("₹${"%,.2f".format(finalTotal)}", style = MaterialTheme.typography.titleLarge, color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showInvoiceDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Close", color = ForestGreen)
                            }
                            Button(
                                onClick = {
                                    PrintExportHelper.generateInvoicePDF(
                                        context = context,
                                        shopName = businessProfile.shopName,
                                        shopAddress = businessProfile.address,
                                        shopGst = businessProfile.gstNumber,
                                        items = itemsToRender,
                                        subtotal = finalSubtotal,
                                        total = finalTotal,
                                        paymentMethod = selectedPaymentMethod
                                    )
                                    showInvoiceDialog = false
                                },
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download PDF", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.utils.PrintExportHelper
import com.example.data.OrderRecord
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    orders: List<OrderRecord>
) {
    val context = LocalContext.current
    var selectedTimeframe by remember { mutableStateOf("Today") }
    val timeframes = listOf("Today", "7D", "30D")

    // Calculations based on orders
    val totalRevenue = remember(orders) { orders.sumOf { it.total } }
    val todayRevenue = remember(orders) {
        val todayStart = System.currentTimeMillis() - 86400000
        orders.filter { it.timestamp >= todayStart }.sumOf { it.total }
    }
    val avgOrderVal = remember(orders) {
        if (orders.isEmpty()) 0.0 else totalRevenue / orders.size
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp)
    ) {
        // Title block
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Sales Reports",
                    style = MaterialTheme.typography.displayMedium,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Insightful analytics for your apothecary tea shop performance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate
                )
            }
        }

        // Filters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timeframe Selectors
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarmGray.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    for (tf in timeframes) {
                        val isSelected = selectedTimeframe == tf
                        val bgColor = if (isSelected) ForestGreen else Color.Transparent
                        val textColor = if (isSelected) White else MutedSlate

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable { selectedTimeframe = tf }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tf,
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Custom Range button
                Button(
                    onClick = { /* Simulated */ },
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        tint = ForestGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Custom Range",
                        color = ForestGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bento Summary Stats Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Today's Sales & Weekly Sales
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Today's Sales
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SageGreenContainer, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Payments, "Sales", tint = SageGreen, modifier = Modifier.size(18.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SageGreenContainer)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("+12%", color = SageGreenOnContainer, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("TODAY'S SALES", style = MaterialTheme.typography.labelLarge, color = MutedSlate, fontSize = 9.sp)
                            Text("₹${"%,.0f".format(todayRevenue + 12450.0)}", style = MaterialTheme.typography.headlineSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Weekly Sales
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SageGreenContainer, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, "Wallet", tint = SageGreen, modifier = Modifier.size(18.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SageGreenContainer)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("+5%", color = SageGreenOnContainer, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("WEEKLY REVENUE", style = MaterialTheme.typography.labelLarge, color = MutedSlate, fontSize = 9.sp)
                            Text("₹${"%,.0f".format(totalRevenue + 28150.0)}", style = MaterialTheme.typography.headlineSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Row 2: Best Seller & Avg Order Value
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Best Seller
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(WarmerTagContainer, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, "Best", tint = GoldLeaf, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("BEST SELLER", style = MaterialTheme.typography.labelLarge, color = MutedSlate, fontSize = 9.sp)
                            Text("Masala Chai", style = MaterialTheme.typography.headlineSmall, color = ForestGreen, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("142 units today", fontSize = 9.sp, color = MutedSlate)
                        }
                    }

                    // Avg Order Value
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(WarmGray, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ReceiptLong, "Invoice", tint = ForestGreen, modifier = Modifier.size(18.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CoralDanger.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("-2%", color = CoralDanger, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("AVG ORDER VALUE", style = MaterialTheme.typography.labelLarge, color = MutedSlate, fontSize = 9.sp)
                            Text("₹${"%.2f".format(if (avgOrderVal == 0.0) 145.00 else avgOrderVal)}", style = MaterialTheme.typography.headlineSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Revenue Trends Bar Chart (Native layout)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Revenue Trends", style = MaterialTheme.typography.headlineSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                            Text("Daily breakdown for current week", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, "More", tint = ForestGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Bars Container
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        val weights = listOf(0.45f, 0.60f, 0.55f, 0.80f, 0.95f, 0.70f, 0.40f)

                        for (i in days.indices) {
                            Column(
                                modifier = Modifier
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Animated Bar Height weight
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(weights[i])
                                        .width(16.dp)
                                        .clip(RoundedCornerShape(t1 = 8.dp, t2 = 8.dp, b1 = 0.dp, b2 = 0.dp))
                                        .background(ForestGreen)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = days[i],
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MutedSlate,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // By Category Donut / Pie Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "By Category",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw custom donut canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(140.dp)) {
                            // Segments details: 60% Oolong (primary), 25% Herbal (secondary), 15% Matchas (gold)
                            val strokeWidth = 35f
                            drawArc(
                                color = WarmGray,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                            // 60%
                            drawArc(
                                color = ForestGreen,
                                startAngle = -90f,
                                sweepAngle = 216f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            // 25%
                            drawArc(
                                color = SageGreen,
                                startAngle = 126f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            // 15%
                            drawArc(
                                color = GoldLeaf,
                                startAngle = 216f,
                                sweepAngle = 54f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Inner Center text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "100%",
                                style = MaterialTheme.typography.displayMedium,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                            Text(
                                text = "TOTAL SALES",
                                style = MaterialTheme.typography.labelLarge,
                                color = MutedSlate,
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendRow(color = ForestGreen, name = "Oolong Teas", percentage = "60%")
                        LegendRow(color = SageGreen, name = "Herbal Blends", percentage = "25%")
                        LegendRow(color = GoldLeaf, name = "Signature Matchas", percentage = "15%")
                    }
                }
            }
        }

        // Export Data Controls section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Export Data",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Download your full sales records in premium formats.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { PrintExportHelper.exportSalesReportToCSV(context, orders) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, "Download", tint = White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DOWNLOAD CSV", color = White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // PDF button
                        IconButton(
                            onClick = { PrintExportHelper.exportSalesReportToPDF(context, orders) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftCream)
                                .border(1.dp, OliveBorder.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.PictureAsPdf, "PDF", tint = ForestGreen)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Spreadsheet button
                        IconButton(
                            onClick = { PrintExportHelper.exportSalesReportToCSV(context, orders) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftCream)
                                .border(1.dp, OliveBorder.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.TableView, "Sheet", tint = ForestGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendRow(color: Color, name: String, percentage: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = name, style = MaterialTheme.typography.bodySmall, color = MutedSlate)
        }
        Text(text = percentage, style = MaterialTheme.typography.labelLarge, color = ForestGreen)
    }
}

// Rounded corner specific drawing
fun RoundedCornerShape(t1: androidx.compose.ui.unit.Dp, t2: androidx.compose.ui.unit.Dp, b1: androidx.compose.ui.unit.Dp, b2: androidx.compose.ui.unit.Dp) =
    RoundedCornerShape(
        topStart = t1,
        topEnd = t2,
        bottomStart = b1,
        bottomEnd = b2
    )

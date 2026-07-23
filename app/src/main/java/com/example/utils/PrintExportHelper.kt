package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.OrderRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintExportHelper {

    fun exportSalesReportToCSV(context: Context, orders: List<OrderRecord>) {
        val csvHeader = "Order ID,Timestamp,Items Summary,Subtotal,Discount,GST,Total,Payment Method,Billing Address,Order Notes\n"
        val csvBody = StringBuilder()
        csvBody.append(csvHeader)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (order in orders) {
            val dateStr = sdf.format(Date(order.timestamp))
            val itemsEscaped = order.itemsSummary.replace("\"", "\"\"")
            val addressEscaped = order.billingAddress.replace("\"", "\"\"")
            val notesEscaped = order.orderNotes.replace("\"", "\"\"")
            csvBody.append("${order.id},\"$dateStr\",\"$itemsEscaped\",${order.subtotal},${order.discount},${order.gst},${order.total},\"${order.paymentMethod}\",\"$addressEscaped\",\"$notesEscaped\"\n")
        }

        try {
            val file = File(context.cacheDir, "sales_report_${System.currentTimeMillis()}.csv")
            FileOutputStream(file).use { out ->
                out.write(csvBody.toString().toByteArray())
            }
            shareFile(context, file, "text/csv", "Export Sales Report CSV")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportSalesReportToPDF(context: Context, orders: List<OrderRecord>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size (72 dpi)
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val fontPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        // Header Title
        fontPaint.apply {
            textSize = 20f
            isFakeBoldText = true
            color = Color.rgb(19, 42, 31) // Forest Green
        }
        canvas.drawText("SALES REPORT", 40f, 60f, fontPaint)

        // Subtitle
        fontPaint.apply {
            textSize = 10f
            isFakeBoldText = false
            color = Color.DKGRAY
        }
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        canvas.drawText("Report Generated: ${sdf.format(Date())}", 40f, 80f, fontPaint)
        canvas.drawText("Total Transactions: ${orders.size}", 40f, 100f, fontPaint)
        canvas.drawText("Total Sales Revenue: INR ${"%,.2f".format(orders.sumOf { it.total })}", 40f, 115f, fontPaint)

        canvas.drawLine(40f, 135f, 555f, 135f, paint)

        // Table Header
        fontPaint.apply {
            textSize = 10f
            isFakeBoldText = true
            color = Color.rgb(19, 42, 31)
        }
        canvas.drawText("ID", 40f, 155f, fontPaint)
        canvas.drawText("Date", 80f, 155f, fontPaint)
        canvas.drawText("Items Selection", 180f, 155f, fontPaint)
        canvas.drawText("Method", 380f, 155f, fontPaint)
        canvas.drawText("Total", 480f, 155f, fontPaint)

        canvas.drawLine(40f, 165f, 555f, 165f, paint)

        fontPaint.apply {
            isFakeBoldText = false
            color = Color.BLACK
        }
        var yPos = 185f
        val sdfRow = SimpleDateFormat("dd-MM HH:mm", Locale.getDefault())
        for (order in orders.take(28)) { // Show up to 28 orders for single page simplicity
            canvas.drawText(order.id.toString(), 40f, yPos, fontPaint)
            canvas.drawText(sdfRow.format(Date(order.timestamp)), 80f, yPos, fontPaint)

            var itemsText = order.itemsSummary
            if (itemsText.length > 32) {
                itemsText = itemsText.substring(0, 29) + "..."
            }
            canvas.drawText(itemsText, 180f, yPos, fontPaint)
            canvas.drawText(order.paymentMethod, 380f, yPos, fontPaint)
            canvas.drawText("₹${"%,.2f".format(order.total)}", 480f, yPos, fontPaint)
            yPos += 22f
        }

        pdfDocument.finishPage(page)

        try {
            val file = File(context.cacheDir, "sales_report_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            shareFile(context, file, "application/pdf", "Export Sales Report PDF")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    fun generateInvoicePDF(
        context: Context,
        shopName: String,
        shopAddress: String,
        shopGst: String,
        items: List<Pair<String, Int>>,
        subtotal: Double,
        total: Double,
        paymentMethod: String
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        // Header Title (Shop Name)
        val titlePaint = Paint().apply {
            color = Color.rgb(19, 42, 31) // Forest Green
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(shopName.uppercase(), 40f, 60f, titlePaint)

        // Address & GST
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText(shopAddress, 40f, 80f, textPaint)
        if (shopGst.isNotEmpty()) {
            canvas.drawText("GSTIN: $shopGst", 40f, 95f, textPaint)
        }

        // Invoice title column details
        val rightPaint = Paint().apply {
            color = Color.rgb(19, 42, 31)
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("INVOICE", 555f, 60f, rightPaint)

        val rightSub = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val invoiceNo = "#INV-2026-${System.currentTimeMillis() % 1000000}"
        canvas.drawText(invoiceNo, 555f, 75f, rightSub)
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        canvas.drawText("Date: ${sdf.format(Date())}", 555f, 90f, rightSub)
        canvas.drawText("Payment Method: $paymentMethod", 555f, 105f, rightSub)

        // Horizontal Line
        canvas.drawLine(40f, 130f, 555f, 130f, paint)

        // Table Header
        val headerPaint = Paint().apply {
            color = Color.rgb(19, 42, 31)
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("ITEM DESCRIPTION", 40f, 150f, headerPaint)
        canvas.drawText("QTY", 420f, 150f, headerPaint)
        val rightHeader = Paint().apply {
            color = Color.rgb(19, 42, 31)
            textSize = 11f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("AMOUNT", 555f, 150f, rightHeader)

        canvas.drawLine(40f, 160f, 555f, 160f, paint)

        var yPos = 185f
        for ((name, qty) in items) {
            // Estimate price based on total divided by qty if individual price is not stored
            val estimatedPrice = total / items.sumOf { it.second }
            canvas.drawText(name, 40f, yPos, textPaint)
            canvas.drawText(qty.toString(), 420f, yPos, textPaint)
            canvas.drawText("₹${"%.2f".format(estimatedPrice * qty)}", 555f, yPos, rightSub)
            yPos += 22f
        }

        canvas.drawLine(40f, yPos + 10f, 555f, yPos + 10f, paint)

        // Summary details
        var summaryY = yPos + 35f
        canvas.drawText("Subtotal:", 340f, summaryY, textPaint)
        canvas.drawText("₹${"%.2f".format(subtotal)}", 555f, summaryY, rightSub)

        summaryY += 20f
        canvas.drawText("Total:", 340f, summaryY, headerPaint)
        val totalRightPaint = Paint().apply {
            color = Color.rgb(19, 42, 31)
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("₹${"%.2f".format(total)}", 555f, summaryY, totalRightPaint)

        pdfDocument.finishPage(page)

        try {
            val file = File(context.cacheDir, "invoice_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            shareFile(context, file, "application/pdf", "Download / Share Invoice")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    fun copyImageToInternalStorage(context: Context, uri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "uploaded_photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}

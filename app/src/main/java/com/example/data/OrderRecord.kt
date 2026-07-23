package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_records")
data class OrderRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val subtotal: Double,
    val discount: Double,
    val gst: Double,
    val total: Double,
    val paymentMethod: String,
    val billingAddress: String,
    val orderNotes: String,
    val itemsSummary: String
)

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "JMH Tea Point",
    val ownerName: String = "Apothecary Admin",
    val phoneNumber: String = "+91 98765 43210",
    val whatsappNumber: String = "+91 98765 43210",
    val email: String = "mohamedasik.in2004@gmail.com",
    val gstNumber: String = "29GGGGG1234F1Z5",
    val address: String = "128 Apothecary Way",
    val city: String = "Botanical District",
    val state: String = "California",
    val pincode: String = "90210",
    val country: String = "United States",
    val openingTime: String = "08:00 AM",
    val closingTime: String = "10:00 PM",
    val description: String = "Insightful apothecary tea shop offering bespoke botanical organic tea curations.",
    val logoUrl: String = "",
    val coverImageUrl: String = "",
    val businessPhotoUrl: String = "",
    val galleryUrls: String = "",
    val upiId: String = "mohamedasik.in2004@okaxis"
)

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendors")
data class Vendor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Chai, Fast Food, Snacks, Juice, Desi Food, Other
    val priceRange: String, // e.g. "Rs 20-50"
    val latitude: Double,
    val longitude: Double,
    val addressOrLandmark: String = "",
    val photoUri: String = "",
    val isOpen: Boolean = true,
    val lastStatusUpdateTimestamp: Long = System.currentTimeMillis(),
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val avgRating: Float = 4.5f,
    val ratingCount: Int = 1
)

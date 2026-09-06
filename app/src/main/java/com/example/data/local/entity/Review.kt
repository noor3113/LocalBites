package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    indices = [Index(value = ["vendorId"])]
)
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long,
    val userName: String,
    val rating: Int, // 1 - 5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

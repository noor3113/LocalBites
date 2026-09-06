package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId ORDER BY timestamp DESC")
    fun getReviewsForVendor(vendorId: Long): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId ORDER BY timestamp DESC")
    suspend fun getReviewsForVendorDirect(vendorId: Long): List<Review>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Query("SELECT AVG(rating) FROM reviews WHERE vendorId = :vendorId")
    suspend fun getAverageRating(vendorId: Long): Float?

    @Query("SELECT COUNT(*) FROM reviews WHERE vendorId = :vendorId")
    suspend fun getReviewCount(vendorId: Long): Int
}

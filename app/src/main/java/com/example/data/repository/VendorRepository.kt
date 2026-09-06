package com.example.data.repository

import com.example.data.local.dao.ReviewDao
import com.example.data.local.dao.VendorDao
import com.example.data.local.entity.Review
import com.example.data.local.entity.Vendor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VendorRepository(
    private val vendorDao: VendorDao,
    private val reviewDao: ReviewDao
) {
    val allVendors: Flow<List<Vendor>> = vendorDao.getAllVendors()

    fun getVendorById(id: Long): Flow<Vendor?> = vendorDao.getVendorById(id)

    fun getReviewsForVendor(vendorId: Long): Flow<List<Review>> =
        reviewDao.getReviewsForVendor(vendorId)

    suspend fun addVendor(vendor: Vendor): Long = withContext(Dispatchers.IO) {
        vendorDao.insertVendor(vendor)
    }

    suspend fun updateVendorOpenStatus(vendorId: Long, isOpen: Boolean) = withContext(Dispatchers.IO) {
        vendorDao.updateOpenStatus(vendorId, isOpen, System.currentTimeMillis())
    }

    suspend fun addReview(vendorId: Long, userName: String, rating: Int, comment: String): Long =
        withContext(Dispatchers.IO) {
            val review = Review(
                vendorId = vendorId,
                userName = userName.ifBlank { "Local Foodie" },
                rating = rating.coerceIn(1, 5),
                comment = comment.trim(),
                timestamp = System.currentTimeMillis()
            )
            val reviewId = reviewDao.insertReview(review)

            // Recalculate average rating & count for vendor
            val avg = reviewDao.getAverageRating(vendorId) ?: rating.toFloat()
            val count = reviewDao.getReviewCount(vendorId)
            vendorDao.updateRatingStats(vendorId, avg, count)

            reviewId
        }

    suspend fun deleteVendor(vendor: Vendor) = withContext(Dispatchers.IO) {
        vendorDao.deleteVendor(vendor)
    }
}

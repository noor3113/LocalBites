package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Vendor
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors ORDER BY createdAtTimestamp DESC")
    fun getAllVendors(): Flow<List<Vendor>>

    @Query("SELECT * FROM vendors WHERE id = :id")
    fun getVendorById(id: Long): Flow<Vendor?>

    @Query("SELECT * FROM vendors WHERE id = :id")
    suspend fun getVendorByIdDirect(id: Long): Vendor?

    @Query("SELECT * FROM vendors WHERE category = :category ORDER BY createdAtTimestamp DESC")
    fun getVendorsByCategory(category: String): Flow<List<Vendor>>

    @Query("SELECT * FROM vendors WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY createdAtTimestamp DESC")
    fun searchVendors(query: String): Flow<List<Vendor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: Vendor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendors(vendors: List<Vendor>)

    @Update
    suspend fun updateVendor(vendor: Vendor)

    @Query("UPDATE vendors SET isOpen = :isOpen, lastStatusUpdateTimestamp = :timestamp WHERE id = :id")
    suspend fun updateOpenStatus(id: Long, isOpen: Boolean, timestamp: Long)

    @Query("UPDATE vendors SET avgRating = :avgRating, ratingCount = :count WHERE id = :id")
    suspend fun updateRatingStats(id: Long, avgRating: Float, count: Int)

    @Delete
    suspend fun deleteVendor(vendor: Vendor)

    @Query("SELECT COUNT(*) FROM vendors")
    suspend fun getVendorCount(): Int
}

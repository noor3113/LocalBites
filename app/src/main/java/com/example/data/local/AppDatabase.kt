package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ReviewDao
import com.example.data.local.dao.VendorDao
import com.example.data.local.entity.Review
import com.example.data.local.entity.Vendor

@Database(
    entities = [Vendor::class, Review::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vendorDao(): VendorDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "local_bites.db"
        ).build()
        INSTANCE = instance
        instance
    }
}

    }
}

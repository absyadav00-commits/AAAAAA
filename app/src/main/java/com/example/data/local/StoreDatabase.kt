package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Customer
import com.example.data.model.KhataTransaction
import com.example.data.model.Product
import com.example.data.model.SaleOrder

@Database(
  entities = [
    Product::class,
    Customer::class,
    SaleOrder::class,
    KhataTransaction::class
  ],
  version = 1,
  exportSchema = false
)
abstract class StoreDatabase : RoomDatabase() {
  abstract fun productDao(): ProductDao
  abstract fun customerDao(): CustomerDao
  abstract fun saleOrderDao(): SaleOrderDao
  abstract fun khataTransactionDao(): KhataTransactionDao

  companion object {
    @Volatile
    private var INSTANCE: StoreDatabase? = null

    fun getDatabase(context: Context): StoreDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          StoreDatabase::class.java,
          "general_store.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}

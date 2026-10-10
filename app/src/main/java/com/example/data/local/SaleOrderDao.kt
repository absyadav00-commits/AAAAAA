package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SaleOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleOrderDao {
  @Query("SELECT * FROM sale_orders ORDER BY timestamp DESC")
  fun getAllOrders(): Flow<List<SaleOrder>>

  @Query("SELECT * FROM sale_orders WHERE id = :id")
  suspend fun getOrderById(id: Long): SaleOrder?

  @Query("SELECT * FROM sale_orders WHERE timestamp >= :startTime ORDER BY timestamp DESC")
  fun getOrdersSince(startTime: Long): Flow<List<SaleOrder>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: SaleOrder): Long

  @Delete
  suspend fun deleteOrder(order: SaleOrder)

  @Query("SELECT COUNT(*) FROM sale_orders")
  suspend fun getOrderCount(): Int
}

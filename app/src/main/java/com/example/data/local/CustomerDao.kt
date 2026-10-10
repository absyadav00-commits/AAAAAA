package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
  @Query("SELECT * FROM customers ORDER BY totalBalanceDue DESC, name ASC")
  fun getAllCustomers(): Flow<List<Customer>>

  @Query("SELECT * FROM customers WHERE id = :id")
  suspend fun getCustomerById(id: Long): Customer?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomer(customer: Customer): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomers(customers: List<Customer>)

  @Update
  suspend fun updateCustomer(customer: Customer)

  @Query("UPDATE customers SET totalBalanceDue = :newBalance, updatedAt = :timestamp WHERE id = :customerId")
  suspend fun updateCustomerBalance(customerId: Long, newBalance: Double, timestamp: Long)

  @Delete
  suspend fun deleteCustomer(customer: Customer)

  @Query("SELECT COUNT(*) FROM customers")
  suspend fun getCustomerCount(): Int
}

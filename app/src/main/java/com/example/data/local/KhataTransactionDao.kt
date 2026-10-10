package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.KhataTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface KhataTransactionDao {
  @Query("SELECT * FROM khata_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
  fun getTransactionsForCustomer(customerId: Long): Flow<List<KhataTransaction>>

  @Query("SELECT * FROM khata_transactions ORDER BY timestamp DESC LIMIT 100")
  fun getAllRecentTransactions(): Flow<List<KhataTransaction>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: KhataTransaction): Long

  @Delete
  suspend fun deleteTransaction(transaction: KhataTransaction)
}

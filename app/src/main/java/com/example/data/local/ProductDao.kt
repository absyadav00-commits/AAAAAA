package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
  @Query("SELECT * FROM products ORDER BY name ASC")
  fun getAllProducts(): Flow<List<Product>>

  @Query("SELECT * FROM products WHERE id = :id")
  suspend fun getProductById(id: Long): Product?

  @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name ASC")
  fun searchProducts(query: String): Flow<List<Product>>

  @Query("SELECT * FROM products WHERE stockQuantity <= minStockThreshold ORDER BY stockQuantity ASC")
  fun getLowStockProducts(): Flow<List<Product>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: Product): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<Product>)

  @Update
  suspend fun updateProduct(product: Product)

  @Query("UPDATE products SET stockQuantity = :newStock WHERE id = :productId")
  suspend fun updateStock(productId: Long, newStock: Double)

  @Delete
  suspend fun deleteProduct(product: Product)

  @Query("SELECT COUNT(*) FROM products")
  suspend fun getProductCount(): Int
}

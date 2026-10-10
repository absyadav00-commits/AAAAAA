package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val category: String,
  val costPrice: Double,
  val sellingPrice: Double,
  val stockQuantity: Double,
  val unit: String = "pcs", // "pcs", "kg", "packet", "liter", "bottle", "box"
  val minStockThreshold: Double = 5.0,
  val barcode: String = "",
  val createdAt: Long = System.currentTimeMillis()
) {
  val isLowStock: Boolean get() = stockQuantity <= minStockThreshold && stockQuantity > 0.0
  val isOutOfStock: Boolean get() = stockQuantity <= 0.0
  val profitMarginPercent: Double
    get() = if (costPrice > 0) ((sellingPrice - costPrice) / costPrice) * 100.0 else 0.0
}

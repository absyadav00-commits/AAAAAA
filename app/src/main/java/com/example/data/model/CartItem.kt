package com.example.data.model

data class CartItem(
  val productId: Long,
  val productName: String,
  val category: String,
  val unitPrice: Double,
  val costPrice: Double,
  val unit: String,
  val quantity: Double = 1.0,
  val availableStock: Double = 0.0
) {
  val subtotal: Double get() = unitPrice * quantity
  val totalCost: Double get() = costPrice * quantity
}

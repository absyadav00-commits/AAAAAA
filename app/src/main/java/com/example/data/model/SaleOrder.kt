package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_orders")
data class SaleOrder(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val invoiceNumber: String,
  val customerId: Long? = null,
  val customerName: String = "Walk-in Customer",
  val customerPhone: String = "",
  val itemsSummary: String = "",
  val itemsJson: String = "",
  val subtotal: Double = 0.0,
  val discount: Double = 0.0,
  val taxAmount: Double = 0.0,
  val totalAmount: Double = 0.0,
  val totalProfit: Double = 0.0,
  val paymentMode: String = "CASH", // CASH, UPI, CARD, CREDIT
  val paymentStatus: String = "PAID", // PAID, CREDIT_DUE
  val timestamp: Long = System.currentTimeMillis()
)

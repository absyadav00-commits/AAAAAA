package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "khata_transactions")
data class KhataTransaction(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val customerId: Long,
  val type: String, // "GAVE_CREDIT" (Shopkeeper gave goods on credit, customer owes money), "RECEIVED_PAYMENT" (Customer paid cash/UPI to settle balance)
  val amount: Double,
  val note: String = "",
  val orderId: Long? = null,
  val timestamp: Long = System.currentTimeMillis()
)

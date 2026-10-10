package com.example.data.model

data class StoreSettings(
  val storeName: String = "Yadav General Store",
  val tagline: String = "All Daily Needs & Groceries Under One Roof",
  val phone: String = "+91 98765 43210",
  val address: String = "Main Market, Sector 4",
  val currencySymbol: String = "₹",
  val taxPercent: Double = 0.0,
  val upiId: String = "generalstore@upi",
  val receiptFooterMessage: String = "Thank you for shopping with us! Please visit again."
)

package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.StoreDatabase
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.KhataTransaction
import com.example.data.model.Product
import com.example.data.model.SaleOrder
import com.example.data.model.StoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class StoreRepository(
  context: Context,
  private val database: StoreDatabase = StoreDatabase.getDatabase(context)
) {
  private val productDao = database.productDao()
  private val customerDao = database.customerDao()
  private val saleOrderDao = database.saleOrderDao()
  private val khataTransactionDao = database.khataTransactionDao()

  private val prefs: SharedPreferences =
    context.getSharedPreferences("general_store_prefs", Context.MODE_PRIVATE)

  private val _settings = MutableStateFlow(loadSettings())
  val settings: StateFlow<StoreSettings> = _settings.asStateFlow()

  val allProducts: Flow<List<Product>> = productDao.getAllProducts()
  val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
  val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
  val allOrders: Flow<List<SaleOrder>> = saleOrderDao.getAllOrders()
  val recentKhataTransactions: Flow<List<KhataTransaction>> =
    khataTransactionDao.getAllRecentTransactions()

  fun getCustomerTransactions(customerId: Long): Flow<List<KhataTransaction>> =
    khataTransactionDao.getTransactionsForCustomer(customerId)

  private fun loadSettings(): StoreSettings {
    return StoreSettings(
      storeName = prefs.getString("storeName", "Yadav General Store") ?: "Yadav General Store",
      tagline = prefs.getString("tagline", "All Daily Needs & Groceries Under One Roof")
        ?: "All Daily Needs & Groceries Under One Roof",
      phone = prefs.getString("phone", "+91 98765 43210") ?: "+91 98765 43210",
      address = prefs.getString("address", "Main Market, Sector 4") ?: "Main Market, Sector 4",
      currencySymbol = prefs.getString("currencySymbol", "₹") ?: "₹",
      taxPercent = prefs.getFloat("taxPercent", 0.0f).toDouble(),
      upiId = prefs.getString("upiId", "generalstore@upi") ?: "generalstore@upi",
      receiptFooterMessage = prefs.getString(
        "receiptFooterMessage",
        "Thank you for shopping with us! Please visit again."
      ) ?: "Thank you for shopping with us! Please visit again."
    )
  }

  fun updateSettings(newSettings: StoreSettings) {
    prefs.edit()
      .putString("storeName", newSettings.storeName)
      .putString("tagline", newSettings.tagline)
      .putString("phone", newSettings.phone)
      .putString("address", newSettings.address)
      .putString("currencySymbol", newSettings.currencySymbol)
      .putFloat("taxPercent", newSettings.taxPercent.toFloat())
      .putString("upiId", newSettings.upiId)
      .putString("receiptFooterMessage", newSettings.receiptFooterMessage)
      .apply()
    _settings.value = newSettings
  }

  suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
    val count = productDao.getProductCount()
    if (count == 0) {
      seedDefaultProducts()
      seedDefaultCustomers()
    }
  }

  suspend fun resetWithSampleData() = withContext(Dispatchers.IO) {
    seedDefaultProducts()
    seedDefaultCustomers()
  }

  private suspend fun seedDefaultProducts() {
    val defaultProducts = listOf(
      Product(name = "Premium Basmati Rice", category = "Groceries & Staples", costPrice = 95.0, sellingPrice = 120.0, stockQuantity = 45.0, unit = "kg", minStockThreshold = 10.0),
      Product(name = "Whole Wheat Atta (Flour)", category = "Groceries & Staples", costPrice = 175.0, sellingPrice = 210.0, stockQuantity = 30.0, unit = "packet", minStockThreshold = 5.0),
      Product(name = "Pure Desi Ghee (500ml)", category = "Groceries & Staples", costPrice = 290.0, sellingPrice = 340.0, stockQuantity = 12.0, unit = "bottle", minStockThreshold = 4.0),
      Product(name = "Refined Sunflower Oil (1L)", category = "Groceries & Staples", costPrice = 125.0, sellingPrice = 145.0, stockQuantity = 22.0, unit = "liter", minStockThreshold = 6.0),
      Product(name = "Refined Sugar", category = "Groceries & Staples", costPrice = 40.0, sellingPrice = 48.0, stockQuantity = 50.0, unit = "kg", minStockThreshold = 15.0),
      Product(name = "Toor Dal (Yellow Lentils)", category = "Groceries & Staples", costPrice = 125.0, sellingPrice = 150.0, stockQuantity = 25.0, unit = "kg", minStockThreshold = 5.0),
      Product(name = "Iodized Salt (1kg)", category = "Groceries & Staples", costPrice = 22.0, sellingPrice = 28.0, stockQuantity = 40.0, unit = "packet", minStockThreshold = 10.0),

      Product(name = "Fresh Full Cream Milk (1L)", category = "Dairy & Bakery", costPrice = 58.0, sellingPrice = 66.0, stockQuantity = 18.0, unit = "packet", minStockThreshold = 8.0),
      Product(name = "Salted Table Butter (100g)", category = "Dairy & Bakery", costPrice = 50.0, sellingPrice = 58.0, stockQuantity = 14.0, unit = "pcs", minStockThreshold = 5.0),
      Product(name = "Whole Wheat Brown Bread", category = "Dairy & Bakery", costPrice = 35.0, sellingPrice = 45.0, stockQuantity = 8.0, unit = "packet", minStockThreshold = 4.0),
      Product(name = "Farm Fresh Eggs (Pack of 6)", category = "Dairy & Bakery", costPrice = 42.0, sellingPrice = 52.0, stockQuantity = 15.0, unit = "box", minStockThreshold = 5.0),
      Product(name = "Fresh Paneer (200g)", category = "Dairy & Bakery", costPrice = 70.0, sellingPrice = 85.0, stockQuantity = 6.0, unit = "packet", minStockThreshold = 4.0),

      Product(name = "Masala Potato Chips (50g)", category = "Snacks & Drinks", costPrice = 16.0, sellingPrice = 20.0, stockQuantity = 35.0, unit = "packet", minStockThreshold = 10.0),
      Product(name = "Butter Cookies Biscuits", category = "Snacks & Drinks", costPrice = 24.0, sellingPrice = 30.0, stockQuantity = 28.0, unit = "packet", minStockThreshold = 8.0),
      Product(name = "Instant Masala Noodles", category = "Snacks & Drinks", costPrice = 11.0, sellingPrice = 14.0, stockQuantity = 40.0, unit = "packet", minStockThreshold = 10.0),
      Product(name = "Roasted Salted Peanuts", category = "Snacks & Drinks", costPrice = 30.0, sellingPrice = 40.0, stockQuantity = 20.0, unit = "packet", minStockThreshold = 5.0),
      Product(name = "Chilled Cola (750ml)", category = "Snacks & Drinks", costPrice = 36.0, sellingPrice = 45.0, stockQuantity = 24.0, unit = "bottle", minStockThreshold = 6.0),
      Product(name = "Assam CTC Tea (250g)", category = "Snacks & Drinks", costPrice = 105.0, sellingPrice = 130.0, stockQuantity = 16.0, unit = "packet", minStockThreshold = 5.0),

      Product(name = "Herbal Bath Soap (125g)", category = "Personal Care", costPrice = 32.0, sellingPrice = 42.0, stockQuantity = 25.0, unit = "pcs", minStockThreshold = 6.0),
      Product(name = "Complete Care Toothpaste (150g)", category = "Personal Care", costPrice = 60.0, sellingPrice = 75.0, stockQuantity = 18.0, unit = "pcs", minStockThreshold = 5.0),
      Product(name = "Coconut Hair Oil (200ml)", category = "Personal Care", costPrice = 72.0, sellingPrice = 90.0, stockQuantity = 12.0, unit = "bottle", minStockThreshold = 4.0),

      Product(name = "Lemon Dishwash Gel (500ml)", category = "Household & Cleaning", costPrice = 82.0, sellingPrice = 105.0, stockQuantity = 14.0, unit = "bottle", minStockThreshold = 4.0),
      Product(name = "Washing Detergent Powder (1kg)", category = "Household & Cleaning", costPrice = 110.0, sellingPrice = 135.0, stockQuantity = 20.0, unit = "packet", minStockThreshold = 5.0),
      Product(name = "Floor Cleaner (1L)", category = "Household & Cleaning", costPrice = 95.0, sellingPrice = 120.0, stockQuantity = 11.0, unit = "bottle", minStockThreshold = 3.0),
      Product(name = "Mosquito Vaporizer Machine + Refill", category = "Household & Cleaning", costPrice = 65.0, sellingPrice = 80.0, stockQuantity = 3.0, unit = "box", minStockThreshold = 5.0), // Low stock example

      Product(name = "Ballpoint Pens (Set of 5)", category = "Stationery & Essentials", costPrice = 35.0, sellingPrice = 50.0, stockQuantity = 15.0, unit = "packet", minStockThreshold = 4.0),
      Product(name = "Ruled Long Notebook", category = "Stationery & Essentials", costPrice = 45.0, sellingPrice = 65.0, stockQuantity = 22.0, unit = "pcs", minStockThreshold = 5.0),
      Product(name = "Safety Matchbox (10 pack)", category = "Stationery & Essentials", costPrice = 15.0, sellingPrice = 20.0, stockQuantity = 2.0, unit = "pack", minStockThreshold = 5.0) // Low stock example
    )
    productDao.insertProducts(defaultProducts)
  }

  private suspend fun seedDefaultCustomers() {
    val defaultCustomers = listOf(
      Customer(name = "Ramesh Kumar", phone = "+91 98112 34567", address = "Flat 102, Shanti Appts", totalBalanceDue = 450.0),
      Customer(name = "Sunita Sharma", phone = "+91 98223 45678", address = "House 45, Street 2", totalBalanceDue = 1200.0),
      Customer(name = "Amit Patel", phone = "+91 98334 56789", address = "Shop 12, Main Road", totalBalanceDue = 0.0),
      Customer(name = "Pooja Verma", phone = "+91 98445 67890", address = "Sector 4, Block C", totalBalanceDue = 680.0)
    )
    customerDao.insertCustomers(defaultCustomers)
  }

  // Product Operations
  suspend fun insertProduct(product: Product) = withContext(Dispatchers.IO) {
    productDao.insertProduct(product)
  }

  suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
    productDao.updateProduct(product)
  }

  suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
    productDao.deleteProduct(product)
  }

  suspend fun quickRestock(productId: Long, currentStock: Double, addAmount: Double) =
    withContext(Dispatchers.IO) {
      productDao.updateStock(productId, currentStock + addAmount)
    }

  // Customer Operations
  suspend fun insertCustomer(customer: Customer) = withContext(Dispatchers.IO) {
    customerDao.insertCustomer(customer)
  }

  suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
    customerDao.updateCustomer(customer)
  }

  suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
    customerDao.deleteCustomer(customer)
  }

  // Khata / Repayment Operation
  suspend fun recordCustomerRepayment(
    customer: Customer,
    repaymentAmount: Double,
    paymentMethod: String,
    note: String
  ) = withContext(Dispatchers.IO) {
    val newBalance = (customer.totalBalanceDue - repaymentAmount).coerceAtLeast(0.0)
    customerDao.updateCustomerBalance(customer.id, newBalance, System.currentTimeMillis())
    khataTransactionDao.insertTransaction(
      KhataTransaction(
        customerId = customer.id,
        type = "RECEIVED_PAYMENT",
        amount = repaymentAmount,
        note = if (note.isNotBlank()) "$note ($paymentMethod)" else "Payment received via $paymentMethod"
      )
    )
  }

  suspend fun recordCustomerCredit(
    customer: Customer,
    creditAmount: Double,
    note: String
  ) = withContext(Dispatchers.IO) {
    val newBalance = customer.totalBalanceDue + creditAmount
    customerDao.updateCustomerBalance(customer.id, newBalance, System.currentTimeMillis())
    khataTransactionDao.insertTransaction(
      KhataTransaction(
        customerId = customer.id,
        type = "GAVE_CREDIT",
        amount = creditAmount,
        note = note.ifBlank { "Manual Udhar / Credit entry" }
      )
    )
  }

  // Complete Sale Order & Deduct Inventory & Handle Udhar/Khata
  suspend fun checkoutSaleOrder(
    cartItems: List<CartItem>,
    customer: Customer?,
    walkInName: String,
    walkInPhone: String,
    subtotal: Double,
    discount: Double,
    taxAmount: Double,
    totalAmount: Double,
    paymentMode: String
  ): SaleOrder = withContext(Dispatchers.IO) {
    val timestamp = System.currentTimeMillis()
    val invoiceNumber = "GS-${(timestamp % 100000).toString().padStart(5, '0')}"

    val itemsSummary = cartItems.joinToString(", ") { "${it.productName} (${it.quantity}${it.unit})" }

    val itemsJson = StringBuilder().apply {
      append("[")
      cartItems.forEachIndexed { index, item ->
        append("""{"id":${item.productId},"name":"${item.productName}","qty":${item.quantity},"unit":"${item.unit}","price":${item.unitPrice},"subtotal":${item.subtotal}}""")
        if (index < cartItems.size - 1) append(",")
      }
      append("]")
    }.toString()

    val totalProfit = cartItems.sumOf { (it.unitPrice - it.costPrice) * it.quantity } - discount

    val isCredit = paymentMode == "CREDIT"
    val customerName = customer?.name ?: walkInName.ifBlank { "Walk-in Customer" }
    val customerPhone = customer?.phone ?: walkInPhone

    val saleOrder = SaleOrder(
      invoiceNumber = invoiceNumber,
      customerId = customer?.id,
      customerName = customerName,
      customerPhone = customerPhone,
      itemsSummary = itemsSummary,
      itemsJson = itemsJson,
      subtotal = subtotal,
      discount = discount,
      taxAmount = taxAmount,
      totalAmount = totalAmount,
      totalProfit = totalProfit,
      paymentMode = paymentMode,
      paymentStatus = if (isCredit) "CREDIT_DUE" else "PAID",
      timestamp = timestamp
    )

    val orderId = saleOrderDao.insertOrder(saleOrder)

    // Deduct stock for all items
    for (item in cartItems) {
      val product = productDao.getProductById(item.productId)
      if (product != null) {
        val updatedStock = (product.stockQuantity - item.quantity).coerceAtLeast(0.0)
        productDao.updateStock(product.id, updatedStock)
      }
    }

    // If payment was made on credit (Khata), update customer balance and record transaction
    if (isCredit && customer != null) {
      val newBalance = customer.totalBalanceDue + totalAmount
      customerDao.updateCustomerBalance(customer.id, newBalance, timestamp)
      khataTransactionDao.insertTransaction(
        KhataTransaction(
          customerId = customer.id,
          type = "GAVE_CREDIT",
          amount = totalAmount,
          note = "Bill #$invoiceNumber",
          orderId = orderId,
          timestamp = timestamp
        )
      )
    }

    saleOrder.copy(id = orderId)
  }
}

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.KhataTransaction
import com.example.data.model.Product
import com.example.data.model.SaleOrder
import com.example.data.model.StoreSettings
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = StoreRepository(application)

  val settings: StateFlow<StoreSettings> = repository.settings

  val allProducts: StateFlow<List<Product>> = repository.allProducts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allOrders: StateFlow<List<SaleOrder>> = repository.allOrders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val recentKhataTransactions: StateFlow<List<KhataTransaction>> = repository.recentKhataTransactions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // POS State
  private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
  val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

  val selectedCustomerForPOS = MutableStateFlow<Customer?>(null)
  val walkInName = MutableStateFlow("")
  val walkInPhone = MutableStateFlow("")
  val discountAmount = MutableStateFlow(0.0)
  val isTaxEnabled = MutableStateFlow(false)

  // POS Filter & Search
  val posSearchQuery = MutableStateFlow("")
  val posSelectedCategory = MutableStateFlow("All")

  // Filtered Products for POS
  val posFilteredProducts: StateFlow<List<Product>> = combine(
    allProducts,
    posSearchQuery,
    posSelectedCategory
  ) { products, query, category ->
    products.filter { p ->
      val matchesQuery = query.isBlank() ||
        p.name.contains(query, ignoreCase = true) ||
        p.barcode.contains(query, ignoreCase = true)
      val matchesCategory = category == "All" || p.category.equals(category, ignoreCase = true)
      matchesQuery && matchesCategory
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Completed Receipt Dialog
  val completedOrderForReceipt = MutableStateFlow<SaleOrder?>(null)

  init {
    viewModelScope.launch {
      repository.checkAndSeedInitialData()
    }
  }

  fun getCustomerTransactions(customerId: Long): Flow<List<KhataTransaction>> {
    return repository.getCustomerTransactions(customerId)
  }

  // Cart Operations
  fun addToCart(product: Product, quantityToAdd: Double = 1.0) {
    val current = _cart.value.toMutableList()
    val existingIndex = current.indexOfFirst { it.productId == product.id }
    if (existingIndex >= 0) {
      val existing = current[existingIndex]
      val newQty = existing.quantity + quantityToAdd
      current[existingIndex] = existing.copy(quantity = newQty)
    } else {
      current.add(
        CartItem(
          productId = product.id,
          productName = product.name,
          category = product.category,
          unitPrice = product.sellingPrice,
          costPrice = product.costPrice,
          unit = product.unit,
          quantity = quantityToAdd,
          availableStock = product.stockQuantity
        )
      )
    }
    _cart.value = current
  }

  fun updateCartQuantity(productId: Long, newQuantity: Double) {
    if (newQuantity <= 0.0) {
      removeFromCart(productId)
    } else {
      _cart.value = _cart.value.map {
        if (it.productId == productId) it.copy(quantity = newQuantity) else it
      }
    }
  }

  fun removeFromCart(productId: Long) {
    _cart.value = _cart.value.filter { it.productId != productId }
  }

  fun clearCart() {
    _cart.value = emptyList()
    selectedCustomerForPOS.value = null
    walkInName.value = ""
    walkInPhone.value = ""
    discountAmount.value = 0.0
    isTaxEnabled.value = false
  }

  fun selectCustomerForPOS(customer: Customer?) {
    selectedCustomerForPOS.value = customer
    if (customer != null) {
      walkInName.value = customer.name
      walkInPhone.value = customer.phone
    }
  }

  // Checkout
  fun checkout(
    paymentMode: String,
    onSuccess: (SaleOrder) -> Unit
  ) {
    val cartItems = _cart.value
    if (cartItems.isEmpty()) return

    val subtotal = cartItems.sumOf { it.subtotal }
    val discount = discountAmount.value.coerceAtMost(subtotal)
    val taxRate = if (isTaxEnabled.value) settings.value.taxPercent else 0.0
    val taxAmount = (subtotal - discount) * (taxRate / 100.0)
    val totalAmount = (subtotal - discount) + taxAmount

    viewModelScope.launch {
      val order = repository.checkoutSaleOrder(
        cartItems = cartItems,
        customer = selectedCustomerForPOS.value,
        walkInName = walkInName.value,
        walkInPhone = walkInPhone.value,
        subtotal = subtotal,
        discount = discount,
        taxAmount = taxAmount,
        totalAmount = totalAmount,
        paymentMode = paymentMode
      )
      completedOrderForReceipt.value = order
      clearCart()
      onSuccess(order)
    }
  }

  // Product Operations
  fun saveProduct(product: Product) {
    viewModelScope.launch {
      if (product.id == 0L) {
        repository.insertProduct(product)
      } else {
        repository.updateProduct(product)
      }
    }
  }

  fun deleteProduct(product: Product) {
    viewModelScope.launch {
      repository.deleteProduct(product)
    }
  }

  fun quickRestock(product: Product, addAmount: Double) {
    viewModelScope.launch {
      repository.quickRestock(product.id, product.stockQuantity, addAmount)
    }
  }

  // Customer Operations
  fun saveCustomer(customer: Customer) {
    viewModelScope.launch {
      if (customer.id == 0L) {
        repository.insertCustomer(customer)
      } else {
        repository.updateCustomer(customer)
      }
    }
  }

  fun deleteCustomer(customer: Customer) {
    viewModelScope.launch {
      repository.deleteCustomer(customer)
    }
  }

  fun recordRepayment(customer: Customer, amount: Double, method: String, note: String) {
    viewModelScope.launch {
      repository.recordCustomerRepayment(customer, amount, method, note)
    }
  }

  fun recordCredit(customer: Customer, amount: Double, note: String) {
    viewModelScope.launch {
      repository.recordCustomerCredit(customer, amount, note)
    }
  }

  fun saveSettings(newSettings: StoreSettings) {
    repository.updateSettings(newSettings)
  }

  fun resetSampleData() {
    viewModelScope.launch {
      repository.resetWithSampleData()
    }
  }
}

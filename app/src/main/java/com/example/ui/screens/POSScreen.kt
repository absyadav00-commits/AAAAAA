package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.ui.components.PRODUCT_CATEGORIES
import com.example.ui.theme.CreditDueContainer
import com.example.ui.theme.CreditDueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.StockLowContainer
import com.example.ui.theme.StockLowOrange
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreBlue
import com.example.ui.theme.StoreGreenPrimary
import com.example.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.launch

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun POSScreen(
  viewModel: StoreViewModel,
  onOpenCustomerKhata: () -> Unit
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val filteredProducts by viewModel.posFilteredProducts.collectAsStateWithLifecycle()
  val cartItems by viewModel.cart.collectAsStateWithLifecycle()
  val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

  val searchQuery by viewModel.posSearchQuery.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.posSelectedCategory.collectAsStateWithLifecycle()
  val selectedCustomer by viewModel.selectedCustomerForPOS.collectAsStateWithLifecycle()
  val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
  val isTaxEnabled by viewModel.isTaxEnabled.collectAsStateWithLifecycle()

  var isCartSheetOpen by remember { mutableStateOf(false) }
  val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  val totalCartQty = cartItems.sumOf { it.quantity }
  val subtotal = cartItems.sumOf { it.subtotal }
  val discount = discountAmount.coerceAtMost(subtotal)
  val taxRate = if (isTaxEnabled) settings.taxPercent else 0.0
  val taxAmount = (subtotal - discount) * (taxRate / 100.0)
  val grandTotal = (subtotal - discount) + taxAmount

  val categories = remember { listOf("All") + PRODUCT_CATEGORIES }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Search & Cart Quick Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.posSearchQuery.value = it },
          placeholder = { Text("Search item, brand or barcode...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.posSearchQuery.value = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          modifier = Modifier
            .weight(1f)
            .testTag("pos_search_input"),
          shape = RoundedCornerShape(16.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Cart Icon with Badge
        BadgedBox(
          badge = {
            if (cartItems.isNotEmpty()) {
              Badge(
                containerColor = StoreAmber,
                contentColor = Color.White
              ) {
                Text(
                  text = if (totalCartQty % 1.0 == 0.0) totalCartQty.toInt().toString() else "%.1f".format(totalCartQty)
                )
              }
            }
          }
        ) {
          IconButton(
            onClick = { isCartSheetOpen = true },
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (cartItems.isNotEmpty()) StoreGreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
              .testTag("open_cart_button")
          ) {
            Icon(
              imageVector = Icons.Default.ShoppingCart,
              contentDescription = "Cart",
              tint = if (cartItems.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Category Horizontal Filter Pills
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(categories) { cat ->
          val isSelected = selectedCategory == cat
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.posSelectedCategory.value = cat },
            label = { Text(cat, fontSize = 13.sp) },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = StoreGreenPrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Products List for Quick Tap Add
      if (filteredProducts.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "No products found",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Try clearing search or check the Inventory tab to add items",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = if (cartItems.isNotEmpty()) 80.dp else 16.dp)
        ) {
          items(filteredProducts, key = { it.id }) { product ->
            val cartEntry = cartItems.find { it.productId == product.id }
            ProductPOSCard(
              product = product,
              currencySymbol = settings.currencySymbol,
              cartQuantity = cartEntry?.quantity ?: 0.0,
              onAddToCart = { viewModel.addToCart(product, 1.0) },
              onQuantityChange = { newQty -> viewModel.updateCartQuantity(product.id, newQty) }
            )
          }
        }
      }
    }

    // Bottom Cart Bar (if items in cart and sheet not already open)
    AnimatedVisibility(
      visible = cartItems.isNotEmpty() && !isCartSheetOpen,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(16.dp)
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .clickable { isCartSheetOpen = true },
        color = StoreGreenPrimary,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
      ) {
        Row(
          modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${cartItems.size}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Cart Total: ${settings.currencySymbol}%.2f".format(grandTotal),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = "${totalCartQty.let { if (it % 1.0 == 0.0) it.toInt() else "%.1f".format(it) }} items • Tap to checkout",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
              )
            }
          }

          Button(
            onClick = { isCartSheetOpen = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = StoreAmber,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("view_bill_summary_button")
          ) {
            Text("View Bill", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Modal Bottom Sheet for Bill & Checkout
    if (isCartSheetOpen) {
      ModalBottomSheet(
        onDismissRequest = { isCartSheetOpen = false },
        sheetState = cartSheetState
      ) {
        CartCheckoutSheetContent(
          viewModel = viewModel,
          cartItems = cartItems,
          customers = allCustomers,
          selectedCustomer = selectedCustomer,
          settings = settings,
          subtotal = subtotal,
          discount = discount,
          taxAmount = taxAmount,
          grandTotal = grandTotal,
          onClose = {
            scope.launch { cartSheetState.hide() }.invokeOnCompletion {
              isCartSheetOpen = false
            }
          },
          onCheckoutSuccess = {
            scope.launch { cartSheetState.hide() }.invokeOnCompletion {
              isCartSheetOpen = false
            }
          }
        )
      }
    }
  }
}

@Composable
private fun ProductPOSCard(
  product: Product,
  currencySymbol: String,
  cartQuantity: Double,
  onAddToCart: () -> Unit,
  onQuantityChange: (Double) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("pos_product_${product.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (cartQuantity > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      else MaterialTheme.colorScheme.surface
    ),
    border = if (cartQuantity > 0) {
      androidx.compose.foundation.BorderStroke(1.5.dp, StoreGreenPrimary.copy(alpha = 0.6f))
    } else {
      androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Text(
            text = product.category,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = " • ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
          // Stock badge
          val stockText = "${product.stockQuantity} ${product.unit}"
          Text(
            text = if (product.isOutOfStock) "Out of stock" else "Stock: $stockText",
            style = MaterialTheme.typography.labelSmall,
            color = when {
              product.isOutOfStock -> CreditDueRed
              product.isLowStock -> StockLowOrange
              else -> StoreGreenPrimary
            },
            fontWeight = FontWeight.Medium
          )
        }

        Text(
          text = "$currencySymbol%.2f / %s".format(product.sellingPrice, product.unit),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      // Quantity Controller or Add Button
      if (cartQuantity > 0) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
          IconButton(
            onClick = { onQuantityChange(cartQuantity - 1.0) },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = StoreGreenPrimary)
          }

          Text(
            text = if (cartQuantity % 1.0 == 0.0) cartQuantity.toInt().toString() else "%.1f".format(cartQuantity),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp)
          )

          IconButton(
            onClick = { onQuantityChange(cartQuantity + 1.0) },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = StoreGreenPrimary)
          }
        }
      } else {
        Button(
          onClick = onAddToCart,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (product.isOutOfStock) MaterialTheme.colorScheme.outline else StoreGreenPrimary
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier.testTag("add_to_cart_${product.id}")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun CartCheckoutSheetContent(
  viewModel: StoreViewModel,
  cartItems: List<com.example.data.model.CartItem>,
  customers: List<Customer>,
  selectedCustomer: Customer?,
  settings: com.example.data.model.StoreSettings,
  subtotal: Double,
  discount: Double,
  taxAmount: Double,
  grandTotal: Double,
  onClose: () -> Unit,
  onCheckoutSuccess: () -> Unit
) {
  var customerDropdownExpanded by remember { mutableStateOf(false) }
  var showDiscountInput by remember { mutableStateOf(false) }
  var discountInputText by remember { mutableStateOf(discount.let { if (it > 0) it.toString() else "" }) }
  val walkInName by viewModel.walkInName.collectAsStateWithLifecycle()
  val walkInPhone by viewModel.walkInPhone.collectAsStateWithLifecycle()
  val isTaxEnabled by viewModel.isTaxEnabled.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = StoreGreenPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Bill Summary (${cartItems.size} items)",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
      TextButton(onClick = { viewModel.clearCart() }) {
        Text("Clear Cart", color = MaterialTheme.colorScheme.error)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Customer Selection (Walk-in vs Khata Customer)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = StoreGreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (selectedCustomer != null) "Customer: ${selectedCustomer.name}" else "Customer: Walk-in",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
          }

          Box {
            TextButton(
              onClick = { customerDropdownExpanded = true },
              modifier = Modifier.testTag("select_customer_dropdown_button")
            ) {
              Text(if (selectedCustomer != null) "Change" else "Select Khata")
              Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
              expanded = customerDropdownExpanded,
              onDismissRequest = { customerDropdownExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("Walk-in Customer (General)") },
                onClick = {
                  viewModel.selectCustomerForPOS(null)
                  customerDropdownExpanded = false
                }
              )
              HorizontalDivider()
              customers.forEach { customer ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(customer.name, fontWeight = FontWeight.SemiBold)
                      Text(
                        "${customer.phone} • Due: ${settings.currencySymbol}%.2f".format(customer.totalBalanceDue),
                        fontSize = 11.sp,
                        color = if (customer.totalBalanceDue > 0) CreditDueRed else PaidGreen
                      )
                    }
                  },
                  onClick = {
                    viewModel.selectCustomerForPOS(customer)
                    customerDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        if (selectedCustomer == null) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = walkInName,
              onValueChange = { viewModel.walkInName.value = it },
              placeholder = { Text("Name (optional)") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = walkInPhone,
              onValueChange = { viewModel.walkInPhone.value = it },
              placeholder = { Text("Phone (optional)") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
          }
        } else if (selectedCustomer.totalBalanceDue > 0) {
          Text(
            text = "⚠️ Has existing unpaid credit: ${settings.currencySymbol}%.2f".format(selectedCustomer.totalBalanceDue),
            color = CreditDueRed,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Cart Items List
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(cartItems) { item ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
              "${item.quantity} ${item.unit} @ ${settings.currencySymbol}%.2f".format(item.unitPrice),
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Text(
            "${settings.currencySymbol}%.2f".format(item.subtotal),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
          )

          IconButton(
            onClick = { viewModel.removeFromCart(item.productId) },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    Spacer(modifier = Modifier.height(8.dp))

    // Calculations: Subtotal, Discount, Tax
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
      Text("${settings.currencySymbol}%.2f".format(subtotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Discount", style = MaterialTheme.typography.bodyMedium)
        TextButton(
          onClick = { showDiscountInput = !showDiscountInput },
          contentPadding = PaddingValues(horizontal = 6.dp)
        ) {
          Text(if (showDiscountInput) "Hide" else "Set")
        }
      }
      Text(
        "-${settings.currencySymbol}%.2f".format(discount),
        style = MaterialTheme.typography.bodyMedium,
        color = PaidGreen,
        fontWeight = FontWeight.SemiBold
      )
    }

    if (showDiscountInput) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = discountInputText,
          onValueChange = {
            discountInputText = it
            viewModel.discountAmount.value = it.toDoubleOrNull() ?: 0.0
          },
          label = { Text("Discount Amount (${settings.currencySymbol})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f),
          singleLine = true
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
    }

    if (settings.taxPercent > 0) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Apply Tax (${settings.taxPercent}%)", style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.width(6.dp))
          Switch(
            checked = isTaxEnabled,
            onCheckedChange = { viewModel.isTaxEnabled.value = it }
          )
        }
        Text(
          "+${settings.currencySymbol}%.2f".format(taxAmount),
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Grand Total Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        "Grand Total",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        "${settings.currencySymbol}%.2f".format(grandTotal),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = StoreGreenPrimary
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Payment Buttons
    Text(
      text = "SELECT PAYMENT MODE",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Cash
      Button(
        onClick = {
          viewModel.checkout("CASH") { onCheckoutSuccess() }
        },
        modifier = Modifier
          .weight(1f)
          .testTag("pay_cash_button"),
        colors = ButtonDefaults.buttonColors(containerColor = StoreGreenPrimary),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.LocalAtm, contentDescription = null, modifier = Modifier.size(20.dp))
          Text("Cash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      // UPI
      Button(
        onClick = {
          viewModel.checkout("UPI") { onCheckoutSuccess() }
        },
        modifier = Modifier
          .weight(1f)
          .testTag("pay_upi_button"),
        colors = ButtonDefaults.buttonColors(containerColor = StoreBlue),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
          Text("UPI / QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      // Card
      Button(
        onClick = {
          viewModel.checkout("CARD") { onCheckoutSuccess() }
        },
        modifier = Modifier
          .weight(1f)
          .testTag("pay_card_button"),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(20.dp))
          Text("Card", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      // Credit / Udhar (Khata)
      Button(
        onClick = {
          if (selectedCustomer != null) {
            viewModel.checkout("CREDIT") { onCheckoutSuccess() }
          } else {
            // Prompts user to pick a Khata customer
            customerDropdownExpanded = true
          }
        },
        modifier = Modifier
          .weight(1.1f)
          .testTag("pay_credit_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (selectedCustomer != null) CreditDueRed else Color(0xFF991B1B)
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
          Text("Udhar / Khata", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

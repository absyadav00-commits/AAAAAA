package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Product
import com.example.ui.theme.StoreGreenPrimary

val PRODUCT_CATEGORIES = listOf(
  "Groceries & Staples",
  "Dairy & Bakery",
  "Snacks & Drinks",
  "Personal Care",
  "Household & Cleaning",
  "Stationery & Essentials",
  "Other"
)

val PRODUCT_UNITS = listOf(
  "pcs",
  "kg",
  "packet",
  "liter",
  "bottle",
  "box",
  "gram",
  "tray"
)

@Composable
fun ProductDialog(
  productToEdit: Product? = null,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onSave: (Product) -> Unit,
  onDelete: ((Product) -> Unit)? = null
) {
  var name by remember { mutableStateOf(productToEdit?.name ?: "") }
  var category by remember { mutableStateOf(productToEdit?.category ?: PRODUCT_CATEGORIES[0]) }
  var sellingPriceStr by remember {
    mutableStateOf(productToEdit?.sellingPrice?.let { if (it > 0) it.toString() else "" } ?: "")
  }
  var costPriceStr by remember {
    mutableStateOf(productToEdit?.costPrice?.let { if (it > 0) it.toString() else "" } ?: "")
  }
  var stockQuantityStr by remember {
    mutableStateOf(productToEdit?.stockQuantity?.let { it.toString() } ?: "10")
  }
  var unit by remember { mutableStateOf(productToEdit?.unit ?: "pcs") }
  var minStockThresholdStr by remember {
    mutableStateOf(productToEdit?.minStockThreshold?.let { it.toString() } ?: "5")
  }
  var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }

  var categoryMenuExpanded by remember { mutableStateOf(false) }
  var unitMenuExpanded by remember { mutableStateOf(false) }
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  val sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
  val costPrice = costPriceStr.toDoubleOrNull() ?: 0.0
  val marginPercent = if (costPrice > 0) ((sellingPrice - costPrice) / costPrice) * 100 else 0.0

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 24.dp)
        .clip(RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Inventory,
              contentDescription = null,
              tint = StoreGreenPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (productToEdit == null) "Add New Product" else "Edit Product",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Product Name *") },
          placeholder = { Text("e.g. Basmati Rice, Tata Salt") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("product_name_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Selector
        Box {
          OutlinedTextField(
            value = category,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
              IconButton(onClick = { categoryMenuExpanded = true }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { categoryMenuExpanded = true }
          )
          DropdownMenu(
            expanded = categoryMenuExpanded,
            onDismissRequest = { categoryMenuExpanded = false }
          ) {
            PRODUCT_CATEGORIES.forEach { cat ->
              DropdownMenuItem(
                text = { Text(cat) },
                onClick = {
                  category = cat
                  categoryMenuExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prices: Selling Price (MRP) & Cost Price (Purchase)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = sellingPriceStr,
            onValueChange = { sellingPriceStr = it },
            label = { Text("Selling Price ($currencySymbol) *") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("product_price_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = costPriceStr,
            onValueChange = { costPriceStr = it },
            label = { Text("Cost Price ($currencySymbol)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        if (sellingPrice > 0 && costPrice > 0) {
          Text(
            text = "Profit Margin: %.1f%% ($currencySymbol%.2f per item)".format(
              marginPercent,
              sellingPrice - costPrice
            ),
            style = MaterialTheme.typography.bodySmall,
            color = if (marginPercent >= 0) StoreGreenPrimary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stock Quantity & Unit
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = stockQuantityStr,
            onValueChange = { stockQuantityStr = it },
            label = { Text("Current Stock *") },
            placeholder = { Text("10") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1.2f)
              .testTag("product_stock_input"),
            singleLine = true
          )

          Box(modifier = Modifier.weight(0.8f)) {
            OutlinedTextField(
              value = unit,
              onValueChange = {},
              readOnly = true,
              label = { Text("Unit") },
              trailingIcon = {
                IconButton(onClick = { unitMenuExpanded = true }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Unit")
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .clickable { unitMenuExpanded = true }
            )
            DropdownMenu(
              expanded = unitMenuExpanded,
              onDismissRequest = { unitMenuExpanded = false }
            ) {
              PRODUCT_UNITS.forEach { u ->
                DropdownMenuItem(
                  text = { Text(u) },
                  onClick = {
                    unit = u
                    unitMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Low stock threshold & Barcode
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = minStockThresholdStr,
            onValueChange = { minStockThresholdStr = it },
            label = { Text("Min Stock Alert") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
          )

          OutlinedTextField(
            value = barcode,
            onValueChange = { barcode = it },
            label = { Text("SKU / Barcode") },
            placeholder = { Text("Optional") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (productToEdit != null && onDelete != null) {
            IconButton(
              onClick = { showDeleteConfirmation = true },
              modifier = Modifier.testTag("delete_product_button")
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Product",
                tint = MaterialTheme.colorScheme.error
              )
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onDismiss) {
              Text("Cancel")
            }

            Button(
              onClick = {
                val parsedSellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
                val parsedCostPrice = costPriceStr.toDoubleOrNull() ?: 0.0
                val parsedStock = stockQuantityStr.toDoubleOrNull() ?: 0.0
                val parsedThreshold = minStockThresholdStr.toDoubleOrNull() ?: 5.0

                if (name.isNotBlank() && parsedSellingPrice > 0) {
                  val product = Product(
                    id = productToEdit?.id ?: 0L,
                    name = name.trim(),
                    category = category,
                    costPrice = parsedCostPrice,
                    sellingPrice = parsedSellingPrice,
                    stockQuantity = parsedStock,
                    unit = unit,
                    minStockThreshold = parsedThreshold,
                    barcode = barcode.trim()
                  )
                  onSave(product)
                }
              },
              enabled = name.isNotBlank() && sellingPriceStr.toDoubleOrNull() != null,
              colors = ButtonDefaults.buttonColors(containerColor = StoreGreenPrimary),
              modifier = Modifier.testTag("save_product_button")
            ) {
              Text("Save Product")
            }
          }
        }
      }
    }
  }

  if (showDeleteConfirmation && productToEdit != null && onDelete != null) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmation = false },
      title = { Text("Delete Product?") },
      text = { Text("Are you sure you want to remove \"${productToEdit.name}\" from store inventory?") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirmation = false
            onDelete(productToEdit)
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmation = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

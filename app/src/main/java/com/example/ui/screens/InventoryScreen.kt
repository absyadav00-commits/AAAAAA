package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.ui.components.PRODUCT_CATEGORIES
import com.example.ui.components.ProductDialog
import com.example.ui.theme.CreditDueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.StockLowContainer
import com.example.ui.theme.StockLowOrange
import com.example.ui.theme.StoreGreenPrimary
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun InventoryScreen(
  viewModel: StoreViewModel
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("All") }
  var selectedStockFilter by remember { mutableStateOf("All") } // "All", "Low Stock", "Out of Stock"

  var productToEdit by remember { mutableStateOf<Product?>(null) }
  var showProductDialog by remember { mutableStateOf(false) }

  val categories = remember { listOf("All") + PRODUCT_CATEGORIES }

  val filteredProducts = allProducts.filter { p ->
    val matchesSearch = searchQuery.isBlank() ||
      p.name.contains(searchQuery, ignoreCase = true) ||
      p.category.contains(searchQuery, ignoreCase = true) ||
      p.barcode.contains(searchQuery, ignoreCase = true)

    val matchesCategory = selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true)

    val matchesStock = when (selectedStockFilter) {
      "Low Stock" -> p.isLowStock
      "Out of Stock" -> p.isOutOfStock
      else -> true
    }

    matchesSearch && matchesCategory && matchesStock
  }

  val totalProductsCount = allProducts.size
  val lowStockCount = allProducts.count { it.isLowStock || it.isOutOfStock }
  val totalStockValue = allProducts.sumOf { it.sellingPrice * it.stockQuantity }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          productToEdit = null
          showProductDialog = true
        },
        containerColor = StoreGreenPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_product_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Product")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Top Summary Stat Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Total Items
        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Total Items", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$totalProductsCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
          }
        }

        // Low Stock alert
        Card(
          modifier = Modifier
            .weight(1f)
            .clickable {
              selectedStockFilter = if (selectedStockFilter == "Low Stock") "All" else "Low Stock"
            },
          colors = CardDefaults.cardColors(
            containerColor = if (lowStockCount > 0) StockLowContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (lowStockCount > 0) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StockLowOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text("Needs Restock", style = MaterialTheme.typography.labelSmall, color = if (lowStockCount > 0) StockLowOrange else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
              "$lowStockCount items",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = if (lowStockCount > 0) StockLowOrange else MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Inventory Value
        Card(
          modifier = Modifier.weight(1.2f),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Stock Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              "${settings.currencySymbol}%.0f".format(totalStockValue),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = StoreGreenPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by product name, category, SKU...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("inventory_search_input"),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Category Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(categories) { cat ->
          val isSelected = selectedCategory == cat
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = cat },
            label = { Text(cat, fontSize = 13.sp) },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = StoreGreenPrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Products List
      if (filteredProducts.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No products match filter", style = MaterialTheme.typography.titleMedium)
            Text("Tap the '+' button to add products to inventory", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 80.dp)
        ) {
          items(filteredProducts, key = { it.id }) { product ->
            InventoryProductCard(
              product = product,
              currencySymbol = settings.currencySymbol,
              onEdit = {
                productToEdit = product
                showProductDialog = true
              },
              onQuickRestock = { addQty ->
                viewModel.quickRestock(product, addQty)
              }
            )
          }
        }
      }
    }
  }

  if (showProductDialog) {
    ProductDialog(
      productToEdit = productToEdit,
      currencySymbol = settings.currencySymbol,
      onDismiss = {
        showProductDialog = false
        productToEdit = null
      },
      onSave = { product ->
        viewModel.saveProduct(product)
        showProductDialog = false
        productToEdit = null
      },
      onDelete = { product ->
        viewModel.deleteProduct(product)
        showProductDialog = false
        productToEdit = null
      }
    )
  }
}

@Composable
private fun InventoryProductCard(
  product: Product,
  currencySymbol: String,
  onEdit: () -> Unit,
  onQuickRestock: (Double) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("inventory_item_${product.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      when {
        product.isOutOfStock -> CreditDueRed.copy(alpha = 0.5f)
        product.isLowStock -> StockLowOrange.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
      }
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = product.category,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Edit button
        IconButton(
          onClick = onEdit,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Price & Cost & Profit Margin row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "$currencySymbol%.2f".format(product.sellingPrice),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = StoreGreenPrimary
          )
          Text(
            text = " / ${product.unit}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (product.costPrice > 0) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Cost: $currencySymbol%.2f".format(product.costPrice),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }

        // Profit margin pill
        if (product.costPrice > 0) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(StoreGreenPrimary.copy(alpha = 0.12f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "+%.1f%% margin".format(product.profitMarginPercent),
              style = MaterialTheme.typography.labelSmall,
              color = StoreGreenPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Stock Level & Quick Restock Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(
                when {
                  product.isOutOfStock -> CreditDueRed
                  product.isLowStock -> StockLowOrange
                  else -> PaidGreen
                }
              )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Stock: ${product.stockQuantity} ${product.unit}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = when {
              product.isOutOfStock -> CreditDueRed
              product.isLowStock -> StockLowOrange
              else -> MaterialTheme.colorScheme.onSurface
            }
          )
        }

        // Quick Restock Buttons (+5, +10, +25)
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Restock:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

          val defaultIncrements = when (product.unit) {
            "kg", "packet", "pcs", "bottle" -> listOf(5.0, 10.0, 20.0)
            else -> listOf(5.0, 10.0, 25.0)
          }

          defaultIncrements.forEach { amount ->
            OutlinedButton(
              onClick = { onQuickRestock(amount) },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
              modifier = Modifier.height(26.dp)
            ) {
              Text("+${amount.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

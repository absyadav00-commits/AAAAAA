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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.SaleOrder
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.CreditDueContainer
import com.example.ui.theme.CreditDueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.StoreGreenPrimary
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
  viewModel: StoreViewModel
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var selectedPaymentFilter by remember { mutableStateOf("ALL") } // "ALL", "CASH", "UPI", "CARD", "CREDIT"
  var selectedOrderForReceipt by remember { mutableStateOf<SaleOrder?>(null) }

  val paymentFilters = listOf("ALL", "CASH", "UPI", "CARD", "CREDIT")

  val filteredOrders = allOrders.filter { order ->
    val matchesSearch = searchQuery.isBlank() ||
      order.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
      order.customerName.contains(searchQuery, ignoreCase = true) ||
      order.customerPhone.contains(searchQuery, ignoreCase = true)

    val matchesFilter = selectedPaymentFilter == "ALL" || order.paymentMode == selectedPaymentFilter
    matchesSearch && matchesFilter
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search invoice #, customer name...") },
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
        .testTag("orders_search_input"),
      shape = RoundedCornerShape(16.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(paymentFilters) { filter ->
        val isSelected = selectedPaymentFilter == filter
        FilterChip(
          selected = isSelected,
          onClick = { selectedPaymentFilter = filter },
          label = { Text(if (filter == "ALL") "All Bills" else filter, fontSize = 13.sp) },
          shape = RoundedCornerShape(20.dp),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = StoreGreenPrimary,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Orders Count
    Text(
      text = "${filteredOrders.size} Invoices Generated",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.outline
    )

    Spacer(modifier = Modifier.height(8.dp))

    // List of Orders
    if (filteredOrders.isEmpty()) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
          Spacer(modifier = Modifier.height(8.dp))
          Text("No bills found", style = MaterialTheme.typography.titleMedium)
          Text("Complete a bill at the POS register to see it recorded here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(filteredOrders, key = { it.id }) { order ->
          OrderCard(
            order = order,
            currencySymbol = settings.currencySymbol,
            onClick = { selectedOrderForReceipt = order }
          )
        }
      }
    }
  }

  // Receipt Modal
  if (selectedOrderForReceipt != null) {
    ReceiptDialog(
      order = selectedOrderForReceipt!!,
      settings = settings,
      onDismiss = { selectedOrderForReceipt = null }
    )
  }
}

@Composable
private fun OrderCard(
  order: SaleOrder,
  currencySymbol: String,
  onClick: () -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
  val isCredit = order.paymentMode == "CREDIT"

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("order_item_${order.id}")
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            tint = StoreGreenPrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = order.invoiceNumber,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        // Status / Payment Mode Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCredit) CreditDueContainer else PaidGreenContainer)
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = if (isCredit) "UDHAR" else order.paymentMode,
            color = if (isCredit) CreditDueRed else PaidGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = order.customerName,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = dateFormat.format(Date(order.timestamp)),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.outline
        )
      }

      if (order.itemsSummary.isNotBlank()) {
        Text(
          text = order.itemsSummary,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (order.totalProfit > 0) "Est. Margin: +$currencySymbol%.2f".format(order.totalProfit) else "Bill Settled",
          style = MaterialTheme.typography.labelSmall,
          color = if (order.totalProfit > 0) PaidGreen else MaterialTheme.colorScheme.outline,
          fontWeight = FontWeight.Medium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Total: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
          Text(
            text = "$currencySymbol%.2f".format(order.totalAmount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = StoreGreenPrimary
          )
        }
      }
    }
  }
}

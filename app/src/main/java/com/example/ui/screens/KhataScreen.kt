package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.KhataTransaction
import com.example.data.model.StoreSettings
import com.example.ui.components.CustomerEditDialog
import com.example.ui.components.KhataTransactionDialog
import com.example.ui.theme.CreditDueContainer
import com.example.ui.theme.CreditDueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.StoreGreenPrimary
import com.example.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun KhataScreen(
  viewModel: StoreViewModel
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var customerForDialog by remember { mutableStateOf<Customer?>(null) }
  var showCustomerEditDialog by remember { mutableStateOf(false) }

  // Khata transaction (repayment or manual credit)
  var transactionCustomer by remember { mutableStateOf<Customer?>(null) }
  var isRecordingPayment by remember { mutableStateOf(true) }
  var showTransactionDialog by remember { mutableStateOf(false) }

  // Customer Statement Modal
  var statementCustomer by remember { mutableStateOf<Customer?>(null) }
  val statementSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  val totalUdharDue = allCustomers.sumOf { it.totalBalanceDue }
  val customersWithDueCount = allCustomers.count { it.totalBalanceDue > 0 }

  val filteredCustomers = allCustomers.filter { c ->
    searchQuery.isBlank() ||
      c.name.contains(searchQuery, ignoreCase = true) ||
      c.phone.contains(searchQuery, ignoreCase = true) ||
      c.address.contains(searchQuery, ignoreCase = true)
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          customerForDialog = null
          showCustomerEditDialog = true
        },
        containerColor = StoreGreenPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_customer_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Khata Customer")
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

      // Udhar Overview Banner
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (totalUdharDue > 0) CreditDueContainer.copy(alpha = 0.5f) else PaidGreenContainer.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (totalUdharDue > 0) CreditDueRed.copy(alpha = 0.3f) else PaidGreen.copy(alpha = 0.3f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Total Credit Due (Udhar Book)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (totalUdharDue > 0) CreditDueRed else PaidGreen
            )
            Text(
              text = "${settings.currencySymbol}%.2f".format(totalUdharDue),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = if (totalUdharDue > 0) CreditDueRed else PaidGreen
            )
            Text(
              text = "$customersWithDueCount customer(s) have pending balances",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = if (totalUdharDue > 0) CreditDueRed else PaidGreen,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search customer name, phone, address...") },
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
          .testTag("khata_search_input"),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Customers List
      if (filteredCustomers.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No customer found", style = MaterialTheme.typography.titleMedium)
            Text("Tap the '+' button to add customers to your Khata book", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 80.dp)
        ) {
          items(filteredCustomers, key = { it.id }) { customer ->
            CustomerKhataCard(
              customer = customer,
              currencySymbol = settings.currencySymbol,
              onReceivePayment = {
                transactionCustomer = customer
                isRecordingPayment = true
                showTransactionDialog = true
              },
              onGiveCredit = {
                transactionCustomer = customer
                isRecordingPayment = false
                showTransactionDialog = true
              },
              onSendReminder = {
                sendPaymentReminder(context, customer, settings)
              },
              onViewStatement = {
                statementCustomer = customer
              },
              onEdit = {
                customerForDialog = customer
                showCustomerEditDialog = true
              }
            )
          }
        }
      }
    }
  }

  // Customer Edit/Add Dialog
  if (showCustomerEditDialog) {
    CustomerEditDialog(
      customerToEdit = customerForDialog,
      onDismiss = {
        showCustomerEditDialog = false
        customerForDialog = null
      },
      onSave = { customer ->
        viewModel.saveCustomer(customer)
        showCustomerEditDialog = false
        customerForDialog = null
      },
      onDelete = { customer ->
        viewModel.deleteCustomer(customer)
        showCustomerEditDialog = false
        customerForDialog = null
      }
    )
  }

  // Payment / Credit Transaction Dialog
  if (showTransactionDialog && transactionCustomer != null) {
    val cust = transactionCustomer!!
    KhataTransactionDialog(
      customer = cust,
      currencySymbol = settings.currencySymbol,
      isRecordingPayment = isRecordingPayment,
      onDismiss = {
        showTransactionDialog = false
        transactionCustomer = null
      },
      onConfirm = { amount, method, note ->
        if (isRecordingPayment) {
          viewModel.recordRepayment(cust, amount, method, note)
        } else {
          viewModel.recordCredit(cust, amount, note)
        }
        showTransactionDialog = false
        transactionCustomer = null
      }
    )
  }

  // Customer Statement Bottom Sheet
  if (statementCustomer != null) {
    ModalBottomSheet(
      onDismissRequest = { statementCustomer = null },
      sheetState = statementSheetState
    ) {
      CustomerStatementSheetContent(
        customer = statementCustomer!!,
        viewModel = viewModel,
        settings = settings,
        onClose = {
          scope.launch { statementSheetState.hide() }.invokeOnCompletion {
            statementCustomer = null
          }
        }
      )
    }
  }
}

@Composable
private fun CustomerKhataCard(
  customer: Customer,
  currencySymbol: String,
  onReceivePayment: () -> Unit,
  onGiveCredit: () -> Unit,
  onSendReminder: () -> Unit,
  onViewStatement: () -> Unit,
  onEdit: () -> Unit
) {
  val hasDue = customer.totalBalanceDue > 0

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("khata_customer_${customer.id}")
      .clickable { onViewStatement() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (hasDue) CreditDueRed.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(if (hasDue) CreditDueContainer else PaidGreenContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = customer.name.take(1).uppercase(),
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = if (hasDue) CreditDueRed else PaidGreen
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = customer.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            if (customer.phone.isNotBlank()) {
              Text(
                text = customer.phone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            if (customer.address.isNotBlank()) {
              Text(
                text = customer.address,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }
        }

        // Balance Due Badge
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = if (hasDue) "Due Amount" else "Status",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
          )
          Text(
            text = if (hasDue) "$currencySymbol%.2f".format(customer.totalBalanceDue) else "Settled",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (hasDue) CreditDueRed else PaidGreen
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
      Spacer(modifier = Modifier.height(8.dp))

      // Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Receive Payment Button
          Button(
            onClick = onReceivePayment,
            colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Got Money", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Give Credit Button
          OutlinedButton(
            onClick = onGiveCredit,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Text("+ Give Udhar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CreditDueRed)
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (hasDue) {
            IconButton(
              onClick = onSendReminder,
              modifier = Modifier.size(34.dp)
            ) {
              Icon(Icons.Default.Send, contentDescription = "Send Reminder", tint = StoreGreenPrimary, modifier = Modifier.size(18.dp))
            }
          }

          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun CustomerStatementSheetContent(
  customer: Customer,
  viewModel: StoreViewModel,
  settings: StoreSettings,
  onClose: () -> Unit
) {
  val transactions by viewModel.getCustomerTransactions(customer.id).collectAsStateWithLifecycle(emptyList())
  val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(customer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
          "Khata Statement • Current Due: ${settings.currencySymbol}%.2f".format(customer.totalBalanceDue),
          style = MaterialTheme.typography.bodyMedium,
          color = if (customer.totalBalanceDue > 0) CreditDueRed else PaidGreen,
          fontWeight = FontWeight.SemiBold
        )
      }
      IconButton(onClick = onClose) {
        Icon(Icons.Default.Clear, contentDescription = "Close")
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text("Transaction History", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
    Spacer(modifier = Modifier.height(8.dp))

    if (transactions.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp),
        contentAlignment = Alignment.Center
      ) {
        Text("No transaction records yet", color = MaterialTheme.colorScheme.outline)
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(280.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(transactions) { tx ->
          val isPayment = tx.type == "RECEIVED_PAYMENT"
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isPayment) "Payment Received (Credited)" else "Udhar Given (Debited)",
                fontWeight = FontWeight.SemiBold,
                color = if (isPayment) PaidGreen else CreditDueRed,
                fontSize = 13.sp
              )
              if (tx.note.isNotBlank()) {
                Text(tx.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Text(dateFormat.format(Date(tx.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }

            Text(
              text = "${if (isPayment) "- " else "+ "}${settings.currencySymbol}%.2f".format(tx.amount),
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = if (isPayment) PaidGreen else CreditDueRed
            )
          }
        }
      }
    }
  }
}

private fun sendPaymentReminder(
  context: Context,
  customer: Customer,
  settings: StoreSettings
) {
  val message = """
    Namaste ${customer.name},
    This is a friendly reminder from *${settings.storeName}*.
    Your pending store balance is *${settings.currencySymbol}%.2f*.
    
    You can pay in cash or online via UPI: ${settings.upiId}
    Thank you for being our valued customer!
  """.trimIndent().format(customer.totalBalanceDue)

  val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, message)
    type = "text/plain"
  }
  val shareIntent = Intent.createChooser(sendIntent, "Send Payment Reminder")
  context.startActivity(shareIntent)
}

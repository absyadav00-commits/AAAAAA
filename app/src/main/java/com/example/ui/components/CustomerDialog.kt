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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import com.example.data.model.Customer
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.StoreGreenPrimary

@Composable
fun CustomerEditDialog(
  customerToEdit: Customer? = null,
  onDismiss: () -> Unit,
  onSave: (Customer) -> Unit,
  onDelete: ((Customer) -> Unit)? = null
) {
  var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
  var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
  var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 24.dp)
        .clip(RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = StoreGreenPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (customerToEdit == null) "New Khata Customer" else "Edit Customer",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Customer Name *") },
          placeholder = { Text("e.g. Ramesh Kumar") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_name_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          placeholder = { Text("e.g. +91 98765 43210") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_phone_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Address / Landmark") },
          placeholder = { Text("e.g. Flat 102, Shanti Appts") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (customerToEdit != null && onDelete != null) {
            IconButton(onClick = { showDeleteConfirmation = true }) {
              Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Customer",
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
                if (name.isNotBlank()) {
                  val customer = Customer(
                    id = customerToEdit?.id ?: 0L,
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    totalBalanceDue = customerToEdit?.totalBalanceDue ?: 0.0
                  )
                  onSave(customer)
                }
              },
              enabled = name.isNotBlank(),
              colors = ButtonDefaults.buttonColors(containerColor = StoreGreenPrimary),
              modifier = Modifier.testTag("save_customer_button")
            ) {
              Text("Save")
            }
          }
        }
      }
    }
  }

  if (showDeleteConfirmation && customerToEdit != null && onDelete != null) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmation = false },
      title = { Text("Delete Customer?") },
      text = { Text("Are you sure you want to delete ${customerToEdit.name}'s account and Khata ledger?") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirmation = false
            onDelete(customerToEdit)
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

@Composable
fun KhataTransactionDialog(
  customer: Customer,
  currencySymbol: String,
  isRecordingPayment: Boolean, // true = customer paid money, false = gave credit/udhar
  onDismiss: () -> Unit,
  onConfirm: (amount: Double, method: String, note: String) -> Unit
) {
  var amountStr by remember {
    mutableStateOf(if (isRecordingPayment && customer.totalBalanceDue > 0) customer.totalBalanceDue.toString() else "")
  }
  var selectedMethod by remember { mutableStateOf("Cash") }
  var note by remember { mutableStateOf("") }
  var methodMenuExpanded by remember { mutableStateOf(false) }

  val paymentMethods = listOf("Cash", "UPI / QR", "Bank Transfer", "Cheque")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 24.dp)
        .clip(RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isRecordingPayment) "Receive Payment" else "Give Credit (Udhar)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isRecordingPayment) PaidGreen else MaterialTheme.colorScheme.error
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Text(
          text = "Customer: ${customer.name}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(top = 4.dp)
        )

        Text(
          text = "Current Balance Due: $currencySymbol%.2f".format(customer.totalBalanceDue),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text("Amount ($currencySymbol) *") },
          placeholder = { Text("0.00") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("khata_amount_input"),
          singleLine = true
        )

        if (isRecordingPayment) {
          Spacer(modifier = Modifier.height(12.dp))

          Box {
            OutlinedTextField(
              value = selectedMethod,
              onValueChange = {},
              readOnly = true,
              label = { Text("Payment Method") },
              trailingIcon = {
                IconButton(onClick = { methodMenuExpanded = true }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Method")
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .clickable { methodMenuExpanded = true }
            )
            DropdownMenu(
              expanded = methodMenuExpanded,
              onDismissRequest = { methodMenuExpanded = false }
            ) {
              paymentMethods.forEach { method ->
                DropdownMenuItem(
                  text = { Text(method) },
                  onClick = {
                    selectedMethod = method
                    methodMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Note / Description (Optional)") },
          placeholder = { Text(if (isRecordingPayment) "e.g. Cleared bill, partial payment" else "e.g. Lent money, grocery credit") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(onClick = onDismiss) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = {
              val amount = amountStr.toDoubleOrNull() ?: 0.0
              if (amount > 0) {
                onConfirm(amount, selectedMethod, note)
              }
            },
            enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0.0,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isRecordingPayment) PaidGreen else MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.testTag("confirm_khata_button")
          ) {
            Text(if (isRecordingPayment) "Confirm Payment" else "Confirm Credit")
          }
        }
      }
    }
  }
}

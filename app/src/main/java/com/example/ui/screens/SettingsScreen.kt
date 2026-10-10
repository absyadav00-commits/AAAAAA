package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StoreSettings
import com.example.ui.theme.StoreGreenPrimary
import com.example.ui.viewmodel.StoreViewModel

val CURRENCIES = listOf("₹ (INR)", "$ (USD)", "€ (EUR)", "£ (GBP)", "AED", "¥ (JPY)")

@Composable
fun SettingsScreen(
  viewModel: StoreViewModel
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()

  var storeName by remember(settings) { mutableStateOf(settings.storeName) }
  var tagline by remember(settings) { mutableStateOf(settings.tagline) }
  var phone by remember(settings) { mutableStateOf(settings.phone) }
  var address by remember(settings) { mutableStateOf(settings.address) }
  var currencySymbol by remember(settings) { mutableStateOf(settings.currencySymbol) }
  var taxPercentStr by remember(settings) {
    mutableStateOf(if (settings.taxPercent > 0) settings.taxPercent.toString() else "0")
  }
  var upiId by remember(settings) { mutableStateOf(settings.upiId) }
  var receiptFooter by remember(settings) { mutableStateOf(settings.receiptFooterMessage) }

  var currencyMenuExpanded by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Store Profile Header Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Storefront,
          contentDescription = null,
          tint = StoreGreenPrimary,
          modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Store Configuration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Customize invoice details, currency, and UPI settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Store Details
    OutlinedTextField(
      value = storeName,
      onValueChange = { storeName = it },
      label = { Text("Store Name *") },
      placeholder = { Text("e.g. Yadav General Store") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("store_name_input"),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = tagline,
      onValueChange = { tagline = it },
      label = { Text("Store Tagline / Slogan") },
      placeholder = { Text("e.g. All Daily Groceries & Essentials") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text("Store Phone Number") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.weight(1f),
        singleLine = true
      )

      // Currency dropdown
      Box(modifier = Modifier.weight(1f)) {
        OutlinedTextField(
          value = currencySymbol,
          onValueChange = { currencySymbol = it },
          label = { Text("Currency Symbol") },
          trailingIcon = {
            IconButton(onClick = { currencyMenuExpanded = true }) {
              Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Currency")
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .clickable { currencyMenuExpanded = true },
          singleLine = true
        )
        DropdownMenu(
          expanded = currencyMenuExpanded,
          onDismissRequest = { currencyMenuExpanded = false }
        ) {
          CURRENCIES.forEach { curr ->
            val symbol = curr.split(" ")[0]
            DropdownMenuItem(
              text = { Text(curr) },
              onClick = {
                currencySymbol = symbol
                currencyMenuExpanded = false
              }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = address,
      onValueChange = { address = it },
      label = { Text("Store Address / Market Location") },
      placeholder = { Text("e.g. Main Market, Shop No. 12") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedTextField(
        value = upiId,
        onValueChange = { upiId = it },
        label = { Text("Store UPI ID for Digital Payments") },
        placeholder = { Text("e.g. storename@upi") },
        modifier = Modifier.weight(1.3f),
        singleLine = true
      )

      OutlinedTextField(
        value = taxPercentStr,
        onValueChange = { taxPercentStr = it },
        label = { Text("Default Tax %") },
        placeholder = { Text("0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.weight(0.7f),
        singleLine = true
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = receiptFooter,
      onValueChange = { receiptFooter = it },
      label = { Text("Receipt Footer Message") },
      placeholder = { Text("Thank you for shopping with us!") },
      modifier = Modifier.fillMaxWidth(),
      maxLines = 2
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Save button
    Button(
      onClick = {
        val updated = StoreSettings(
          storeName = storeName.trim(),
          tagline = tagline.trim(),
          phone = phone.trim(),
          address = address.trim(),
          currencySymbol = currencySymbol.trim(),
          taxPercent = taxPercentStr.toDoubleOrNull() ?: 0.0,
          upiId = upiId.trim(),
          receiptFooterMessage = receiptFooter.trim()
        )
        viewModel.saveSettings(updated)
        Toast.makeText(context, "Store settings saved!", Toast.LENGTH_SHORT).show()
      },
      colors = ButtonDefaults.buttonColors(containerColor = StoreGreenPrimary),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("save_store_settings_button")
    ) {
      Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Save Store Settings", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Demo Data Seeding Section
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("Data Management", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
          "Reset or populate starter grocery inventory (rice, flour, milk, snacks, etc.) and demo customers.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
          onClick = { showResetConfirmDialog = true },
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Load Starter General Store Catalog")
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))
  }

  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = { Text("Load Starter Store Data?") },
      text = { Text("This will add authentic staples, dairy, snacks, toiletries, and sample customer accounts to your store database.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetSampleData()
            showResetConfirmDialog = false
            Toast.makeText(context, "Starter inventory loaded!", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = StoreGreenPrimary)
        ) {
          Text("Confirm Load")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

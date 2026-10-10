package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SaleOrder
import com.example.data.model.StoreSettings
import com.example.ui.theme.CreditDueContainer
import com.example.ui.theme.CreditDueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.StoreGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDialog(
  order: SaleOrder,
  settings: StoreSettings,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
  val formattedDate = dateFormat.format(Date(order.timestamp))

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
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Receipt,
              contentDescription = "Receipt",
              tint = StoreGreenPrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Tax Invoice",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_receipt_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Printable Thermal Receipt Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = settings.storeName,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              textAlign = TextAlign.Center
            )
            if (settings.tagline.isNotBlank()) {
              Text(
                text = settings.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              )
            }
            Text(
              text = "${settings.address} • Phone: ${settings.phone}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Invoice Info Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "Invoice: ${order.invoiceNumber}",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Date: $formattedDate",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              // Payment Mode Badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (order.paymentStatus == "PAID") PaidGreenContainer else CreditDueContainer
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = if (order.paymentStatus == "PAID") "${order.paymentMode} • PAID" else "UDHAR (CREDIT)",
                  color = if (order.paymentStatus == "PAID") PaidGreen else CreditDueRed,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Customer: ${order.customerName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
              )
              if (order.customerPhone.isNotBlank()) {
                Text(
                  text = order.customerPhone,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Items List
            Text(
              text = "ITEMS PURCHASED",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Item summary text
            Text(
              text = order.itemsSummary.replace(", ", "\n• "),
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = FontFamily.Monospace,
              lineHeight = 20.sp,
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown Totals
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
              Text(
                "${settings.currencySymbol}%.2f".format(order.subtotal),
                style = MaterialTheme.typography.bodyMedium
              )
            }

            if (order.discount > 0.0) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Discount", style = MaterialTheme.typography.bodyMedium, color = PaidGreen)
                Text(
                  "-${settings.currencySymbol}%.2f".format(order.discount),
                  style = MaterialTheme.typography.bodyMedium,
                  color = PaidGreen
                )
              }
            }

            if (order.taxAmount > 0.0) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Tax", style = MaterialTheme.typography.bodyMedium)
                Text(
                  "+${settings.currencySymbol}%.2f".format(order.taxAmount),
                  style = MaterialTheme.typography.bodyMedium
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "TOTAL AMOUNT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                "${settings.currencySymbol}%.2f".format(order.totalAmount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = StoreGreenPrimary
              )
            }

            if (settings.upiId.isNotBlank()) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Pay via UPI: ${settings.upiId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = settings.receiptFooterMessage,
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              shareReceipt(context, order, settings, formattedDate)
            },
            modifier = Modifier
              .weight(1f)
              .testTag("share_receipt_button")
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share Bill")
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("done_receipt_button"),
            colors = ButtonDefaults.buttonColors(containerColor = StoreGreenPrimary)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Done")
          }
        }
      }
    }
  }
}

private fun shareReceipt(
  context: Context,
  order: SaleOrder,
  settings: StoreSettings,
  formattedDate: String
) {
  val receiptText = """
    🧾 *${settings.storeName}*
    ${settings.tagline}
    📍 ${settings.address}
    📞 ${settings.phone}
    -------------------------------
    *Invoice:* ${order.invoiceNumber}
    *Date:* $formattedDate
    *Customer:* ${order.customerName}
    *Payment:* ${order.paymentMode} (${order.paymentStatus})
    -------------------------------
    *Items:*
    ${order.itemsSummary.replace(", ", "\n• ")}
    -------------------------------
    Subtotal: ${settings.currencySymbol}%.2f
    ${if (order.discount > 0) "Discount: -${settings.currencySymbol}%.2f\n" else ""}${if (order.taxAmount > 0) "Tax: +${settings.currencySymbol}%.2f\n" else ""}*Total Amount: ${settings.currencySymbol}%.2f*
    -------------------------------
    ${if (settings.upiId.isNotBlank()) "UPI ID: ${settings.upiId}\n" else ""}${settings.receiptFooterMessage}
  """.trimIndent().format(
    order.subtotal,
    order.discount,
    order.taxAmount,
    order.totalAmount
  )

  val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, receiptText)
    type = "text/plain"
  }
  val shareIntent = Intent.createChooser(sendIntent, "Share Bill Receipt")
  context.startActivity(shareIntent)
}

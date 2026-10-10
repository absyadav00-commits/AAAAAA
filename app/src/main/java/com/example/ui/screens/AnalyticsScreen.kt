package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.CreditDueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.StockLowContainer
import com.example.ui.theme.StockLowOrange
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreBlue
import com.example.ui.theme.StoreGreenPrimary
import com.example.ui.viewmodel.StoreViewModel
import java.util.Calendar

@Composable
fun AnalyticsScreen(
  viewModel: StoreViewModel,
  onNavigateToInventory: () -> Unit
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
  val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
  val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
  val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

  // Calculate Today's Start Timestamp (00:00:00 today)
  val todayStart = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }.timeInMillis

  val todayOrders = allOrders.filter { it.timestamp >= todayStart }
  val todaySales = todayOrders.sumOf { it.totalAmount }
  val todayProfit = todayOrders.sumOf { it.totalProfit }
  val todayBillsCount = todayOrders.size
  val avgBillValue = if (todayBillsCount > 0) todaySales / todayBillsCount else 0.0

  val totalSalesAllTime = allOrders.sumOf { it.totalAmount }
  val totalProfitAllTime = allOrders.sumOf { it.totalProfit }
  val totalUdharDue = allCustomers.sumOf { it.totalBalanceDue }

  // Payment Breakdown
  val cashSales = allOrders.filter { it.paymentMode == "CASH" }.sumOf { it.totalAmount }
  val upiSales = allOrders.filter { it.paymentMode == "UPI" }.sumOf { it.totalAmount }
  val cardSales = allOrders.filter { it.paymentMode == "CARD" }.sumOf { it.totalAmount }
  val creditSales = allOrders.filter { it.paymentMode == "CREDIT" }.sumOf { it.totalAmount }
  val totalPaymentSum = (cashSales + upiSales + cardSales + creditSales).coerceAtLeast(1.0)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 32.dp)
  ) {
    // Hero Store Banner with Visual Polish
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
    ) {
      Image(
        painter = painterResource(id = R.drawable.store_hero_banner),
        contentDescription = "General Store Banner",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // Gradient overlay for readability
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.85f)
              )
            )
          )
      )

      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp)
      ) {
        Text(
          text = settings.storeName,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White
        )
        Text(
          text = settings.tagline,
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.85f)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      // Today's Performance Header
      Text(
        text = "TODAY'S OVERVIEW",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Today's Stats Cards (2x2 grid)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Today's Revenue
        Card(
          modifier = Modifier
            .weight(1f)
            .testTag("today_revenue_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PaidGreenContainer.copy(alpha = 0.6f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Today's Sales", style = MaterialTheme.typography.labelMedium, color = PaidGreen, fontWeight = FontWeight.Bold)
              Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${settings.currencySymbol}%.2f".format(todaySales),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = StoreGreenPrimary
            )
            Text(
              text = "$todayBillsCount bills generated",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Today's Profit
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Est. Profit", style = MaterialTheme.typography.labelMedium, color = StoreAmber, fontWeight = FontWeight.Bold)
              Icon(Icons.Default.Payments, contentDescription = null, tint = StoreAmber, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${settings.currencySymbol}%.2f".format(todayProfit),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Avg: ${settings.currencySymbol}%.0f/bill".format(avgBillValue),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // All-Time Summary Row
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Store Sales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(
              "${settings.currencySymbol}%.1f".format(totalSalesAllTime),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = StoreGreenPrimary
            )
          }

          HorizontalDivider(
            modifier = Modifier
              .height(36.dp)
              .width(1.dp)
          )

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Profit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(
              "${settings.currencySymbol}%.1f".format(totalProfitAllTime),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = PaidGreen
            )
          }

          HorizontalDivider(
            modifier = Modifier
              .height(36.dp)
              .width(1.dp)
          )

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Udhar in Market", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(
              "${settings.currencySymbol}%.1f".format(totalUdharDue),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (totalUdharDue > 0) CreditDueRed else PaidGreen
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Payment Modes Breakdown
      Text(
        text = "PAYMENT METHOD DISTRIBUTION",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Cash
          PaymentModeProgressRow(
            title = "Cash Payments",
            amount = cashSales,
            fraction = (cashSales / totalPaymentSum).toFloat(),
            icon = Icons.Default.LocalAtm,
            color = StoreGreenPrimary,
            currencySymbol = settings.currencySymbol
          )

          Spacer(modifier = Modifier.height(10.dp))

          // UPI
          PaymentModeProgressRow(
            title = "UPI / QR Payments",
            amount = upiSales,
            fraction = (upiSales / totalPaymentSum).toFloat(),
            icon = Icons.Default.QrCodeScanner,
            color = StoreBlue,
            currencySymbol = settings.currencySymbol
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Card
          PaymentModeProgressRow(
            title = "Card Payments",
            amount = cardSales,
            fraction = (cardSales / totalPaymentSum).toFloat(),
            icon = Icons.Default.CreditCard,
            color = Color(0xFF64748B),
            currencySymbol = settings.currencySymbol
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Credit / Udhar
          PaymentModeProgressRow(
            title = "Udhar / Credit Given",
            amount = creditSales,
            fraction = (creditSales / totalPaymentSum).toFloat(),
            icon = Icons.Default.MenuBook,
            color = CreditDueRed,
            currencySymbol = settings.currencySymbol
          )
        }
      }

      // Low Stock Alert Box if any items are low
      if (lowStockProducts.isNotEmpty()) {
        Spacer(modifier = Modifier.height(18.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = StockLowContainer),
          border = androidx.compose.foundation.BorderStroke(1.dp, StockLowOrange.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StockLowOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Urgent: ${lowStockProducts.size} Items Need Restocking",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = StockLowOrange
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            lowStockProducts.take(4).forEach { p ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "• ${p.name}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Only ${p.stockQuantity} ${p.unit} left",
                  style = MaterialTheme.typography.labelSmall,
                  color = StockLowOrange,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PaymentModeProgressRow(
  title: String,
  amount: Double,
  fraction: Float,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  currencySymbol: String
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
      }
      Text(
        "$currencySymbol%.2f (%.0f%%)".format(amount, fraction * 100),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { fraction },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = color,
      trackColor = color.copy(alpha = 0.15f)
    )
  }
}

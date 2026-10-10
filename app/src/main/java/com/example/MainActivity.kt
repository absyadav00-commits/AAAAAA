package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.KhataScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.POSScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StockLowOrange
import com.example.ui.theme.StoreGreenPrimary
import com.example.ui.viewmodel.StoreViewModel

enum class StoreNavScreen(val label: String, val icon: ImageVector) {
  POS("Billing", Icons.Default.PointOfSale),
  INVENTORY("Inventory", Icons.Default.Inventory2),
  KHATA("Khata", Icons.Default.MenuBook),
  ORDERS("Bills", Icons.Default.History),
  DASHBOARD("Insights", Icons.Default.Analytics),
  SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        GeneralStoreApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralStoreApp(viewModel: StoreViewModel = viewModel()) {
  var currentScreen by remember { mutableStateOf(StoreNavScreen.POS) }
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
  val completedOrder by viewModel.completedOrderForReceipt.collectAsStateWithLifecycle()

  // Handle back button to return to POS screen
  BackHandler(enabled = currentScreen != StoreNavScreen.POS) {
    currentScreen = StoreNavScreen.POS
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = when (currentScreen) {
              StoreNavScreen.SETTINGS -> "Store Settings"
              else -> settings.storeName
            },
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        navigationIcon = {
          Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = "Store",
            tint = StoreGreenPrimary,
            modifier = Modifier
              .padding(start = 12.dp)
              .size(26.dp)
          )
        },
        actions = {
          // Low stock alert indicator
          if (lowStockProducts.isNotEmpty() && currentScreen != StoreNavScreen.INVENTORY) {
            IconButton(
              onClick = { currentScreen = StoreNavScreen.INVENTORY },
              modifier = Modifier.testTag("low_stock_appbar_button")
            ) {
              BadgedBox(
                badge = {
                  Badge(containerColor = StockLowOrange) {
                    Text("${lowStockProducts.size}")
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = "Low Stock Alerts",
                  tint = StockLowOrange
                )
              }
            }
          }

          // Settings action button
          IconButton(
            onClick = {
              currentScreen = if (currentScreen == StoreNavScreen.SETTINGS) StoreNavScreen.POS else StoreNavScreen.SETTINGS
            },
            modifier = Modifier.testTag("settings_top_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = if (currentScreen == StoreNavScreen.SETTINGS) StoreGreenPrimary else MaterialTheme.colorScheme.onSurface
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
      ) {
        val navItems = listOf(
          StoreNavScreen.POS,
          StoreNavScreen.INVENTORY,
          StoreNavScreen.KHATA,
          StoreNavScreen.ORDERS,
          StoreNavScreen.DASHBOARD
        )

        navItems.forEach { screen ->
          val isSelected = currentScreen == screen
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentScreen = screen },
            icon = {
              if (screen == StoreNavScreen.INVENTORY && lowStockProducts.isNotEmpty()) {
                BadgedBox(
                  badge = {
                    Badge(containerColor = StockLowOrange) {
                      Text("${lowStockProducts.size}")
                    }
                  }
                ) {
                  Icon(screen.icon, contentDescription = screen.label)
                }
              } else {
                Icon(screen.icon, contentDescription = screen.label)
              }
            },
            label = {
              Text(
                text = screen.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = StoreGreenPrimary,
              selectedTextColor = StoreGreenPrimary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentScreen) {
        StoreNavScreen.POS -> {
          POSScreen(
            viewModel = viewModel,
            onOpenCustomerKhata = { currentScreen = StoreNavScreen.KHATA }
          )
        }
        StoreNavScreen.INVENTORY -> {
          InventoryScreen(viewModel = viewModel)
        }
        StoreNavScreen.KHATA -> {
          KhataScreen(viewModel = viewModel)
        }
        StoreNavScreen.ORDERS -> {
          OrdersScreen(viewModel = viewModel)
        }
        StoreNavScreen.DASHBOARD -> {
          AnalyticsScreen(
            viewModel = viewModel,
            onNavigateToInventory = { currentScreen = StoreNavScreen.INVENTORY }
          )
        }
        StoreNavScreen.SETTINGS -> {
          SettingsScreen(viewModel = viewModel)
        }
      }
    }
  }

  // Automatic Receipt Modal when a sale order finishes checkout
  if (completedOrder != null) {
    ReceiptDialog(
      order = completedOrder!!,
      settings = settings,
      onDismiss = { viewModel.completedOrderForReceipt.value = null }
    )
  }
}

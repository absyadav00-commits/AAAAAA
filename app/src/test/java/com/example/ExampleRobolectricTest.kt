package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CartItem
import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("General Store", appName)
  }

  @Test
  fun `product profit margin and cart calculation`() {
    val product = Product(
      name = "Basmati Rice",
      category = "Groceries & Staples",
      costPrice = 80.0,
      sellingPrice = 100.0,
      stockQuantity = 20.0
    )
    assertEquals(25.0, product.profitMarginPercent, 0.01)

    val cartItem = CartItem(
      productId = 1L,
      productName = product.name,
      category = product.category,
      unitPrice = product.sellingPrice,
      costPrice = product.costPrice,
      unit = "kg",
      quantity = 3.0,
      availableStock = 20.0
    )
    assertEquals(300.0, cartItem.subtotal, 0.01)
  }
}

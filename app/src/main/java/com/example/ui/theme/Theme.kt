package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = StoreGreenPrimaryDark,
  onPrimary = Color(0xFF00390B),
  primaryContainer = StoreGreenContainerDark,
  onPrimaryContainer = Color(0xFF9DF59F),
  secondary = StoreAmber,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF563600),
  onSecondaryContainer = StoreAmberContainer,
  tertiary = Color(0xFF70D2FF),
  background = StoreBackgroundDark,
  surface = StoreSurfaceDark,
  surfaceVariant = StoreSurfaceVariantDark,
  onBackground = StoreTextPrimaryDark,
  onSurface = StoreTextPrimaryDark,
  outline = StoreOutlineDark
)

private val LightColorScheme = lightColorScheme(
  primary = StoreGreenPrimary,
  onPrimary = StoreGreenOnPrimary,
  primaryContainer = StoreGreenContainer,
  onPrimaryContainer = StoreGreenOnContainer,
  secondary = StoreAmber,
  onSecondary = Color.White,
  secondaryContainer = StoreAmberContainer,
  onSecondaryContainer = StoreAmberOnContainer,
  tertiary = StoreBlue,
  background = StoreBackgroundLight,
  surface = StoreSurfaceLight,
  surfaceVariant = StoreSurfaceVariantLight,
  onBackground = StoreTextPrimaryLight,
  onSurface = StoreTextPrimaryLight,
  outline = StoreOutlineLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set false to maintain distinctive store branding by default
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

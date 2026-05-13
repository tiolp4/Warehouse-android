package com.example.warehouse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary           = Brand40,
    onPrimary         = Color.White,
    primaryContainer  = Brand90,
    onPrimaryContainer = Brand10,
    secondary         = Accent40,
    onSecondary       = Color.White,
    secondaryContainer = Accent80,
    onSecondaryContainer = Color(0xFF052B0B),
    tertiary          = Warn40,
    onTertiary        = Color.White,
    tertiaryContainer = Warn80,
    background        = NeutralLight,
    onBackground      = Color(0xFF1A1C24),
    surface           = Color.White,
    onSurface         = Color(0xFF1A1C24),
    surfaceVariant    = Brand95,
    onSurfaceVariant  = Color(0xFF454754),
    outline           = OutlineLight
)

private val DarkColors = darkColorScheme(
    primary           = Brand80,
    onPrimary         = Brand10,
    primaryContainer  = Brand30,
    onPrimaryContainer = Brand90,
    secondary         = Accent80,
    onSecondary       = Color(0xFF052B0B),
    tertiary          = Warn80,
    onTertiary        = Color(0xFF2E1500),
    background        = SurfaceDark,
    onBackground      = Color(0xFFE5E7EE),
    surface           = Color(0xFF181C28),
    onSurface         = Color(0xFFE5E7EE),
    surfaceVariant    = Color(0xFF252A38),
    onSurfaceVariant  = Color(0xFFC5C8D2),
    outline           = Color(0xFF3A3F4D)
)

@Composable
fun WarehouseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

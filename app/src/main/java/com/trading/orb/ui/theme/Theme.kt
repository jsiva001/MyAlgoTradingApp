package com.trading.orb.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.trading.orb.data.model.TradingMode

// Paper Mode (Blue) Color Scheme
private val PaperColorScheme = darkColorScheme(
    primary = PaperPrimary,
    onPrimary = OnPrimary,
    secondary = PaperPrimaryVariant,
    background = PaperBackground,
    surface = PaperSurface,
    surfaceVariant = PaperSurfaceVariant,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = OnPrimary
)

// Live Mode (Red) Color Scheme
private val LiveColorScheme = darkColorScheme(
    primary = LivePrimary,
    onPrimary = OnPrimary,
    secondary = LivePrimaryVariant,
    background = LiveBackground,
    surface = LiveSurface,
    surfaceVariant = LiveSurfaceVariant,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = OnPrimary
)

@Composable
fun OrbTradingTheme(
    tradingMode: TradingMode = TradingMode.PAPER,
    darkTheme: Boolean = true, // Always dark for trading app
    content: @Composable () -> Unit
) {
    val colorScheme = when (tradingMode) {
        TradingMode.PAPER -> PaperColorScheme
        TradingMode.LIVE -> LiveColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

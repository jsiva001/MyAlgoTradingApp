package com.trading.orb.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trading.orb.data.model.*
import com.trading.orb.ui.components.*
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.theme.*

@Composable
fun DashboardScreen(
    uiState: DashboardUiState = DashboardUiState(),
    appState: AppState = AppState(),
    onToggleStrategy: () -> Unit = {},
    onToggleMode: () -> Unit = {},
    onEmergencyStop: () -> Unit = {},
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DashboardScreenContent(
        uiState = uiState,
        appState = appState,
        onToggleStrategy = onToggleStrategy,
        onToggleMode = onToggleMode,
        onEmergencyStop = onEmergencyStop,
        onRetry = onRetry,
        modifier = modifier
    )
}

@Composable
private fun DashboardScreenContent(
    uiState: DashboardUiState,
    appState: AppState,
    onToggleStrategy: () -> Unit,
    onToggleMode: () -> Unit,
    onEmergencyStop: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.loading.isLoading -> {
                DashboardLoadingScreen(message = uiState.loading.loadingMessage)
            }
            uiState.error.hasError -> {
                DashboardErrorScreen(
                    message = uiState.error.errorMessage,
                    isRetryable = uiState.error.isRetryable,
                    onRetry = onRetry
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Stats
                    QuickStatsSection(appState.dailyStats)

                    // Strategy Status
                    StrategyStatusCard(
                        status = appState.strategyStatus,
                        onToggleStrategy = onToggleStrategy
                    )

                    // ORB Levels
                    if (appState.orbLevels != null) {
                        OrbLevelsCard(orbLevels = appState.orbLevels)
                    }

                    // Quick Actions
                    QuickActionsSection(
                        tradingMode = appState.tradingMode,
                        onToggleMode = onToggleMode,
                        onEmergencyStop = onEmergencyStop
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsSection(stats: DailyStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = if (stats.totalPnl >= 0) "+₹${String.format("%.0f", stats.totalPnl)}"
                    else "₹${String.format("%.0f", stats.totalPnl)}",
            label = "Today's P&L",
            color = if (stats.totalPnl >= 0) ProfitColor else LossColor,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            value = stats.activePositions.toString(),
            label = "Active",
            color = Primary,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            value = "${String.format("%.0f", stats.winRate)}%",
            label = "Win Rate",
            color = Warning,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StrategyStatusCard(
    status: StrategyStatus,
    onToggleStrategy: () -> Unit
) {
    OrbCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Strategy Status",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusIndicator(status = status)
            }

            StrategyToggleButton(
                status = status,
                onClick = onToggleStrategy
            )
        }
    }
}

@Composable
private fun StatusIndicator(status: StrategyStatus) {
    val (text, color) = when (status) {
        StrategyStatus.ACTIVE -> "● Running" to Success
        StrategyStatus.INACTIVE -> "○ Inactive" to TextSecondary
        StrategyStatus.PAUSED -> "◐ Paused" to Warning
        StrategyStatus.ERROR -> "● Error" to Error
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}

@Composable
private fun StrategyToggleButton(
    status: StrategyStatus,
    onClick: () -> Unit
) {
    val (icon, text, color) = when (status) {
        StrategyStatus.ACTIVE, StrategyStatus.PAUSED ->
            Triple(Icons.Default.Stop, "Stop", Error)
        StrategyStatus.INACTIVE, StrategyStatus.ERROR ->
            Triple(Icons.Default.PlayArrow, "Start", Success)
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        modifier = Modifier.height(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun OrbLevelsCard(orbLevels: OrbLevels) {
    OrbCard {
        SectionHeader(
            text = "ORB 15-Min Levels",
            icon = Icons.Default.Timeline
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoRow(
            label = "Instrument:",
            value = orbLevels.instrument.displayName
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Current LTP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LTP:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "₹${String.format("%.2f", orbLevels.ltp)}",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )
        }

        Divider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = SurfaceVariant
        )

        // High/Low levels
        InfoRow(
            label = "H0 (High):",
            value = "₹${String.format("%.2f", orbLevels.high)}",
            valueColor = Success
        )

        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(
            label = "L0 (Low):",
            value = "₹${String.format("%.2f", orbLevels.low)}",
            valueColor = Error
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Breakout Buffer: ±${orbLevels.breakoutBuffer} ticks",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun QuickActionsSection(
    tradingMode: TradingMode,
    onToggleMode: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onToggleMode,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = when (tradingMode) {
                    TradingMode.PAPER -> Icons.Default.Shield
                    TradingMode.LIVE -> Icons.Default.TrendingUp
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (tradingMode) {
                        TradingMode.PAPER -> "Paper Mode"
                        TradingMode.LIVE -> "Live Mode"
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Button(
            onClick = onEmergencyStop,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Error
            )
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Emergency",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ================== LOADING & ERROR SCREENS ==================

@Composable
private fun DashboardLoadingScreen(message: String = "Loading dashboard...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DashboardErrorScreen(
    message: String,
    isRetryable: Boolean = true,
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error Loading Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            if (isRetryable) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}


// ================== PREVIEW COMPOSABLES - LIVE MODE ONLY ==================

@Preview(name = "Dashboard - Live Mode - Success", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DashboardScreenLiveSuccessPreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        DashboardScreenContent(
            uiState = DashboardPreviewProvider.sampleDashboardUiState(),
            appState = DashboardPreviewProvider.sampleAppState(tradingMode = TradingMode.LIVE),
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Dashboard - Loading State (Live)", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DashboardScreenLoadingPreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        DashboardScreenContent(
            uiState = DashboardPreviewProvider.sampleDashboardUiState(isLoading = true),
            appState = DashboardPreviewProvider.sampleAppState(tradingMode = TradingMode.LIVE),
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Dashboard - Error State Retryable (Live)", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DashboardScreenErrorRetryablePreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        DashboardScreenContent(
            uiState = DashboardPreviewProvider.sampleDashboardUiState(
                hasError = true,
                errorMessage = "Failed to connect to server. Please check your internet connection."
            ),
            appState = DashboardPreviewProvider.sampleAppState(tradingMode = TradingMode.LIVE),
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Dashboard - Error State Non-Retryable (Live)", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DashboardScreenErrorNonRetryablePreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        DashboardScreenContent(
            uiState = DashboardUiState(
                loading = LoadingState(isLoading = false),
                error = ErrorState(
                    hasError = true,
                    errorMessage = "Authorization failed. Please login again.",
                    isRetryable = false
                )
            ),
            appState = DashboardPreviewProvider.sampleAppState(tradingMode = TradingMode.LIVE),
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Dashboard - Positive P&L (Live)", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DashboardScreenPositivePnlPreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        DashboardScreenContent(
            uiState = DashboardPreviewProvider.sampleDashboardUiState(),
            appState = DashboardPreviewProvider.sampleAppState(tradingMode = TradingMode.LIVE, totalPnl = 5000.0, winRate = 75.0),
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Dashboard - Negative P&L (Live)", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DashboardScreenNegativePnlPreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        DashboardScreenContent(
            uiState = DashboardPreviewProvider.sampleDashboardUiState(),
            appState = DashboardPreviewProvider.sampleAppState(tradingMode = TradingMode.LIVE, totalPnl = -1250.0, winRate = 35.0),
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Dashboard - Strategy Inactive (Live)", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DashboardScreenStrategyInactivePreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        DashboardScreenContent(
            uiState = DashboardPreviewProvider.sampleDashboardUiState(),
            appState = DashboardPreviewProvider.sampleAppState(tradingMode = TradingMode.LIVE, strategyStatus = StrategyStatus.INACTIVE),
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Dashboard - Multiple Positions (Live)", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun DashboardScreenMultiplePositionsPreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        DashboardScreenContent(
            uiState = DashboardPreviewProvider.sampleDashboardUiState(),
            appState = DashboardPreviewProvider.sampleAppState(tradingMode = TradingMode.LIVE, activePositions = 5, totalPnl = 3500.50, winRate = 72.5),
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {},
            onRetry = {}
        )
    }
}


package com.trading.orb.ui.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trading.orb.data.model.*
import com.trading.orb.ui.components.*
import com.trading.orb.ui.theme.*

@Composable
fun DashboardScreen(
    appState: AppState,
    onToggleStrategy: () -> Unit,
    onToggleMode: () -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
        // Toggle Mode Button - Use MaterialTheme colors for dynamic theme
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

        // Emergency Stop Button
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

// Preview with sample data - Paper Mode
@Preview(name = "Dashboard - Paper Mode")
@Composable
fun DashboardScreenPaperPreview() {
    val sampleState = AppState(
        tradingMode = TradingMode.PAPER,
        strategyStatus = StrategyStatus.ACTIVE,
        connectionStatus = ConnectionStatus.CONNECTED,
        dailyStats = DailyStats(
            totalPnl = 2450.0,
            activePositions = 2,
            winRate = 68.0
        ),
        orbLevels = OrbLevels(
            instrument = Instrument(
                symbol = "NIFTY24DEC22000CE",
                exchange = "NSE",
                lotSize = 50,
                tickSize = 0.05,
                displayName = "NIFTY 22000 CE"
            ),
            high = 188.0,
            low = 183.0,
            ltp = 185.50,
            breakoutBuffer = 2
        )
    )

    OrbTradingTheme(tradingMode = TradingMode.PAPER) {
        DashboardScreen(
            appState = sampleState,
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {}
        )
    }
}

// Preview with sample data - Live Mode
@Preview(name = "Dashboard - Live Mode")
@Composable
fun DashboardScreenLivePreview() {
    val sampleState = AppState(
        tradingMode = TradingMode.LIVE,
        strategyStatus = StrategyStatus.ACTIVE,
        connectionStatus = ConnectionStatus.CONNECTED,
        dailyStats = DailyStats(
            totalPnl = 2450.0,
            activePositions = 2,
            winRate = 68.0
        ),
        orbLevels = OrbLevels(
            instrument = Instrument(
                symbol = "NIFTY24DEC22000CE",
                exchange = "NSE",
                lotSize = 50,
                tickSize = 0.05,
                displayName = "NIFTY 22000 CE"
            ),
            high = 188.0,
            low = 183.0,
            ltp = 185.50,
            breakoutBuffer = 2
        )
    )

    OrbTradingTheme(tradingMode = TradingMode.LIVE) {
        DashboardScreen(
            appState = sampleState,
            onToggleStrategy = {},
            onToggleMode = {},
            onEmergencyStop = {}
        )
    }
}

// Preview for individual components - Paper Mode
@Preview(name = "Components - Paper Mode")
@Composable
fun ComponentsPaperPreview() {
    OrbTradingTheme(tradingMode = TradingMode.PAPER) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ...existing code...
            Text(
                text = "Component Preview - Paper Mode",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )

            Divider(color = SurfaceVariant, thickness = 1.dp)

            // Quick Stats Cards
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = "+₹2,450",
                    label = "Today's P&L",
                    color = ProfitColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "2",
                    label = "Active",
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "68%",
                    label = "Win Rate",
                    color = Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = SurfaceVariant, thickness = 1.dp)

            // Strategy Status
            Text(
                text = "Strategy Status",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            OrbCard {
                StatusIndicator(status = StrategyStatus.ACTIVE)
                Spacer(modifier = Modifier.height(12.dp))
                StatusIndicator(status = StrategyStatus.PAUSED)
            }

            Divider(color = SurfaceVariant, thickness = 1.dp)

            // Order Side Badges
            Text(
                text = "Order Side Badges",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OrderSideBadge(OrderSide.BUY)
                OrderSideBadge(OrderSide.SELL)
            }
        }
    }
}

// Preview for individual components - Live Mode
@Preview(name = "Components - Live Mode")
@Composable
fun ComponentsLivePreview() {
    OrbTradingTheme(tradingMode = TradingMode.LIVE) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ...existing code...
            Text(
                text = "Component Preview - Live Mode",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )

            Divider(color = SurfaceVariant, thickness = 1.dp)

            // Quick Stats Cards
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = "+₹2,450",
                    label = "Today's P&L",
                    color = ProfitColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "2",
                    label = "Active",
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "68%",
                    label = "Win Rate",
                    color = Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = SurfaceVariant, thickness = 1.dp)

            // Strategy Status
            Text(
                text = "Strategy Status",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            OrbCard {
                StatusIndicator(status = StrategyStatus.ACTIVE)
                Spacer(modifier = Modifier.height(12.dp))
                StatusIndicator(status = StrategyStatus.PAUSED)
            }

            Divider(color = SurfaceVariant, thickness = 1.dp)

            // Order Side Badges
            Text(
                text = "Order Side Badges",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OrderSideBadge(OrderSide.BUY)
                OrderSideBadge(OrderSide.SELL)
            }
        }
    }
}


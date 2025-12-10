package com.trading.orb.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trading.orb.ui.components.*
import com.trading.orb.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MoreScreen(
    onNavigateToRisk: () -> Unit,
    onNavigateToLogs: () -> Unit,
    modifier: Modifier = Modifier,
    brokerName: String = "Zerodha",
    isConnected: Boolean = true,
    onDisconnect: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Features Section
        Text(
            text = "FEATURES",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MenuCard(
            icon = Icons.Default.Shield,
            title = "Risk & Safety",
            subtitle = "Limits & Circuit Breakers",
            iconTint = Warning,
            onClick = onNavigateToRisk
        )

        MenuCard(
            icon = Icons.Default.Timeline,
            title = "Live Logs",
            subtitle = "Real-time monitoring",
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = onNavigateToLogs
        )

        MenuCard(
            icon = Icons.Default.BarChart,
            title = "Backtesting",
            subtitle = "Test strategies",
            iconTint = Success,
            onClick = { /* Navigate to backtesting */ }
        )

        MenuCard(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            subtitle = "Manage alerts",
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = { /* Navigate to notifications */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Settings Section
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MenuCard(
            icon = Icons.Default.Settings,
            title = "App Settings",
            subtitle = "Preferences & display",
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { /* Navigate to settings */ }
        )

        MenuCard(
            icon = Icons.Default.Security,
            title = "Security",
            subtitle = "Biometric lock",
            iconTint = Error,
            onClick = { /* Navigate to security */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Broker Section
        Text(
            text = "BROKER",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BrokerCard(
            brokerName = brokerName,
            isConnected = isConnected,
            onDisconnect = onDisconnect
        )

        Spacer(modifier = Modifier.height(8.dp))

        // About Section
        MenuCard(
            icon = Icons.Default.Info,
            title = "About",
            subtitle = "Version 1.0.0",
            iconTint = TextSecondary,
            onClick = { /* Navigate to about */ }
        )
    }
}

@Composable
private fun MenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OrbCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BrokerCard(
    brokerName: String,
    isConnected: Boolean,
    onDisconnect: () -> Unit
) {
    OrbCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isConnected) Success else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Broker: $brokerName",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isConnected) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isConnected) Success else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isConnected) {
                TextButton(onClick = onDisconnect) {
                    Text(
                        text = "Disconnect",
                        color = Error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                Button(
                    onClick = { /* Handle connect */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Connect")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "More Screen - Paper Mode")
@Composable
fun MoreScreenPreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.PAPER) {
        MoreScreen(
            onNavigateToRisk = {},
            onNavigateToLogs = {}
        )
    }
}

@Preview(showBackground = true, name = "More Screen - Live Mode")
@Composable
fun MoreScreenLivePreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        MoreScreen(
            onNavigateToRisk = {},
            onNavigateToLogs = {}
        )
    }
}

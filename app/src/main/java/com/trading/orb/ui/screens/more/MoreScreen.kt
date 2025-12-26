package com.trading.orb.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.trading.orb.ui.utils.*
import androidx.navigation.NavController
import com.trading.orb.ui.components.*
import com.trading.orb.ui.event.MoreUiEvent
import com.trading.orb.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.trading.orb.ui.navigation.Screen
import com.trading.orb.ui.utils.LaunchEventCollector
import com.trading.orb.ui.utils.*

@Composable
fun MoreScreen(
    navController: NavController? = null,
    viewModel: MoreViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.moreUiState.collectAsStateWithLifecycle()
    
    LaunchEventCollector(eventFlow = viewModel.uiEvent) { event ->
        when (event) {
            is MoreUiEvent.ShowError -> {}
            is MoreUiEvent.ShowSuccess -> {}
            is MoreUiEvent.OpenFeedback -> {}
            is MoreUiEvent.OpenUrl -> {}
            is MoreUiEvent.ShareApp -> {}
        }
    }
    
    MoreScreenContent(
        uiState = uiState,
        navController = navController,
        modifier = modifier
    )
}

@Composable
private fun MoreScreenContent(
    uiState: MoreUiState = MoreUiState(),
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PADDING_STANDARD),
        verticalArrangement = Arrangement.spacedBy(PADDING_MEDIUM)
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
            onClick = { navController?.navigate(Screen.Risk.route) }
        )

        MenuCard(
            icon = Icons.Default.Timeline,
            title = "Live Logs",
            subtitle = "Real-time monitoring",
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = { navController?.navigate(Screen.Logs.route) }
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

        Spacer(modifier = Modifier.height(PADDING_SMALL))

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

        Spacer(modifier = Modifier.height(PADDING_SMALL))

        // Broker Section
        Text(
            text = "BROKER",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BrokerCard(
            brokerName = uiState.brokerName,
            isConnected = uiState.isConnected,
            onDisconnect = { }
        )

        Spacer(modifier = Modifier.height(PADDING_SMALL))

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
                horizontalArrangement = Arrangement.spacedBy(PADDING_LARGE),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(ICON_SIZE_LARGE)
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
                horizontalArrangement = Arrangement.spacedBy(PADDING_LARGE),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isConnected) Success else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(ICON_SIZE_LARGE)
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
        MoreScreenContent(uiState = MorePreviewProvider.sampleMoreUiState())
    }
}

@Preview(showBackground = true, name = "More Screen - Live Mode")
@Composable
fun MoreScreenLivePreview() {
    OrbTradingTheme(tradingMode = com.trading.orb.data.model.TradingMode.LIVE) {
        MoreScreenContent(uiState = MorePreviewProvider.sampleMoreUiState())
    }
}

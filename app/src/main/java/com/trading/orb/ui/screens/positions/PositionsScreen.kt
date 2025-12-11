package com.trading.orb.ui.screens.positions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trading.orb.data.model.*
import com.trading.orb.ui.components.*
import com.trading.orb.ui.event.PositionsUiEvent
import com.trading.orb.ui.state.PositionUiModel
import com.trading.orb.ui.theme.*
import com.trading.orb.ui.utils.LaunchDataLoader
import com.trading.orb.ui.utils.LaunchEventCollector
import java.time.LocalDateTime

@Composable
fun PositionsScreen(
    viewModel: PositionsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.positionsUiState.collectAsStateWithLifecycle()
    
    LaunchDataLoader(viewModel = viewModel) {
        viewModel.loadPositions()
    }
    
    LaunchEventCollector(eventFlow = viewModel.uiEvent) { event ->
        when (event) {
            is PositionsUiEvent.ShowError -> {}
            is PositionsUiEvent.ShowSuccess -> {}
            is PositionsUiEvent.NavigateToPositionDetails -> {}
        }
    }
    
    PositionsScreenContent(
        uiState = uiState,
        onClosePosition = { positionId -> viewModel.closePosition(positionId) },
        modifier = modifier
    )
}

@Composable
private fun PositionsScreenContent(
    uiState: PositionsUiState,
    onClosePosition: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Summary Header
        PositionsSummaryHeader(uiState = uiState)

        // Positions List
        if (uiState.positions.isEmpty()) {
            EmptyPositionsView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.positions) { positionUiModel ->
                    PositionCard(
                        positionUiModel = positionUiModel,
                        onClose = { onClosePosition(positionUiModel.positionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionsSummaryHeader(uiState: PositionsUiState) {
    val totalPnL = uiState.totalProfit + uiState.totalLoss

    OrbCard(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Total Open P&L",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            PnLDisplay(
                pnl = totalPnL,
                fontSize = 32
            )
        }
    }
}

@Composable
private fun PositionCard(
    positionUiModel: PositionUiModel,
    onClose: () -> Unit
) {
    var showCloseDialog by remember { mutableStateOf(false) }

    val borderColor = if (positionUiModel.profitLoss >= 0) Success else Error

    OrbCard(
        modifier = Modifier.border(
            width = 2.dp,
            color = borderColor,
            shape = RoundedCornerShape(12.dp)
        )
    ) {
        // Header: Instrument name and side badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = positionUiModel.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Qty: ${positionUiModel.quantity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            OrderSideBadge(side = if (positionUiModel.type == "LONG") OrderSide.BUY else OrderSide.SELL)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price info grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PriceInfoColumn(
                label = "Entry",
                value = "₹${String.format("%.2f", positionUiModel.entryPrice)}",
                modifier = Modifier.weight(1f)
            )
            PriceInfoColumn(
                label = "Current",
                value = "₹${String.format("%.2f", positionUiModel.currentPrice)}",
                valueColor = if (positionUiModel.profitLoss >= 0) Success else Error,
                modifier = Modifier.weight(1f)
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "P&L",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PnLDisplay(
                    pnl = positionUiModel.profitLoss,
                    percentage = positionUiModel.profitLossPercent,
                    fontSize = 16
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SL and Target
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelBox(
                label = "SL",
                value = "₹${String.format("%.2f", positionUiModel.stopLoss ?: 0.0)}",
                color = Error,
                modifier = Modifier.weight(1f)
            )
            LevelBox(
                label = "Target",
                value = "₹${String.format("%.2f", positionUiModel.takeProfit ?: 0.0)}",
                color = Success,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Entry time
        Text(
            text = "Entry: ${positionUiModel.openTime}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Close button
        Button(
            onClick = { showCloseDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Error
            )
        ) {
            Text(
                text = "Close Position",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    // Confirmation dialog
    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Close Position?") },
            text = {
                Text("Are you sure you want to close this position for ${positionUiModel.symbol}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClose()
                        showCloseDialog = false
                    }
                ) {
                    Text("Close", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PriceInfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}

@Composable
private fun LevelBox(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = color
            )
        }
    }
}

@Composable
private fun EmptyPositionsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary
            )
            Text(
                text = "No Active Positions",
                style = MaterialTheme.typography.headlineLarge,
                color = TextSecondary
            )
            Text(
                text = "Start the strategy to open positions",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
        }
    }
}

// Preview
@Preview
@Composable
fun PositionsScreenPreview() {
    OrbTradingTheme {
        PositionsScreenContent(
            uiState = PositionsPreviewProvider.samplePositionsUiState(),
            onClosePosition = {}
        )
    }
}

@Preview
@Composable
fun PositionsScreenEmptyPreview() {
    OrbTradingTheme {
        PositionsScreenContent(
            uiState = PositionsPreviewProvider.samplePositionsUiStateEmpty(),
            onClosePosition = {}
        )
    }
}

@Preview
@Composable
fun PositionsScreenLoadingPreview() {
    OrbTradingTheme {
        PositionsScreenContent(
            uiState = PositionsPreviewProvider.samplePositionsUiStateLoading(),
            onClosePosition = {}
        )
    }
}

@Preview
@Composable
fun PositionsScreenErrorPreview() {
    OrbTradingTheme {
        PositionsScreenContent(
            uiState = PositionsPreviewProvider.samplePositionsUiStateError(),
            onClosePosition = {}
        )
    }
}


